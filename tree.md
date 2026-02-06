# Iris 바이옴 나무 제거 작업 추적

## 목표
평탄화가 귀찮은 나무들을 제거하여 깨끗한 스폰 월드 생성

## 제거 기준
- ❌ **제거**: 초거대 나무 (sup-pine, lgeneric, largegeneric 등), 울타리(fence) 나무
- ✅ **유지**: 일반 크기 나무, 큰 얼음 기둥(icespike), 장식용 clutter, icecluster, icespec

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

## 제거 완료 목록 (63개)

### tundra (9개)
1. tundra/sequia-redwoods.json - sup-pine
2. tundra/sequia-redwoods-extended.json - sup-pine
3. tundra/redwood-extended-cliffs.json - sup-pine
4. tundra/ether-extended.json - dotree1-10 → pollup1-8
5. tundra/ether.json - dotree1-10 → pollup1-8
6. tundra/magic-forest-extended.json - dotree1-10 → pollup1-8
7. tundra/magic-forest.json - dotree1-10 → pollup1-8
8. tundra/spruce-denmyre.json - denmyre1-7 → levergreen1-6 + mevergreen1-3
9. tundra/sea/lake.json - denmyre1-7 → levergreen1-6 + mevergreen1-3

### tropical (4개)
10. tropical/jungle-denmyre.json - denmyre1-7 → jungle/generic1-4 + sgeneric1,3
11. tropical/beach-charred.json - palm1,2,3,9,10,11,12 → cocogeneric2-4 + sgeneric1-4
12. tropical/beach.json - palm1,2,3,9,10,11,12 → cocogeneric2-4 + sgeneric1-4
13. tropical/island-beach.json - palm1-8 → cocogeneric2-5 + sgeneric1-4

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
28. mountain/plain-extended.json - truegeneric 3, 4, 5 울타리 → oak/generic 추가
29. mountain/mplain-extended.json - 나무 없음 → oak/generic 추가

### mushroom (2개)
30. mushroom/warped-forest-extended.json - 버섯→돌 edit 제거
31. mushroom/warped-forest.json - 버섯→돌 edit 제거

### savanna (8개)
32. savanna/shore/beach.json - obelisk, literalgarbage (전리품)
33. savanna/acacia-denmyre.json - denmyre 전체 → acacia/vexed + savannaF
34. savanna/forest.json - dadwood 1,3,4,5,6 → acacia/vexed + savannaF
35. savanna/cliff-extended.json - savannaD 전체, savannaF(F1,F2,F3,F17), savannaS(S4,S5,S8,s12)
36. savanna/cliff.json - savannaD 전체, savannaF(F1,F2,F3,F17), savannaS(S4,S5,S8,s12)
37. savanna/plateau.json - savannaD 전체, savannaF(F1,F2,F3,F17), savannaS(S4,S5,S8,s12)
38. savanna/savanna.json - savannaD 전체, savannaF(F1,F2,F3,F17), savannaS(S4,S5,S8,s12)

### swamp (9개)
39. swamp/creaks.json - mroofed1-12 → oak/croak1-5 + pollup1-3
40. swamp/denmyre.json - denmyre1-7 → acacia/vexed1-3 + savannaF4-6
41. swamp/roofed-forest-extended.json - mroofed1-12 → oak/croak1-5 + pollup1-3
42. swamp/roofed-forest.json - mroofed1-12 → oak/croak1-5 + pollup1-3
43. swamp/roofed-wayward-extended.json - mroofed1-12 → oak/croak1-5 + pollup1-3
44. swamp/roofed-wayward.json - mroofed1-12 → oak/croak1-5 + pollup1-3
45. swamp/swamp-forest.json - dotree1-10 → pollup1-8
46. swamp/swamp-puddle.json - willow/t1-8 → darkoak/willowgeneric + generic, mangrove/t → mangrove
47. swamp/swamp-mangrove-lake.json - mangrove5-20 제거

### temperate (13개)
48. temperate/sea/ocean.json - obelisk (전리품)
49. temperate/sea/river.json - obelisk (전리품)
50. temperate/shore/oak-beach.json - obelisk, literalgarbage (전리품)
51. temperate/shore/beach.json - bincluster1 (배럴 전리품)
52. temperate/birch-denmyre.json - denmyre 1~7 (울타리)
53. temperate/oak-denmyre.json - denmyre1-7 → acacia/vexed1-3 + savannaF4-6
54. temperate/combo-forest-extended.json - toak 1,2,3,4 (울타리) + literalgarbage
55. temperate/combo-forest.json - toak 1,2,3,4 (울타리) + literalgarbage
56. temperate/cherry-blossom-forest.json - literalgarbage
57. temperate/flower-forest-extended.json - literalgarbage
58. temperate/flower-forest.json - literalgarbage
59. temperate/fancyplains.json - oakFancy → shoakgeneric 교체 (stairs/slab 문제)
60. temperate/croak.json - croak18 (moving_piston)

### terralost (1개)
61. terralost/ - WIP 폴더 확인, 나머지 choppable 나무만 사용

---

## 진행 상황

- **완료**: 63개 파일 (unchoppable 나무 교체)
- **이전 세션**: 57개 파일 (구조물/동굴/광석 제거)
- **전체**: ~325개 바이옴 파일
- **진행률**: ~37% (120/325)

## 다음 작업
- Git 커밋 (24개 파일 수정)
- 나머지 바이옴 확인 (hot, ocean 등)
- 플러그인 config 한국어 번역
