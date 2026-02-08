# Iris 바이옴 나무 정리

## 목표
평탄화가 귀찮은 나무들을 제거하여 깨끗한 스폰 월드 생성

## 제거 기준
- ❌ **제거**: 초거대 나무 (sup-pine, lgeneric, largegeneric 등), 울타리(fence) 나무
- ✅ **유지**: 일반 크기 나무, 큰 얼음 기둥(icespike), 장식용 clutter, icecluster, icespec

---

## 전체 나무 통계
- **총 나무**: 1,536개
- **✅ 찹트리 가능**: 509개 (33.1%)
- **❌ 찹트리 불가능**: 1,027개 (66.9%)
- **나무 타입**: 15개 (acacia, birch, bonsai, corrupted, crimson, darkoak, jungle, mangrove, mixed, mushroom, oak, sakura, sproak, spruce, willow)

---

## 타입별 상세 목록

### ACACIA (73개)
- ✅ **찹트리 가능** (25개): 17, 18, savannaF4~F16, savannaS1~S3, savannaS6~S7, savannas9~s11, vexed1~3
- ❌ **찹트리 불가능** (48개):
  - **denmyre** 1~16: acacia_fence, acacia_planks
  - **savana** 1~5: oak_fence
  - **savannaD** 1~3: acacia_fence
  - **savannaF** 1~3, 17: acacia_fence
  - **savannaS** 4, 5, 8, s12: acacia_fence

### BIRCH (74개)
- ✅ **찹트리 가능** (17개): 1~17, fantasy1~5, forest1~4, generic1~5, large1~8, regeneric1~3, rgeneric1~3, tgeneric1~10, thin1~20
- ❌ **찹트리 불가능** (57개):
  - **denmyre** 1~7: birch_fence, birch_planks
  - **fancy** 1~10: birch_stairs, birch_slab
  - **btree** 1~17: birch_fence/planks/slab/stairs
  - **구조물 계열**: pillars, towers, ramps 등

### BONSAI (1개)
- ✅ **찹트리 가능** (1개): bonsai1

### CORRUPTED (42개)
- ❌ **찹트리 불가능** (42개 전부):
  - c1~c3, c5~c7: stone, iron_bars, chain
  - sc1~sc7: stone, cobblestone
  - wc1~wc7: stone, chain, iron_bars
  - rc1~rc7, tc1~tc9, vc1~vc9: 다양한 stone 블록들

### CRIMSON (38개)
- ✅ **찹트리 가능** (2개): ccrimson1, ccrimson2
- ❌ **찹트리 불가능** (36개):
  - **crimson** 1~25: nether_wart_block, shroomlight, blackstone, nether_bricks
  - **scrimson** 1~11: 위와 동일

### DARKOAK (156개)
- ✅ **찹트리 가능** (42개): croak1~17, 19, pollup1~13, shoakgeneric1~10
- ❌ **찹트리 불가능** (114개):
  - **dadwood** 1, 3~6: oak_fence, oak_planks
  - **denmyre** 1~7: oak_fence, oak_planks
  - **toak** 1~4: oak_fence
  - **oakFancy** 1~10: oak_stairs, oak_slab
  - **croak** 18: moving_piston
  - **roofed** 계열: fence, planks, stairs, slab

### JUNGLE (97개)
- ✅ **찹트리 가능** (63개): generic1~7, lgeneric1~9, palm1~7, sgeneric1, 3, 5~14, tall1~8, thin1~8, thin-med1~7, thin-short1~9, variety1~10
- ❌ **찹트리 불가능** (34개):
  - **sgeneric** 2, 4: barrier
  - **btree** 1~17: jungle_fence/planks/slab
  - **largejungle** 1~15: jungle_fence, jungle_planks

### MANGROVE (36개)
- ✅ **찹트리 가능** (17개): mangrove1~4, 6
- ❌ **찹트리 불가능** (19개):
  - **mangrove** 5, 7~20: coarse_dirt
  - **sman** 1~16: coarse_dirt, mud, muddy_mangrove_roots

