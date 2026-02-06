# ItemsAdder 추가 예정 목록

## 특수 목재 (벌목 시스템)

### 호두나무 (Walnut)
- **용도**: 특수 원목, 고가 거래용
- **필요 파일**:
  - 텍스처: `walnut_log.png`, `walnut_planks.png`
  - 설정: `contents/walnut/data/items_packs/walnut.yml`
- **드롭**: 벌목 시 원목 획득
- **판매가**: 일반 원목보다 3~5배 고가

### 흑단 (Ebony)
- **용도**: 특수 원목, 고가 거래용
- **필요 파일**:
  - 텍스처: `ebony_log.png`, `ebony_planks.png`
  - 설정: `contents/ebony/data/items_packs/ebony.yml`
- **드롭**: 벌목 시 원목 획득
- **판매가**: 일반 원목보다 3~5배 고가

### 마호가니 (Mahogany)
- **용도**: 특수 원목, 고가 거래용
- **필요 파일**:
  - 텍스처: `mahogany_log.png`, `mahogany_planks.png`
  - 설정: `contents/mahogany/data/items_packs/mahogany.yml`
- **드롭**: 벌목 시 원목 획득
- **판매가**: 일반 원목보다 3~5배 고가

## 구현 계획

1. **텍스처 제작**
   - 기존 바닐라 목재 텍스처 변형 또는 새로 제작
   - 각 목재당 2개: 원목(log), 판자(planks)

2. **ItemsAdder 설정**
   - 아이템 정의 (yml)
   - 블록 정의 (커스텀 블록 필요 시)
   - 월드 생성 연동 (Iris 또는 수동 배치)

3. **경제 시스템 연동**
   - ShopGUI+ 판매가 설정
   - Jobs Reborn 벌목 보너스

4. **월드 배치**
   - wild(자원 월드)에 특수 나무 스폰
   - 희귀도 조절

## 참고 문서
- economy-guide.md: 1-3. 벌목 (Foraging)
