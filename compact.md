# Minecraft Economy Server - 컴팩트 전 정보

## 서버 기본 정보
- **서버**: Purpur 1.21.10-2535
- **월드 생성**: Iris 플러그인 (직접 빌드)
- **패키지 명**: `io.clorose`

## 최신 세션 진행 상황 (2026-02-07 10:00 진행중)

### 서버 테스트 및 JSON 에러 수정 🔄 진행중

**작업 목표**:
- 새 월드 생성하여 Iris 바이옴 수정 사항 테스트
- 서버 정상 시작 확인

**작업 내용**:
1. 월드 폴더 삭제 (world, world_nether, world_the_end)
2. 서버 백그라운드 실행: `nohup ./start.sh > server.log 2>&1 &`

**발생한 문제**:
- 서버 크래시: `java.lang.RuntimeException: Iris failed to replace the levelStem`
- 에러 메시지: `Missing element ResourceKey[minecraft:dimension_type / iris:aiamgajach8.]`

**트러블슈팅 과정**:
1. troubleshooting/2026-02-06-iris-json-trailing-comma.md 확인
   - 이전에 동일한 에러 발생 경험 있음
   - 원인: JSON trailing comma

2. JSON 에러 수정 (5개 파일):
   - `swamp/sea/ocean.json`: line 61 trailing comma 제거
   - `temperate/meadows.json`: lines 124, 128 missing commas 추가
   - `temperate/reaching-forest-violet.json`: line 111 trailing comma 제거
   - `temperate/reaching-forest.json`:
     - lines 89, 91, 123 missing commas 추가
     - lines 132, 136, 140, 147, 154 missing commas 추가
   - `temperate/stranged-plains.json`:
     - lines 72, 100, 106, 110, 117, 123, 128, 135 missing commas 추가

3. 삭제된 폴더 Git에서 복구 (커밋 154c7d0):
   - `plugins/Iris/packs/overworld/biomes/carving/`: 동굴 바이옴 정의 파일
   - `plugins/Iris/packs/overworld/biomes/terralost/WIP/`: 74개 WIP 바이옴 파일

4. JSON 검증:
   - 모든 수정된 파일 Python json.tool로 검증 완료
   - trailing comma 에러 모두 수정 확인

**해결 과정**:
1. 에러 원인 발견: **temperate/*.bak 파일** (3개)
   - meadows.json.bak, reaching-forest.json.bak, stranged-plains.json.bak
   - 모두 invalid JSON이었으나 Iris가 로드 시도
   - .bak 파일 삭제 후 크래시 → Unstable Mode로 변경

2. 추가 조치:
   - Iris 캐시 삭제 (`plugins/Iris/cache/*`)
   - 월드 폴더 삭제 및 재생성
   - 여러 차례 서버 재시작 시도

**현재 상태**: ⚠️  부분 해결
- ✅ .bak 파일 제거로 즉시 크래시는 해결
- ❌ 여전히 Iris Unstable Mode 진입
- ❌ "Required Iris dimension types were not loaded" 에러 지속
- ❌ "Custom Biomes: 0" - Iris가 custom biomes 인식 못함
- ❌ "The Pack overworld is INCAPABLE of generating custom biomes"

**근본 원인 분석**:
- 188개 바이옴 파일에서 `customDerivitives` 사용
- Iris가 customDerivitives 처리하려면 추가 설정/데이터팩 필요할 수 있음
- dimension_type 등록 자체가 실패하고 있음

**다음 조치 필요**:
- Iris custom biomes 사용 방법 확인
- 데이터팩 필요 여부 확인
- customDerivitives 제거 고려 (하지만 188개 파일...)
- Iris Discord/지원 포럼 문의 고려

**참고**:
- 이전 트러블슈팅(2026-02-06)에서는 JSON 수정만으로 해결
- 이번에는 .bak 파일 문제 + customDerivitives 문제 복합적

---

## 이전 세션 진행 상황 (2026-02-06 16:30 완료)

### 월드 생성 및 트러블슈팅 ✓ 완료

**작업 내용**:
1. Git 커밋: 바이옴 파일 57개 수정 사항 커밋 (9090d9e)
2. bukkit.yml 정리: test1~5 월드 설정 제거
3. 테스트 월드 삭제: test1~5 폴더 삭제
4. world 폴더 삭제 및 재생성

**발생한 문제**:
- 서버 크래시: `java.lang.RuntimeException: Iris failed to replace the levelStem`
- Iris Unstable Mode 진입
- 바이옴/Dimension Type 등록 실패

**원인**:
- JSON trailing comma 3개 발견
  - temperate/sea/ocean-deep.json:88
  - frozen/sea/frozen-river-ice.json:127
  - frozen/sea/ocean.json:126

**해결**:
- trailing comma 제거
- 서버 재시작
- world 폴더 Iris로 정상 생성 확인

**결과**: ✅ 성공
- 서버 정상 시작 (Done preparing level "world" (4.731s))
- world/iris/ 폴더 생성됨
- world/mantle/ 폴더 생성됨
- Iris Unstable Mode 없음

**참고 문서**: troubleshooting/2026-02-06-iris-json-trailing-comma.md

### 플러그인 프로젝트 계획 및 생성 ✓ 완료

**작업 내용**:
1. 경제 시스템 확장 계획
   - 주식/코인 시스템 추가 결정
   - 플러그인 이름: **Wolf of Minestreet** 확정

2. 플러그인 이름 변경
   - `soul-tax` → `mine-insurance` (사망 패널티 → 사망 보험)

3. wolf-of-minestreet 프로젝트 생성
   - 언어: Kotlin 2.3.10
   - API: Paper 1.21 (api-version: 1.21)
   - 빌드 시스템: Gradle + Shadow
   - 패키지: `io.clorose.wolfofminestreet`

4. Git 작업
   - .gitignore 업데이트: test*/ 추가 (월드 데이터 제외)
   - test1 제거 커밋 (4b8ac75)
   - Git push: 26개 커밋 업로드