### MIXED (77개)
- ✅ **찹트리 가능** (55개): pollup1~13
- ❌ **찹트리 불가능** (22개):
  - **dotree** 2~10: gray_terracotta
  - **poptree** 1~12: stone, cobblestone, polished_andesite

### MUSHROOM (67개)
- ❌ **찹트리 불가능** (67개 전부):
  - shroomlight 포함 (버섯 구조물이므로 정상)
  - lumo, redlumo, redlumotall, shroom, wshroom 등

### OAK (379개)
- ✅ **찹트리 가능** (144개): croak1~17, 19, generic1~18, genericsak1~5, lgeneric1~13, llg1~12, shoakgeneric1~10, sgeneric1~17, toak5~11, truegeneric1, 7~11, 13~15, 18~19, 24
- ❌ **찹트리 불가능** (235개):
  - **dadwood** 1, 3~6: oak_fence, oak_planks
  - **denmyre** 1~7: oak_fence, oak_planks
  - **toak** 1~4: oak_fence
  - **truegeneric** 2~6, 12, 16~17, 20~23: oak_fence, spruce_slab, vine
  - **oakFancy** 1~10: oak_stairs, oak_slab
  - **mroofed** 1~12: dark_oak_planks
  - **croak** 18: moving_piston
  - **large/roofed/super** 계열: 다양한 fence, planks, stairs, slab

### SAKURA (33개)
- ✅ **찹트리 가능** (8개): genericsak1~5, sakura1~3
- ❌ **찹트리 불가능** (25개):
  - **bambooT** 1~7: bamboo, bamboo_planks
  - **lsakura** 1~18: sakura_fence, sakura_planks (커스텀 블록)

### SPROAK (21개)
- ✅ **찹트리 가능** (21개 전부): sp1~3, sproak1~18

### SPRUCE (434개)
- ✅ **찹트리 가능** (159개): evergreen1~10, generic1~18, levergreen1~6, mevergreen1~9, pine1~4, sgeneric1~13, vgeneric1, 6, 11, 13~18, 22~28
- ❌ **찹트리 불가능** (275개):
  - **vgeneric** 2~5, 7~10, 12, 19~21, 29~32: spruce_fence
  - **large/lgeneric/largegeneric** 계열: spruce_fence, spruce_planks, spruce_stairs
  - **sup-pine** 1~8: spruce_fence, cobblestone, stone
  - **unfrosted** 계열: 다양한 블록들

### WILLOW (108개)
- ❌ **찹트리 불가능** (108개 전부):
  - **bt** 1~6, 9: birch_fence, birch_planks
  - **t** 1~8: oak_fence, oak_planks
  - **w** 1~9: oak_fence, oak_planks
  - **willow** 1~82: oak_fence, oak_planks, oak_slab

---

## 주요 문제 블록별 분류

### Fence (울타리) 포함
- acacia_fence: denmyre, savannaD/F/S
- birch_fence: birch/denmyre, btree
- oak_fence: dadwood, denmyre, toak, willow 계열
- spruce_fence: vgeneric 일부, large 계열

### Planks (판자) 포함
- acacia_planks: denmyre
- birch_planks: birch/denmyre, btree
- dark_oak_planks: mroofed
- jungle_planks: largejungle
- oak_planks: dadwood, denmyre, roofed, willow

### Stairs/Slab (계단/반블록) 포함
- oak_stairs/slab: oakFancy, willow, roofed 계열
- birch_stairs/slab: fancy, btree
- spruce_slab: truegeneric1
- jungle_slab: btree

### 기타 특수 블록
- barrier: jungle/sgeneric2, 4
- coarse_dirt: mangrove, sman
- gray_terracotta: dotree
- shroomlight: mushroom 계열 (정상)
- moving_piston: croak18
- stone/cobblestone: corrupted, poptree, sup-pine

---

## 나무 크기별 분류

### 크기 기준
- **초거대**: 파일 50KB 이상 또는 블록 1000개 이상 - **제거 권장**
- **거대**: 20KB 이상 또는 500블록 이상
- **큰**: 10KB 이상 또는 250블록 이상
- **중간**: 5KB 이상 또는 100블록 이상
- **작은**: 5KB 미만

