# Wolf of Minestreet — 시스템 설계 레퍼런스

## 개요

마인크래프트 서버 내 주식/코인 거래소 플러그인. 가상 종목 30개 + 동적 코인 5개를 운영하며, GBM(기하 브라운 운동) 기반 가격 엔진 + 3단계 스토리 이벤트 시스템으로 가격이 결정됨.

---

## 1. 가격 엔진 (PriceEngine.kt)

### 1.1 틱 주기
- **10초** 간격 (TICK_INTERVAL = 200L, 마인크래프트 틱 기준)
- 1분 = 6틱, 1시간 = 360틱

### 1.2 틱당 가격 계산 흐름

```
1. 쿨다운 감소
2. 후속 기사 처리
3. YAML 스토리 이벤트 롤 (시장 → 업종 → 종목)
4. ActiveEvent에서 이번 틱 점프 계산
5. 종목별 가격 업데이트 (GBM + 평균회귀 + 이벤트)
6. VI 체크
7. 글로벌 랜덤 이벤트 롤
8. 코인 생명주기 (CoinManager.tick)
9. 6틱마다 저장
```

### 1.3 수익률 계산 (computeReturn)

```
rGbm  = mu - v²/2 + v*z         // GBM (이토 보정)
rMr   = mrStrength * ln(ma/price) // MA120 평균회귀
rBase = clamp(rGbm + rMr, -capBase, +capBase)
rEvent = 이벤트 점프              // capEvent 개별 적용됨
최종   = clamp(rBase + rEvent, -0.20, +0.20)  // SAFETY_CAP
```

- **z**: 표준정규분포 난수 (ThreadLocalRandom.nextGaussian)
- **v**: 종목별 volatility (YAML에서 로드)
- **mu**: 주식 0, COIN -0.0004, MEMECOIN -0.001 (누름목)
- **이토 보정** (-v²/2): GBM의 기대수익률을 0으로 맞춤
- **SAFETY_CAP ±20%**: 버그/이벤트 중첩 시 폭주 방지 안전망

### 1.4 이동평균 (MA)
- HISTORY_SIZE = 120틱 (20분)
- 종목별 ArrayDeque에 가격 기록, 평균값 계산
- 평균회귀: 현재가가 MA보다 높으면 하락 압력, 낮으면 상승 압력

---

## 2. 등급 시스템 (AssetGrade.kt)

| 등급        | capBase        | mrStrength | viEnabled | viThreshold | floorPrice | liquidation | isCoin |
| ----------- | -------------- | ---------- | --------- | ----------- | ---------- | ----------- | ------ |
| BLUECHIP    | 0.002 (±0.2%)  | 0.015      | true      | ±8%         | 1.0        | false       | false  |
| GROWTH      | 0.003 (±0.3%)  | 0.01       | true      | ±8%         | 1.0        | false       | false  |
| SPECULATIVE | 0.005 (±0.5%)  | 0.005      | true      | ±10%        | 1.0        | false       | false  |
| COIN        | 999.0 (무제한) | 0          | false     | 없음        | 0.01       | true        | true   |
| MEMECOIN    | 999.0 (무제한) | 0          | false     | 없음        | 0.01       | true        | true   |

**설계 의도:**
- BLUECHIP: 안정적, 평균회귀 강함, 뉴스 없으면 거의 안 움직임
- GROWTH: 중간 변동성, 적당한 평균회귀
- SPECULATIVE: 높은 변동성, 약한 평균회귀, VI 기준도 넓음
- COIN/MEMECOIN: 캡 없음, 평균회귀 없음, 하락 드리프트로 자연 하락

### volatility 범위 (YAML)
- BLUECHIP: 0.0015 ~ 0.003
- GROWTH: 0.003 ~ 0.005
- SPECULATIVE: 0.004 ~ 0.007
- COIN: 0.02 ~ 0.04 (CoinManager에서 생성 시 랜덤)
- MEMECOIN: 0.04 ~ 0.06

**capBase와 volatility 관계:** volatility ≈ capBase 수준이어야 종목 개성이 살아남. volatility가 capBase보다 크면 GBM이 항상 캡에 걸려서 전 종목 동일한 움직임이 됨.

---

## 3. 이벤트 시스템

### 3.1 YAML 스토리 이벤트