5. 문서 작업
   - troubleshooting/ 폴더 생성
   - troubleshooting/README.md 작성 (작성 가이드)
   - README.md 업데이트 (플러그인 목록)

**최종 커스텀 플러그인 목록**:
- `clorose-core` - 공통 API/유틸 (Kotlin 2.3.0)
- `longshot` - 경마 시스템 (Kotlin 2.3.0)
- `mine-atelier` - 장비 시스템 (Kotlin 2.3.0)
- `mine-insurance` - 사망 보험 (Kotlin 2.3.10) ✨ 신규
- `wolf-of-minestreet` - 주식/코인 거래소 (Kotlin 2.3.10) ✨ 신규

### Kotlin 버전 통일 및 정리 ✓ 완료

**작업 내용**:
1. Kotlin 버전 통일
   - 기존: clorose-core, longshot, mine-atelier (2.3.0)
   - 신규: mine-insurance, wolf-of-minestreet (2.3.10)
   - **전체 2.3.10으로 업그레이드 완료**

2. soul-tax 폴더 삭제
   - mine-insurance로 완전 대체
   - 중복 제거 완료

**최종 플러그인 목록 (5개, 모두 Kotlin 2.3.10)**:
1. clorose-core - 공통 API/유틸
2. longshot - 경마 시스템
3. mine-atelier - 장비 시스템
4. mine-insurance - 사망 보험
5. wolf-of-minestreet - 주식/코인 거래소

**플러그인 개발 순서 (컨텐츠 우선순위)**:
1. **wolf-of-minestreet** ⭐⭐ - 주식/코인 거래 (새 컨텐츠, 쉬움)
2. **longshot** ⭐⭐⭐⭐⭐ - 경마+스킬+베팅+NPC (새 컨텐츠, 매우 어려움)
3. **mine-atelier** ⭐⭐⭐⭐⭐ - 장비 강화/룬/세트 (새 컨텐츠, 매우 어려움)
4. **mine-insurance** ⭐ - 사망 보험 (보조 기능, 쉬움)
5. **clorose-core** - 개발 중 중복 코드 발견 시 분리

**개발 우선순위 이유**:
- 컨텐츠 플러그인 우선 (플레이어가 할 수 있는 활동 증가)
- 보조 시스템은 나중에 (게임플레이 보호 장치)
- 난이도보다 서버 재미 요소가 우선

### wolf-of-minestreet 플러그인 개발 ✓ 완료

**작업 내용**:
1. 기본 구조 생성
   - model/Stock.kt - 종목 데이터
   - model/Portfolio.kt - 포트폴리오
   - manager/StockManager.kt - 종목 관리
   - manager/PortfolioManager.kt - 포트폴리오 관리
   - command/WomCommand.kt - /wom 명령어