### 크기별 개수
- **초거대**: 67개
- **거대**: 105개
- **큰**: 220개
- **중간**: 343개
- **작은**: 801개

### 초거대 나무 목록 (제거 권장)

#### BONSAI
- smbase1, smbon1~3, smfallen1

#### CRIMSON
- bonehand1~3

#### DARKOAK
- large-10, smdeadwillow1~2, smwillow1~4

#### JUNGLE
- largegeneric1~5, smgeneric1

#### MIXED
- AmyLarge1~2, 4~6, dead1, smoakog1, smoakog80, smoakog160

#### OAK
- deadfallen1, massivegeneric1~3, mdeadwood1~3, sloak1
- smdeadwilt1, smdeadwood1~3, smfallen1, smoak1, smtoak1, smwiltedoak1

#### SAKURA
- mlarge1~3, 9~11, ogabsurd1~2, ogabsurdfallen1, ogbase1~2
- smtaint1~2, smwilt1~2, vlarge1~3

#### SPRUCE
- unfrostedmed1~3

---

## 울타리 나무 종류 분석

### vgeneric (trees/spruce/vgeneric)
- **울타리 나무** (16개): 2, 3, 4, 5, 7, 8, 9, 10, 12, 19, 20, 21, 29, 30, 31, 32
- **일반 나무** (16개): 1, 6, 11, 13, 14, 15, 16, 17, 18, 22, 23, 24, 25, 26, 27, 28

### truegeneric (trees/oak/truegeneric)
- **울타리 나무** (12개): 2, 3, 4, 5, 6, 12, 16, 17, 20, 21, 22, 23
- **일반 나무** (12개): 1, 7, 8, 9, 10, 11, 13, 14, 15, 18, 19, 24

### denmyre (trees/acacia/denmyre)
- **울타리 나무** (16개): 1~16 전부

### dadwood (trees/oak/dadwood)
- **울타리 나무** (5개): 1, 3, 4, 5, 6
- **일반 나무** (1개): 2

### savanna 나무
- **savannaD**: D1, D2, D3 전부 울타리
- **savannaF**: F1, F2, F3, F17 울타리 / F4-F14 일반
- **savannaS**: S4, S5, S8, s12 울타리 / S1, S2, S3, S6, S7, s9, s10, s11 일반

### toak (trees/oak/toak)
- **울타리 나무** (4개): 1, 2, 3, 4
- **일반 나무** (7개): 5, 6, 7, 8, 9, 10, 11

---

## 찹트리 가능 여부 검증 방법

### 검증 명령어
```bash
strings 파일.iob | grep "minecraft:" | sed 's/\[.*//g' | sort | uniq | grep -vE "(log|wood|leaves|stripped)"
```

- **출력 없음** = 순수 나무만 (log/wood/leaves만 사용, 찹트리 ✅)
- **출력 있음** = 다른 블록 있음 (fence/stairs/slab/stone 등, 찹트리 ❌)

### 찹트리 가능한 나무
- **shoakgeneric** (trees/oak/shoakgeneric): 1~10 전부
- **croak** (trees/oak/croak): 1~17, 19 (18은 moving_piston)
- **pollup** (trees/mixed/pollup): 1~13 전부
- **toak** (trees/oak/toak): 5~11
- **birch/forest** (trees/birch/forest): 1~4 전부
- **genericsak** (trees/sakura/genericsak): 1~5 전부

### 찹트리 불가능한 나무 (제거됨)
- **toak** 1~4: fence 포함
- **oakFancy** 1~10: stairs/slab 포함
- **croak** 18: moving_piston 포함

---

## 바이옴에서 제거 필요한 나무

### 1. 찹트리 불가능한 나무 (필수 제거)