3단계 구조:
1. **시장** (`_market.yml`): 전 종목에 영향
2. **업종** (`{category}/_common.yml`): 해당 업종 종목에 영향
3. **종목** (`{category}/{company}.yml`): 해당 종목에만 영향

**YAML 이벤트 구조:**
```yaml
events:
  - headline: "뉴스 헤드라인"
    tier: NORMAL | MAJOR | EXTREME
    bullish: true | false | null(랜덤)
    probability: 0.025  # 틱당 발생 확률
    jumpMin: 0.005
    jumpMax: 0.03
    # 선택: 분기형 이벤트
    successRate: 0.5
    success:
      headline: "성공 헤드라인"
      jump: 0.10
      tier: EXTREME
      followUp:           # 후속 기사
        delayTicks: 5
        headline: "후속 헤드라인"
        jump: 0.04
        tier: MAJOR
    failure:
      headline: "실패 헤드라인"
      jump: -0.08
      tier: MAJOR
```

### 3.2 EventTier

| Tier    | label      | capEvent | durationTicks | cooldownTicks | viFreezeTicks | jumpMin | jumpMax |
| ------- | ---------- | -------- | ------------- | ------------- | ------------- | ------- | ------- |
| NORMAL  | 속보       | 3%       | 1             | 30 (5분)      | 3 (30초)      | 0.5%    | 3%      |
| MAJOR   | §6긴급     | 8%       | 3             | 60 (10분)     | 6 (60초)      | 3%      | 8%      |
| EXTREME | §c§l초대형 | 15%      | 6             | 180 (30분)    | 9 (90초)      | 8%      | 15%     |

- **capEvent**: 해당 이벤트의 틱당 최대 점프 (resolveJumpsForStock에서 적용)
- **durationTicks**: 점프를 몇 틱에 걸쳐 분배할지
- **cooldownTicks**: 같은 스코프에서 다음 이벤트까지 대기
- **viFreezeTicks**: VI 발동 시 거래정지 틱 수

### 3.3 ActiveEvent (진행 중인 이벤트)

```kotlin
data class ActiveEvent(
    key, tier, bullish, totalJump,
    remainingTicks, deliveredJump,
    scope: MARKET | CATEGORY | COMPANY,
    category?, stockId?
)
```

- totalJump를 remainingTicks에 걸쳐 분배
- 틱당 점프 = (남은량 / 남은틱) ± 20% 노이즈
- 분배 완료 후 자동 제거

### 3.4 글로벌 랜덤 이벤트

- **트리거**: 8%/틱 (GLOBAL_EVENT_CHANCE)
- **하위이벤트 수**: 1개(50%), 2개(35%), 3개(15%)
- **대상**: 전체 자산(주식30+코인5) 중 랜덤, 중복 방지
- **주식 타겟**: ±0.5~1.5%, 60% 하락/40% 상승, 3틱 분배
- **코인 타겟**: CoinManager.applyRandomEvent() 위임

---

## 4. VI (Volatility Interrupt)

### 발동 조건
- `abs((현재가 - MA) / MA) > viThreshold`
- BLUECHIP/GROWTH: ±8%, SPECULATIVE: ±10%
- 코인은 VI 없음

### 동작
1. 발동 → 해당 종목 거래정지 (viFreezeTicks만큼)
2. 정지 중 가격 변동 없음 (tick에서 return)
3. 정지 해제 → 쿨다운 5틱 (50초간 재발동 불가)
4. 서버 공지 + 뉴스 등록

---

## 5. 코인 시스템 (CoinManager.kt)

### 5.1 기본 운영
- 항상 5개 활성: COIN 3개 + MEMECOIN 2개
- 부족하면 자동 상장
- 이름: PREFIXES(25개) × SUFFIXES(8개) 조합, 중복 방지

### 5.2 가격 특성
- capBase 무제한 (999.0) → GBM 출력 그대로 적용
- 평균회귀 없음 (mrStrength = 0)
- **누름목 (하락 드리프트)**: mu에 음수값 추가
  - COIN: -0.0004/틱 → 시간당 ~15% 하락 압력
  - MEMECOIN: -0.001/틱 → 시간당 ~30% 하락 압력

### 5.3 코인 이벤트 (글로벌 롤에서 코인 선택 시)

