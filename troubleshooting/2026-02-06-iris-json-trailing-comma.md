# 트러블슈팅 로그

## 2026-02-06: Iris 월드 생성 실패

### 문제 상황

바이옴 파일 57개 수정 후 서버 시작 시 크래시 발생

```
java.lang.RuntimeException: Iris failed to replace the levelStem
Caused by: java.lang.IllegalStateException: Missing element ResourceKey[minecraft:dimension_type / iris:aiamgajach8.]
```

### 증상

1. **Iris Unstable Mode 경고**
   ```
   [Iris]: Iris is running in Unstable Mode
   [Iris]: - Server won't boot
   [Iris]: - Data Loss
   [Iris]: - Unexpected behavior
   ```

2. **바이옴 등록 실패**
   ```
   [Iris]: The Biome overworld:meadow is not registered on the server
   [Iris]: The Biome overworld:longtree_forest is not registered on the server
   [Iris]: The Biome overworld:frozen_pine_plains is not registered on the server
   ```

3. **Dimension Type 등록 실패**
   ```
   [Iris]: The Dimension Type for plugins/Iris/packs/overworld/dimensions/overworld.json is not registered on the server
   [Iris]: The Pack overworld is INCAPABLE of generating custom biomes
   ```

4. **서버 크래시**
   - crash-reports/crash-2026-02-06_00.03.51-server.txt 생성
   - 서버가 월드를 로드하지 못하고 종료

### 원인 분석

JSON 파일 수정 중 **trailing comma** 남김

**문제 파일:**
1. `plugins/Iris/packs/overworld/biomes/temperate/sea/ocean-deep.json:88`
   ```json
   "place": [
       "clutter/substat5"
   ]
   },  // ← 이 콤마가 문제
   ],
   ```

2. `plugins/Iris/packs/overworld/biomes/frozen/sea/frozen-river-ice.json:127`
   ```json
   "translate": {
       "x": 0,
       "y": 0,
       "z": 0
   }
   },  // ← 이 콤마가 문제
   ],
   ```

3. `plugins/Iris/packs/overworld/biomes/frozen/sea/ocean.json:126`
   ```json
   "place": [
       "clutter/icespec5"
   ]
   },  // ← 이 콤마가 문제
   ],
   ```

### 원인 상세

- Python의 `json.tool`은 trailing comma를 허용하지만, Iris는 엄격한 JSON 파싱 사용
- 바이옴 파일의 JSON 구문 오류로 Iris가 바이옴을 로드하지 못함
- 바이옴 로드 실패 → Dimension Type 등록 실패 → 서버 크래시

### 해결 방법

**1단계: 문제 진단**
```bash
# JSON 유효성 검사 (Python은 통과)
find plugins/Iris/packs/overworld/biomes -name "*.json" -exec python3 -m json.tool {} \; > /dev/null

# 에러 로그 확인
grep -i "error\|exception\|fail" crash-reports/crash-2026-02-06_00.03.51-server.txt
```

**2단계: Trailing Comma 제거**

ocean-deep.json:
```diff
             "clutter/substat5"
         ]
-    },
+    }
 ],
```

frozen-river-ice.json:
```diff
             "z": 0
         }
-    },
+    }
 ],
```

frozen-ocean.json:
```diff
             "clutter/icespec5"
         ]
-    },
+    }
 ],
```

**3단계: 서버 재시작**
```bash
./start.sh
```

### 결과

✅ 서버 정상 시작
```
[00:07:19 INFO]: Done preparing level "world" (4.731s)
```

✅ Iris 정상 로드
- Unstable Mode 경고 없음
- 바이옴 등록 성공
- world/iris/ 폴더 생성 확인
- world/mantle/ 폴더 생성 확인

### 교훈

1. **JSON 편집 시 주의사항**
   - 배열/객체 마지막 요소 뒤에 콤마 금지
   - Edit 도구 사용 시 주변 구조 확인 필수

2. **검증 도구의 한계**
   - Python json.tool은 lenient parsing 사용
   - 실제 애플리케이션은 더 엄격할 수 있음
   - 가능하면 실제 로드 테스트 필요

3. **에러 추적 방법**
   - 크래시 리포트 우선 확인
   - 플러그인 로그에서 "unstable", "fail", "error" 키워드 검색
   - 최근 수정한 파일부터 검증

### 재발 방지

- JSON 파일 수정 후 반드시 서버 테스트
- Edit 작업 시 전체 블록 구조 확인
- trailing comma 제거 자동화 스크립트 고려