2. 명령어 구현
   - `/wom list` - 종목 목록
   - `/wom buy <종목> <수량>` - 매수
   - `/wom sell <종목> <수량>` - 매도
   - `/wom info <종목>` - 종목 정보
   - `/wom portfolio` - 포트폴리오

3. 테스트용 종목 등록
   - DIA (다이아몬드) 100원
   - EME (에메랄드) 50원
   - GOLD (금) 30원
   - IRON (철) 10원

4. 빌드 및 배포
   - gradle build 성공
   - plugins/ 폴더에 복사
   - 서버 재시작 후 정상 로드 확인

**TODO**:
- Vault 연동 (실제 돈 차감/지급)
- 가격 변동 시스템
- 데이터 저장 (YAML/JSON)

### ItemsAdder 추가 예정 항목 문서화 ✓ 완료

**작업 내용**:
- docs/itemsadder-todo.md 생성
- 특수 목재 추가 계획 문서화
  - 호두나무 (Walnut)
  - 흑단 (Ebony)
  - 마호가니 (Mahogany)

**참고**: economy-guide.md의 벌목 시스템 연동

### Iris 바이옴 울타리 나무 제거 ✅ 완료 (2026-02-06)

- **목표**: 찹트리 불가능한 나무 전체 제거 및 교체
- **진행**: 24개 파일 수정 완료 (이전 15개 + 이번 9개)
- **상세**: tree.md 참고

**찹트리 불가능한 나무**:
- denmyre (planks, fence 포함)
- mroofed (planks 포함)
- dotree (terracotta 포함)
- mangrove5-20 (coarse_dirt 포함)
- willow/t (fence, planks 포함)
- palm (terracotta 포함)

**교체 원칙**:
- 원본 나무 종류 유지 (아카시아→아카시아, 정글→정글, 윌로우→다크오크)
- edit 블록으로 unchoppable 블록 제거 또는 choppable로 변환
- 나무가 없던 곳에는 적절한 나무 추가

#### 전체 수정 내역 (2026-02-06 완료)

**1. Mountain (2 files)**
- `plain-extended.json`: 나무 없음 → oak generic 추가 (5%)
- `mplain-extended.json`: 나무 없음 → oak generic 추가 (5%)

**2. Savanna (2 files)**
- `acacia-denmyre.json`: denmyre 제거됨 → acacia vexed + savannaF 추가 (25%)
- `forest.json`: dadwood 제거됨 → acacia vexed + savannaF 추가 (30%)

**3. Swamp (9 files)**
- `creaks.json`: mroofed1-12 → oak/croak1-5 + oak/pollup1-3
- `denmyre.json`: denmyre1-7 → acacia/vexed1-3 + acacia/savannaF4-6, edit 블록 제거
- `roofed-forest-extended.json`: mroofed1-12 → oak/croak1-5 + oak/pollup1-3
- `roofed-forest.json`: mroofed1-12 → oak/croak1-5 + oak/pollup1-3
- `roofed-wayward-extended.json`: mroofed1-12 → oak/croak1-5 + oak/pollup1-3
- `roofed-wayward.json`: mroofed1-12 → oak/croak1-5 + oak/pollup1-3
- `swamp-forest.json`: mixed/dotree1-10 → mixed/pollup1-8 (mixed 타입 유지)
- `swamp-puddle.json`: willow/t1-8 → darkoak/willowgeneric1-2 + darkoak/generic1-2, mangrove/t1-4 → mangrove1-4,6 + edit 블록 추가 (planks→stripped_wood)
- `swamp-mangrove-lake.json`: mangrove5-20 제거 (coarse_dirt 포함), mangrove1-4,6만 유지

**4. Temperate (1 file)**
- `oak-denmyre.json`: denmyre1-7 → acacia/vexed1-3 + acacia/savannaF4-6 (acacia 타입 유지), edit 블록 제거

**5. Terralost (확인 완료)**
- 5개 non-WIP 파일 확인: 모두 choppable 나무 사용 (mixed/pollup, mixed/Amy*)
- WIP 폴더 (80 files): 어디서도 참조 안됨 → 삭제 가능

**6. Tropical (4 files)** ✨ 신규
- `jungle-denmyre.json`: denmyre1-7 → jungle/generic1-4 + jungle/sgeneric1,3 (jungle 타입 유지), edit 블록 제거
- `beach-charred.json`: palm1,2,3,9,10,11,12 → jungle/cocogeneric2-4 + jungle/sgeneric1-4
- `beach.json`: palm1,2,3,9,10,11,12 → jungle/cocogeneric2-4 + jungle/sgeneric1-4
- `island-beach.json`: palm1-8 → jungle/cocogeneric2-5 + jungle/sgeneric1-4