| 이벤트 | 기본 가중치   | 부스트 가중치 | 효과                    |
| ------ | ------------- | ------------- | ----------------------- |
| 러그풀 | 5% (밈 10%)   | 동일          | 가격 → 0.001, 즉시 상폐 |
| 문샷   | 5%            | 10%           | +50~200% 즉시           |
| 펌프   | 20%           | 30%           | +10~50% 즉시            |
| 덤프   | 나머지 (~70%) | ~55%          | -5~15% 즉시             |

- **상장 부스트**: 신규 상장 후 18틱(3분) 동안 펌프/문샷 확률 증가

### 5.4 생명주기

```
상장 → (누름목으로 자연 하락) → 청산 체크 → 상폐 체크 → 교체
```

1. **청산**: 보유자의 평균매수가 대비 -50% → 강제 매도 + 현금 지급
2. **상폐**: 가격 ≤ 0.01원 → 보유자 전액 손실 + StockManager에서 제거
3. **교체 대기**: 상폐 후 30틱(5분) 대기
4. **신규 상장**: 같은 등급으로 새 코인 생성

### 5.5 저장

- `active_coins.yml`: 활성 코인 정보, 사용된 이름, 교체 대기 목록
- `stocks.yml`: StockManager가 모든 자산(주식+코인) 저장 → 상폐 시 제거됨

---

## 6. 웹 대시보드

### 6.1 백엔드 (WebServer.kt)

| 엔드포인트               | 설명                                |
| ------------------------ | ----------------------------------- |
| `/api/stocks`            | 주식 30종목 (코인 제외, grade 포함) |
| `/api/coins`             | 활성 코인 목록                      |
| `/api/ohlc?symbol=XXX`   | 1분 캔들 OHLC 데이터                |
| `/api/trades?symbol=XXX` | 체결 내역 (선택적 필터)             |
| `/api/news`              | 최근 뉴스                           |

### 6.2 등락률 기준 (refPrices)
- 서버 시작 시 전 종목 현재가 스냅샷
- 1시간마다 리셋
- 등락률 = (현재가 - refPrice) / refPrice × 100

### 6.3 OHLC (TradeRecorder.kt)
- 1분 캔들, 종목당 최대 60개 (1시간분)
- 메모리만 (서버 재시작 시 초기화)
- PriceEngine.tick()에서 updatePrice() 호출

### 6.4 프론트엔드
- React + lightweight-charts v5
- 한국 HTS 라이트 테마 (빨강=상승, 파랑=하락)
- 주식/코인 탭 분리, 행 클릭 → 캔들봉 차트 + 체결 내역

---

## 7. 거래 (WomCommand.kt)

### 명령어
- `/주식 목록` — 전 종목 현황
- `/주식 정보 <티커>` — 상세 정보 (가격, 등락, 거래량)
- `/주식 매수 <티커> <수량>` — 매수
- `/주식 매도 <티커> <수량>` — 매도
- `/주식 포트폴리오` — 내 보유 현황

### 매수/매도 처리
1. VI 정지 중이면 거래 거부
2. 가격 × 수량 계산
3. Vault 경제에서 출금/입금
4. Portfolio 업데이트
5. stock.totalVolume += amount
6. TradeRecorder에 기록

---

## 8. 파일 구조

