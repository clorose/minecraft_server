# Minecraft Economy Server - 컴팩트 전 정보

## 서버 기본 정보
- **서버**: Purpur 1.21.10-2535
- **월드 생성**: Iris 플러그인 (직접 빌드)
- **패키지 명**: `io.clorose`

## 현재 세션 진행 상황 (2026-02-05)

### Iris 월드 정리 목표
- 동굴/광석/구조물/몹 스폰 제거
- 큰 나무 제거 (smoakog, tredwood 등)
- 바닐라 데코레이션 유지 (꽃, 풀, 일반 나무)
- 해수면 Y=16

### 완료된 작업

#### Dimension 레벨
- [x] caves: [] (동굴 비활성화)
- [x] jigsawStructures: [] (구조물 비활성화)
- [x] deposits: [] (광석 비활성화)
- [x] fluidHeight: 16 (해수면)
- [x] 지형 높이 조정 (>=100 값에서 32 빼기)

#### Region 레벨 (9개 파일 전부)
- [x] jigsawStructures: [] (던전, 포탈 등)
- [x] caveBiomes: []
- [x] entitySpawners: [] (몹 스폰)
- [x] carving.ravines: [] (협곡)
- [x] objects: [] (자수정 지하구조물)
- [x] deposits: []

#### Biome 레벨 - 큰 나무 제거
- [x] smoakog80 제거 (4개 파일)
- [x] tredwood 전부 제거 (10개 파일)
- [x] 포탈 구조물 제거 (5개 파일)

#### Biome 레벨 - 카테고리별 정리
- [x] frozen/ (28개) - jigsawStructures, obelisk, grave, snowulder, landstone, camp, gall 제거
- [x] hot/ (13개) - jigsawStructures, pyramid, ruins, sphinx, shipwreck, oruins 제거
- [x] mesa/ (15개) - jigsawStructures, camp, bincluster 제거
- [x] mountain/ (15개) - landstone, obelisk, bincluster 제거
- [ ] mushroom/ (10개)
- [ ] ocean/ (7개)
- [ ] savanna/ (7개)
- [ ] swamp/ (18개)
- [ ] temperate/ (43개)
- [ ] terralost/ (70개)
- [ ] tropical/ (30개)
- [ ] tundra/ (28개)
- [x] carving/ (31개) - 스킵 (동굴 비활성화됨)

### 제거 대상 패턴
**jigsawStructures**: village-*, pillager-outpost, igloo, pyramid-*, trail-ruins, dungeon-*
**objects 제거**:
- 구조물: pyramid, ruins, sphinx, obelisk, camp, landstone, snowulder, desertpost
- 난파선: swreck, usdship, ship*, gall
- 유적: oruins, ruins-desert
- 기타: bomb, bincluster, grave

**objects 유지**:
- 일반 나무: trees/oak/*, trees/spruce/*, trees/birch/*, palm*
- 자연 장식: clutter/sbush*, clutter/boulder*, icecluster*, icespec*
- 데코레이터: 꽃, 풀, 펀 등

### Git 커밋 이력
```
50bfffb Clean frozen biomes: remove structures and jigsawStructures
b3a8521 Remove tredwood (big redwood) trees from biome files
9951109 Remove portal structures from biome files
c5a2fd6 Remove giant trees (smoakog80) from biome files
8a51893 Remove structures, mobs, caves from all region files
7025428 Lower high terrain (>=100) by 32 blocks
ab13c2b Set fluidHeight to 16 as per readme spec
```

### **긴급 작업 재시작 (현재 세션)**

**문제**: 이전에 grep 검색만 하고 파일을 하나하나 읽지 않아서 토큰 낭비 및 누락 발생
**해결**: 모든 biome 파일 325개를 처음부터 하나씩 읽으면서 체크

**체크 항목**:
- jigsawStructures: 구조물 제거
- objects: 대형 나무/구조물 제거
  - 제거 대상 대형 나무: hoakgeneric, thoakgeneric, antioch, largeponderosa, lgeneric, largegeneric, lfrostgeneric, spire, AmyLarge
  - 유지 대상 작은 나무: shoakgeneric, sgeneric, generic, forest, cocogeneric
- carving: 동굴 제거
- decorators: kelp, seagrass 제거

**현재 진행 상황** (tropical/ 폴더):
- wilds.json ✓ (cocogeneric, sgeneric만 - OK)
- rainforest-hills.json ⚠️ **spire1-7 나무 발견 - 제거 필요**
- rainforest-island.json ✓ (cocogeneric, sgeneric만 - OK)
- rainforest-wicked-child.json ✓ (cocogeneric, sgeneric만 - OK)

**대기 중** (frozen/fields/):
- cold-spines.json ⚠️ **lfrostgeneric 제거 필요**
- hills.json ⚠️ **lfrostgeneric 제거 필요**

### 다음 할 일
1. **즉시**: tropical/rainforest-hills.json에서 spire 나무 제거
2. tropical 폴더의 모든 파일 하나씩 읽으면서 체크
3. 다른 모든 폴더도 하나씩 체크 (mushroom, ocean, savanna, swamp, temperate, terralost, tundra, frozen 재확인)
4. 테스트 월드 생성 및 확인
5. 커밋

### 테스트 명령어
```
/iris create name=test8 type=overworld
/iris tp world=test8
```

## 주요 파일 경로
```
plugins/Iris/packs/overworld/
├── dimensions/overworld.json (dimension 설정)
├── regions/*.json (9개 region 파일)
└── biomes/ (325개 바이옴 파일)
    ├── carving/ (31개, 스킵)
    ├── frozen/ (28개, 완료)
    ├── hot/ (13개, 완료)
    ├── mesa/ (15개, 완료)
    ├── mountain/ (15개, 완료)
    ├── mushroom/ (10개)
    ├── ocean/ (7개)
    ├── savanna/ (7개)
    ├── swamp/ (18개)
    ├── temperate/ (43개)
    ├── terralost/ (70개)
    ├── tropical/ (30개)
    └── tundra/ (28개)
```