1. **denmyre** 전체 (1~16) - acacia/birch/oak 모두
2. **mroofed** 1~12 - dark_oak_planks
3. **dotree** 2~10 - gray_terracotta
4. **mangrove** 5, 7~20 - coarse_dirt
5. **jungle/sgeneric** 2, 4 - barrier
6. **truegeneric** 울타리 variants - fence/slab
7. **vgeneric** 울타리 variants (2~5, 7~10, 12, 19~21, 29~32)
8. **dadwood** 1, 3~6 - fence/planks
9. **toak** 1~4 - fence
10. **oakFancy** 1~10 - stairs/slab
11. **willow** 전체 - fence/planks/slab

**예외:** 버섯 나무(mushroom), 구조물(corrupted, crimson의 일부)은 제외

### 2. 초거대 나무 (제거 권장)

위 목록의 초거대 나무들도 바이옴에서 제거 권장 (평탄화가 귀찮음)

---

## 나무 대체 원칙

1. **같은 재질** - oak -> oak, spruce -> spruce, acacia -> acacia
2. **비슷한 크기** - 초거대 -> 거대/큰, 작은 -> 작은
3. **찹트리 가능 확인** - verify_trees.py 사용

### 예시

**Swamp (mroofed 제거):**
```json
"place": [
  "trees/oak/croak1",
  "trees/oak/croak2",
  "trees/oak/pollup1",
  "trees/mangrove/mangrove1"
]
```

**Savanna (denmyre 제거):**
```json
"place": [
  "trees/acacia/vexed1",
  "trees/acacia/vexed2",
  "trees/acacia/savannaF4",
  "trees/acacia/savannaS1"
]
```

**Frozen (vgeneric 울타리 제거):**
```json
"place": [
  "trees/spruce/vgeneric1",
  "trees/spruce/vgeneric6",
  "trees/spruce/evergreen1",
  "trees/spruce/generic1"
]
```

**Tropical (jungle/sgeneric 2,4 제거):**
```json
"place": [
  "trees/jungle/palm1",
  "trees/jungle/generic1",
  "trees/jungle/sgeneric1",
  "trees/jungle/sgeneric3"
],
"edit": [
  {
    "find": [{"block": "minecraft:oak_button"}],
    "replace": {"palette": [{"block": "minecraft:air"}]}
  },
  {
    "find": [{"block": "minecraft:vine"}],
    "replace": {"palette": [{"block": "minecraft:air"}]}
  }
]
```

---

## 제거 완료 목록 (63개 파일)

### tundra (9개)
1. tundra/sequia-redwoods.json - sup-pine
2. tundra/sequia-redwoods-extended.json - sup-pine
3. tundra/redwood-extended-cliffs.json - sup-pine
4. tundra/ether-extended.json - dotree1-10 -> pollup1-8
5. tundra/ether.json - dotree1-10 -> pollup1-8
6. tundra/magic-forest-extended.json - dotree1-10 -> pollup1-8
7. tundra/magic-forest.json - dotree1-10 -> pollup1-8
8. tundra/spruce-denmyre.json - denmyre1-7 -> levergreen1-6 + mevergreen1-3
9. tundra/sea/lake.json - denmyre1-7 -> levergreen1-6 + mevergreen1-3

### tropical (4개)
10. tropical/jungle-denmyre.json - denmyre1-7 -> jungle/generic1-4 + sgeneric1,3
11. tropical/beach-charred.json - palm1,2,3,9,10,11,12 -> cocogeneric2-4 + sgeneric1-4
12. tropical/beach.json - palm1,2,3,9,10,11,12 -> cocogeneric2-4 + sgeneric1-4
13. tropical/island-beach.json - palm1-8 -> cocogeneric2-5 + sgeneric1-4

### frozen (13개)
14. frozen/fields/mountain-spruce-winter-extended.json - vgeneric 울타리 나무
15. frozen/fields/mountain-spruce-winter.json - vgeneric 울타리 나무
16. frozen/mountains/large-mountain-top.json - pine
17. frozen/sea/ocean.json - icespike
18. frozen/sea/river.json - icespike
19. frozen/shore/beach.json - icespike
20. frozen/hills-extended.json - pine
21. frozen/ice-spikes.json - icecluster
22. frozen/pine-hills.json - pine
23. frozen/pine-plains.json - pine
24. frozen/plains.json - pine, icespike
25. frozen/pines.json - icespike (evergreen 유지)
26. frozen/vander.json - ice/froShroom 버섯, sproak/generic 울타리, singleicespike

