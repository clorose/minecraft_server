# Iris 미해결 작업

## 현재 상태: 보류

Iris 플러그인이 삭제된 상태. datapack 생성 시 JSON 버그가 있어 서버 크래시 발생.

## 문제점

### 1. 바이옴 파일 JSON 문법 에러
- **에러**: `Missing element ResourceKey[minecraft:dimension_type / iris:aiamgajach8.]`
- Claude가 바이옴 파일 수정 시 JSON 문법 에러를 만들어냄 (trailing comma, missing comma 등)
- 이로 인해 Iris가 datapack 생성 시 dimension_type 등록 실패
- 기존 월드가 있으면 작동할 수도 있음 (미확인)

### 2. 바닐라 바이옴 팩 부재
- Iris는 커스텀 바이옴만 지원 (overworld, earthworld 등)
- 바닐라 바이옴 + 동굴/광석 제거만 하는 팩은 없음
- Iris 자체가 커스텀 바이옴을 만드는 것이 목적

### 3. 나무 제거 작업 미완료
- 63개 파일 완료 / ~325개 전체 → 37% 진행
- 나머지 200개+ 파일에 unchoppable 나무 남아있음
- 상세: docs/tree.md 참고

## 이전 트러블슈팅

### 2026-02-06: JSON trailing comma
- 파일: troubleshooting/2026-02-06-iris-json-trailing-comma.md
- trailing comma 제거로 해결

### 2026-02-07: dimension_type 등록 실패
- .bak 파일 제거로 즉시 크래시는 해결
- 하지만 Iris Unstable Mode 진입 지속
- "Custom Biomes: 0" - Iris가 custom biomes 인식 못함
- customDerivitives 문제와 복합적

## Iris 바이옴 정리 작업 이력

### 구조물/동굴/광석 제거 (57개 파일 완료)
- jigsawStructures: 모든 구조물 제거
- carving: 동굴 생성 제거
- deposits: 광석 생성 제거
- objects 내 대형 나무/구조물 제거
- decorators: kelp, seagrass 제거
- layers 내 광석: coal_ore, iron_ore 제거

### 나무 제거 (63개 파일 완료)
- 상세: docs/tree.md 참고

## 파일 경로
```
plugins/Iris/packs/overworld/    (현재 삭제됨, git 15b9386에서 복원 가능)
iris-build/build/libs/Iris-3.9.1-1.20.1-1.21.11.jar    (빌드된 jar)
```
