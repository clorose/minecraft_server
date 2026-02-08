# Claude Code 프로젝트 정보

## compact.md 파일 용도

`docs/compact.md`는 Claude Code 세션의 컨텍스트가 압축(compaction)되기 전에 작업 진행 상황을 기록하는 파일입니다.

### 왜 필요한가?
- Claude Code는 대화가 길어지면 이전 컨텍스트를 요약(compact)합니다
- compact 시점에 진행 중이던 작업 내용이 손실될 수 있습니다
- 이 파일에 현재 작업 상태를 기록해두면 compact 후에도 이어서 작업 가능합니다

### 기록해야 할 내용
- 현재 진행 중인 작업
- 완료된 작업 목록
- 수정한 파일들
- 남은 할 일
- 서버/시스템 상태
- 에러/문제 해결 이력

### 사용 방법
1. 긴 작업 중간중간 docs/compact.md 업데이트
2. compact 경고 메시지 나타나면 즉시 현재 상태 기록
3. 새 세션 시작 시 docs/compact.md 먼저 읽고 컨텍스트 파악

## 프로젝트 개요

한국어 마인크래프트 경제 서버 구축 프로젝트입니다.

### 경제 구조
- 수입/지출 경로: **ShopGUI+** 상점 판매/구매만 (Jobs 제거됨)
- 커스텀 아이템: **Nexo** (ItemsAdder에서 교체)
- 데이터 수집: **clorose-tracker** (CSV로 거래/잔액 기록)

### 주요 작업 영역
1. **Iris 월드 생성** - 보류 중 (docs/iris-todo.md 참고)
2. **플러그인 한국어 번역** - 21개 플러그인 현지화
3. **커스텀 플러그인 개발** - 경마, 장비 시스템, 사망 패널티, 경제 추적 등
4. **wolf-of-minestreet** - 주식 거래소 (이번 시즌 보류, 다음 시즌용. docs/wom/ 참고)

### 중요 주의사항
- **파일 삭제 시 반드시 사전 확인** (내용 확인 후 유저에게 삭제 여부 물어볼 것. 임의 삭제 절대 금지)
- **시킨 것만 할 것** (유저가 요청하지 않은 작업을 임의로 수행하지 말 것)
- Iris 파일 수정 시 **반드시 직접 하나씩** 수정 (Python 등 자동화 스크립트 사용 금지 - 파일 손상 전적 있음)
- 서버 실행 전 기존 서버 프로세스 확인 필수
- RCON 사용 가능 (port: 25575, password: minecraft123)

### 파일 구조
```
/Users/gohan/20_Dev/minecraft_server/
├── CLAUDE.md           # 이 파일 (Claude Code 가이드)
├── docs/
│   ├── compact.md      # 작업 진행 상황 기록
│   ├── iris-todo.md    # Iris 관련 미해결 작업
│   ├── tree.md         # Iris 나무 정리 (찹트리 가능 여부)
│   ├── economy-guide.md
│   └── wom/            # Wolf of Minestreet 참고자료 (다음 시즌용)
│       ├── system.md
│       ├── coin-design.md
│       └── worldbuilding.md
├── plugins/            # 서버 플러그인
├── clorose/            # 커스텀 플러그인 소스 코드
└── start.sh            # 서버 실행 스크립트
```