### mesa (1개)
27. mesa/valleys.json - savannaD 전체, savannaF(F1,F2,F3), savannaS(S4,S5,S8,s12)

### mountain (3개)
28. mountain/plain-extended.json - truegeneric 3, 4, 5 울타리 -> oak/generic 추가
29. mountain/mplain-extended.json - 나무 없음 -> oak/generic 추가

### mushroom (2개)
30. mushroom/warped-forest-extended.json - 버섯->돌 edit 제거
31. mushroom/warped-forest.json - 버섯->돌 edit 제거

### savanna (8개)
32. savanna/shore/beach.json - obelisk, literalgarbage (전리품)
33. savanna/acacia-denmyre.json - denmyre 전체 -> acacia/vexed + savannaF
34. savanna/forest.json - dadwood 1,3,4,5,6 -> acacia/vexed + savannaF
35. savanna/cliff-extended.json - savannaD 전체, savannaF(F1,F2,F3,F17), savannaS(S4,S5,S8,s12)
36. savanna/cliff.json - savannaD 전체, savannaF(F1,F2,F3,F17), savannaS(S4,S5,S8,s12)
37. savanna/plateau.json - savannaD 전체, savannaF(F1,F2,F3,F17), savannaS(S4,S5,S8,s12)
38. savanna/savanna.json - savannaD 전체, savannaF(F1,F2,F3,F17), savannaS(S4,S5,S8,s12)

### swamp (9개)
39. swamp/creaks.json - mroofed1-12 -> oak/croak1-5 + pollup1-3
40. swamp/denmyre.json - denmyre1-7 -> acacia/vexed1-3 + savannaF4-6
41. swamp/roofed-forest-extended.json - mroofed1-12 -> oak/croak1-5 + pollup1-3
42. swamp/roofed-forest.json - mroofed1-12 -> oak/croak1-5 + pollup1-3
43. swamp/roofed-wayward-extended.json - mroofed1-12 -> oak/croak1-5 + pollup1-3
44. swamp/roofed-wayward.json - mroofed1-12 -> oak/croak1-5 + pollup1-3
45. swamp/swamp-forest.json - dotree1-10 -> pollup1-8
46. swamp/swamp-puddle.json - willow/t1-8 -> darkoak/willowgeneric + generic, mangrove/t -> mangrove
47. swamp/swamp-mangrove-lake.json - mangrove5-20 제거

### temperate (13개)
48. temperate/sea/ocean.json - obelisk (전리품)
49. temperate/sea/river.json - obelisk (전리품)
50. temperate/shore/oak-beach.json - obelisk, literalgarbage (전리품)
51. temperate/shore/beach.json - bincluster1 (배럴 전리품)
52. temperate/birch-denmyre.json - denmyre 1~7 (울타리)
53. temperate/oak-denmyre.json - denmyre1-7 -> acacia/vexed1-3 + savannaF4-6
54. temperate/combo-forest-extended.json - toak 1,2,3,4 (울타리) + literalgarbage
55. temperate/combo-forest.json - toak 1,2,3,4 (울타리) + literalgarbage
56. temperate/cherry-blossom-forest.json - literalgarbage
57. temperate/flower-forest-extended.json - literalgarbage
58. temperate/flower-forest.json - literalgarbage
59. temperate/fancyplains.json - oakFancy -> shoakgeneric 교체 (stairs/slab 문제)
60. temperate/croak.json - croak18 (moving_piston)

### terralost (1개)
61. terralost/ - WIP 폴더 확인, 나머지 choppable 나무만 사용

---

## 진행 상황

- **완료**: 63개 파일 (unchoppable 나무 교체)
- **이전 세션**: 57개 파일 (구조물/동굴/광석 제거)
- **전체**: ~325개 바이옴 파일
- **진행률**: ~37% (120/325)