```
plugins/wolf-of-minestreet/
├── companies/              # 종목 YAML (이벤트 포함)
│   ├── _market.yml         # 시장 전체 이벤트
│   ├── tech/
│   │   ├── _common.yml     # 업종 공통 이벤트
│   │   ├── nayuta.yml      # 종목별 설정 + 이벤트
│   │   ├── zenith.yml
│   │   └── silicondyn.yml
│   ├── industrial/
│   ├── transport/
│   ├── pharma/
│   ├── finance/
│   ├── food/
│   ├── entertainment/
│   ├── retail/
│   ├── realestate/
│   └── luxury/
├── stocks.yml              # 런타임 데이터 (현재가, 거래량)
├── active_coins.yml        # 활성 코인 상태
├── portfolios.yml          # 플레이어 포트폴리오
└── companies.yml           # (구버전, 미사용)

clorose/wolf-of-minestreet/src/main/kotlin/io/clorose/wolfOfMinestreet/
├── WolfOfMinestreet.kt     # 메인 플러그인 클래스
├── model/
│   ├── Stock.kt            # 종목 데이터 클래스
│   ├── AssetGrade.kt       # 등급 enum
│   ├── EventTier.kt        # 이벤트 등급 enum
│   ├── Portfolio.kt        # 플레이어 포트폴리오
│   ├── OhlcCandle.kt       # OHLC 캔들 모델
│   └── TradeRecord.kt      # 체결 기록 모델
├── engine/
│   ├── PriceEngine.kt      # 가격 엔진 (핵심)
│   ├── CoinManager.kt      # 코인 생명주기 관리
│   ├── NewsManager.kt      # 뉴스 관리
│   ├── EventRegistry.kt    # YAML 이벤트 로더
│   └── TradeRecorder.kt    # OHLC + 체결 기록
├── manager/
│   ├── StockManager.kt     # 종목 CRUD + 저장/로드
│   └── PortfolioManager.kt # 포트폴리오 관리
├── command/
│   └── WomCommand.kt       # /주식 명령어 처리
└── web/
    └── WebServer.kt        # HTTP API 서버

web/                        # 프론트엔드 (React)
├── src/
│   ├── App.jsx
│   ├── App.css
│   ├── hooks/useApi.js
│   └── components/
│       ├── Header.jsx
│       ├── TabBar.jsx
│       ├── StockTable.jsx
│       ├── CoinTable.jsx
│       ├── StockRow.jsx
│       ├── CoinRow.jsx
│       ├── Sparkline.jsx
│       ├── NewsFeed.jsx
│       ├── DetailPanel.jsx
│       ├── CandlestickChart.jsx
│       └── TradeHistory.jsx
└── package.json
```

---

## 9. 상수 요약

### PriceEngine
| 상수                | 값      | 설명                      |
| ------------------- | ------- | ------------------------- |
| TICK_INTERVAL       | 200L    | 10초 (MC 틱)              |
| HISTORY_SIZE        | 120     | MA120 = 20분              |
| SAVE_INTERVAL       | 6       | 60초마다 저장             |
| GLOBAL_EVENT_CHANCE | 0.08    | 8%/틱 ≈ 분당 0.5회        |
| MAX_SUB_EVENTS      | 3       | 글로벌 롤 최대 하위이벤트 |
| COIN_DRIFT          | -0.0004 | COIN 하락 드리프트        |
| MEMECOIN_DRIFT      | -0.001  | MEMECOIN 하락 드리프트    |
| SAFETY_CAP          | 0.20    | ±20%/틱 안전망            |
| VI_COOLDOWN_TICKS   | 5       | VI 해제 후 쿨다운 50초    |
| MARKET_COOLDOWN     | 60      | 시장 이벤트 쿨다운 10분   |
| COMPANY_COOLDOWN    | 18      | 종목 이벤트 쿨다운 3분    |

### CoinManager
| 상수                    | 값   | 설명                  |
| ----------------------- | ---- | --------------------- |
| TARGET_COINS            | 3    | 활성 COIN 수          |
| TARGET_MEMECOINS        | 2    | 활성 MEMECOIN 수      |
| REPLACEMENT_DELAY_TICKS | 30   | 상폐 후 교체 대기 5분 |
| LISTING_PUMP_DURATION   | 18   | 상장 부스트 3분       |
| LIQUIDATION_THRESHOLD   | 0.5  | 평균매수가 -50% 청산  |
| DELIST_PRICE            | 0.01 | 상폐 기준가           |

---

## 10. 예상 변동폭 (시간대별)

이벤트 없이 GBM만:

| 등급        | 10분  | 1시간  |
| ----------- | ----- | ------ |
| BLUECHIP    | ±1.5% | ±3~4%  |
| GROWTH      | ±2.5% | ±5~6%  |
| SPECULATIVE | ±4%   | ±8~10% |

이벤트 포함 (1시간):

| 등급        | 일반적  | MAJOR 이벤트 시 |
| ----------- | ------- | --------------- |
| BLUECHIP    | ±5~8%   | ±10~15%         |
| GROWTH      | ±7~10%  | ±12~18%         |
| SPECULATIVE | ±10~15% | ±15~25%         |
