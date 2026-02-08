# Minecraft Economy Server - 컴팩트 전 정보

## 서버 기본 정보
- **서버**: Purpur 1.21.10-2535
- **패키지 명**: `io.clorose`
- **경제 구조**: ShopGUI+ 상점 판매/구매가 유일한 수입/지출 경로 (Jobs 제거됨)

---

## 이번 세션 변경사항 (2026-02-08)

### 플러그인 정리
- ✅ ItemsAdder → Nexo (nexo-1.19.jar) 교체
- ✅ Jobs 제거 (설치/파괴 악용, ShopGUI+만으로 경제 운영)
- ✅ BattlePass 제거 (미설정)
- ✅ RealisticSeasons 제거 (성능)
- ✅ wolf-of-minestreet 제거 (이번 시즌 안 씀, 소스코드 clorose/에 보존)
- ✅ MobFarmManager 제거 후 복구 (엔티티 렉 방지용으로 유지)

### clorose-tracker 구현 완료
- ShopGUI+ 거래 즉시 기록 (transactions.csv)
- 접속/퇴장/1시간/서버종료 잔액 스냅샷 (balances.csv)
- plugins/에 배포 완료

### 현재 플러그인 목록 (21개)
| 분류 | 플러그인 |
|---|---|
| 경제 | Vault, ShopGUIPlus |
| 관리/유틸 | CMI, CMILib, LuckPerms, PlaceholderAPI, ProtocolLib |
| 월드 | multiverse-core, worldedit, worldguard |
| NPC/몹 | Citizens, MythicMobs, ModelEngine, MythicCrucible, MobFarmManager |
| 생활 | LiteFarm, LiteFish, LiteCooking, Lands |
| 커스텀 | Nexo, HeadDatabase, AdvancedEnchantments, AdvancedCrates |
| 자체 | clorose-tracker |

### 결정사항
- 이번 시즌 주식 안 돌림 — 경제 데이터 수집 목적
- MythicCrucible 유지 (드릴 등 커스텀 도구 제작용)
- wom 상세 정보는 `docs/wom-reference.md`로 분리

## TODO
- [ ] 서버 시작 + 플러그인 동작 확인
- [ ] ShopGUI+ 상점 가격 설정
- [ ] Nexo 초기 설정
- [ ] 플러그인 한국어 번역
