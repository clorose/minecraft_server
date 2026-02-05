# Minecraft Economy Server - 컴팩트 전 정보

## 서버 기본 정보
- **서버**: Purpur 1.21.10-2535
- **월드 생성**: Iris 플러그인 (직접 빌드)
- **패키지 명**: `io.clorose`

## 현재 세션 진행 상황 (2026-02-05 완료)

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
