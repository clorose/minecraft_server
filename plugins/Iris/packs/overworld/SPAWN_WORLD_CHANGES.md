# 스폰 월드 제거 항목

## 완료된 작업 (dimensions/overworld.json)
1. ✅ **광석 (Deposits)** - `deposits` 배열 비움
2. ✅ **동굴 (Caves)** - `carving.caves` 배열 비움
3. ✅ **구조물 (Structures)** - `jigsawStructures` 배열 비움, `stronghold` 관련 설정 제거

## 남은 작업
4. ⬜ **몹 스포너** - spawners 비활성화
5. ⬜ **바이옴별 구조물** - 각 바이옴 JSON에서 `jigsawStructures` 제거
6. ⬜ **바이옴별 오브젝트** - 각 바이옴 JSON에서 `objects` 제거 (커스텀 큰 나무, 캠프 등)
7. ⬜ **해양 식물** - 해양 바이옴에서 kelp, seagrass 제거

## 유지 항목
- 바닐라 장식 (꽃, 풀, 일반 나무)
- 지형 생성 (바이옴, terrain)
- bedrock, deepslate 레이어

## 수정된 파일
- `dimensions/overworld.json` - 메인 설정