**7. Tundra (6 files)** ✨ 신규
- `ether-extended.json`: mixed/dotree1-10 → mixed/pollup1-8
- `ether.json`: mixed/dotree1-10 → mixed/pollup1-8
- `magic-forest-extended.json`: mixed/dotree1-10 → mixed/pollup1-8
- `magic-forest.json`: mixed/dotree1-10 → mixed/pollup1-8
- `spruce-denmyre.json`: acacia/denmyre1-7 → spruce/levergreen1-6 + spruce/mevergreen1-3, edit 블록 제거
- `sea/lake.json`: acacia/denmyre1-7 → spruce/levergreen1-6 + spruce/mevergreen1-3, edit 블록 제거

**최종 통계**:
- 총 24개 파일 수정 완료
- 모든 unchoppable 나무 교체 완료
- 나무 타입 일관성 유지 (jungle→jungle, mixed→mixed, spruce→spruce)

### Git 커밋 완료 ✓

**커밋 1 (154c7d0)**:
- Tropical + Tundra 나무 교체 (12개 파일)
- tree.md, compact.md 업데이트
- +1940/-1626 줄

**커밋 2 (15b9386)**:
- 전체 Iris 바이옴 정리 (316개 파일)
- 동굴/광석/구조물 제거
- Unchoppable 나무 전체 교체
- soul-tax → mine-insurance 이름 변경
- wolf-of-minestreet 추가
- +32408/-37255 줄

### 플러그인 한국어 번역 상태 확인 ✓

**조사 결과 (2026-02-06 16:40)**:
- **번역 완료**: 7개 플러그인
  - ItemsAdder (ko.yml)
  - AdvancedEnchantments (ko.yml)
  - Lands (ko-KR.yml 3개 파일)
  - Jobs (messages_ko.yml, 28KB)
  - ShopGUIPlus (lang.yml)
  - BattlePass (lang.yml)
  - RealisticSeasons (lang.yml)

- **번역 필요**: 1개
  - AdvancedCrates (messages.yml 영어)

- **번역 불필요**: 22개 (시스템/개발용 플러그인)
  - CMI, LuckPerms, PlaceholderAPI, ProtocolLib, Vault, WorldEdit 등

**결론**: 주요 플레이어용 플러그인은 대부분 한국어 완료 ✅

**다음 작업**:
- AdvancedCrates 한국어 번역 (나중에)
- wolf-of-minestreet 개발 계속 (Vault 연동, 가격 변동, 데이터 저장)
- 서버 테스트 (새 월드 생성, Iris 바이옴 확인)

---

## 이전 세션 진행 상황 (2026-02-05 완료)

### Iris 바이옴 파일 정리 작업 ✓ 완료

**목표**: 하위 폴더 포함 전체 바이옴 파일에서 동굴/광석/구조물/대형 나무 제거

**제거 대상**:
- jigsawStructures: 모든 구조물 (pillager-outpost, woodland-mansion, village-plains, ocean-monument)
- carving: 동굴 생성
- deposits: 광석 생성
- objects 내 대형 나무: lgeneric, largegeneric, lfrostgeneric, antioch, largeponderosa, lponderosa, spire, AmyLarge
- objects 내 구조물: pyramid, ruins, sphinx, structures/* (landstone, shipgeneric, BShip, kship, swreck, usdship, oruins, gall, woodhand, swordnp, oakspindle, swampforearm)
- decorators: kelp, seagrass (tall_seagrass 포함)
- layers 내 광석: coal_ore, iron_ore

**유지 대상**:
- 일반 크기 나무: sgeneric, generic, forest, cocogeneric, palm, pine, levergreen, mevergreen, troofed, mroofed, denmyre, pollup, dotree, lumo, AmyMed, AmyNormal, AmySmol
- 바닐라 장식: 꽃, 풀, 산호, clutter (boulder, sbush, ellipsoid, substat 등), sea_pickle

### 이번 세션 수정 내역 (2026-02-05 23:40)

#### 1. tropical/sea (5 files 수정)
- coral-ocean-cliffs.json: seagrass + 2 kelp + carving 제거
- coral-ocean.json: seagrass + kelp + carving 제거
- ocean.json: structures (swreck1, usdship1-2, oruins1-3) + seagrass + 2 kelp + carving 제거
- river-soft.json: structures (landstone1-5, shipgeneric*, BShip*, kship*) + kelp + seagrass + tall_seagrass + carving 제거
- river-steep.json: structures (landstone1-5, shipgeneric*, BShip*, kship*) + kelp + seagrass + tall_seagrass + carving 제거

#### 2. ocean/shore (1 file 수정)
- beach.json: structures (landstone1-5) 제거

#### 3. mesa/sea (1 file 수정)
- river.json: seagrass 제거

#### 4. mountain/sea (2 files 수정)
- river-soft.json: seagrass + kelp 제거
- river.json: seagrass + kelp 제거

#### 5. mushroom/sea (1 file 수정)
- ocean.json: structures (swreck1, usdship1-2) + seagrass + kelp 제거

#### 6. swamp/sea (2 files 수정)
- ocean.json: structures (swreck1, usdship1-2) + seagrass + 2 kelp 제거
- ocean-tree.json: structures (swreck1, usdship1-2) + lgeneric1-9 (대형 나무) + seagrass + 2 kelp 제거

#### 7. swamp/shore (1 file 수정)
- beach.json: lgeneric1-9 (대형 나무) 제거

#### 8. temperate/sea (3 files 수정)
- ocean.json: structures (swreck1, usdship1-2, oruins1-3) + seagrass + kelp 제거
- ocean-deep.json: structures (swreck1, usdship1-2, gall1-3, oruins1-3) + jigsaw (ocean-monument) + seagrass + kelp 제거
- river.json: seagrass + kelp 제거

#### 9. temperate/shore (1 file 수정)
- oak-beach.json: jigsawStructures (pillager-outpost, village-plains) 제거

#### 10. hot/sea (2 files 수정)
- ocean.json: seagrass + kelp 제거
- ocean-cliffs.json: seagrass + kelp 제거

#### 11. frozen/fields (1 file 수정)
- cold-spines.json: lfrostgeneric1-21 (대형 나무) 제거

#### 12. frozen/sea (3 files 수정)
- ocean.json: structures (oruins1-3) 제거
- frozen-parent-river.json: seagrass + kelp 제거
- frozen-river-ice.json: structures (oruins1-3) + seagrass + kelp 제거

#### 13. temperate (확인)
- oak-denmyre.json: 깨끗함, 수정 불필요 (denmyre는 유지 대상 나무)

### 전체 작업 완료!

**최종 통계**:
- 이번 세션 수정: 23개 파일
- 이전 세션 수정: 34개 파일 (tropical 메인, frozen, mesa, mountain, swamp, temperate 메인, terralost, tundra)
- **총 수정 파일: 57개**
- **전체 바이옴 파일: 325개+** (하위 폴더 포함)

**제거된 콘텐츠 (전체)**:
- jigsawStructures: pillager-outpost, woodland-mansion, village-plains, ocean-monument
- carving: 동굴 생성 (volcanic/main, frosted-peaks 등)
- deposits: 광석 생성 (netherrack ore, coal ore, iron ore)
- objects 내 대형 나무: spire, lgeneric, largegeneric, lfrostgeneric, antioch, largeponderosa, lponderosa, AmyLarge
- objects 내 구조물: pyramid, ruins-desert, sphinx, landstone, shipgeneric, BShip, kship, swreck, usdship, oruins, gall, woodhand, swordnp, oakspindle, swampforearm
- decorators: kelp (kelp_plant), seagrass, tall_seagrass

### 테스트 명령어
```
/iris create name=test9 type=overworld
/iris tp world=test9
```

## 주요 파일 경로
```
plugins/Iris/packs/overworld/
├── dimensions/overworld.json
├── regions/*.json
└── biomes/
    ├── tropical/ + sea/ ✓ 완료
    ├── frozen/ + fields/ + sea/ ✓ 완료
    ├── hot/ + sea/ ✓ 완료
    ├── mesa/ + sea/ ✓ 완료
    ├── mountain/ + sea/ ✓ 완료
    ├── mushroom/ + sea/ ✓ 완료
    ├── ocean/ + shore/ ✓ 완료
    ├── swamp/ + sea/ + shore/ ✓ 완료
    ├── temperate/ + sea/ + shore/ ✓ 완료
    ├── terralost/ ✓ 완료
    ├── tundra/ + sea/ ✓ 완료
    └── carving/ ✓ 완료
```
