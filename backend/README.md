# Block Coding Backend

Spring Boot 기반 블록 코딩 백엔드입니다. Scratch와 유사한 비주얼 프로그래밍 환경을 위한 REST API와 WebSocket 실시간 실행을 제공합니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Framework | Spring Boot 4.0.0 |
| Language | Java 17 |
| Build | Gradle |
| Database | MySQL (dev) / PostgreSQL |
| API 문서 | Swagger OpenAPI (`/swagger-ui.html`) |
| 실시간 통신 | SockJS + STOMP WebSocket |

## 블록 타입 (7종)

| 블록 타입 | 설명 | 주요 필드 |
|-----------|------|-----------|
| **START** | 프로그램 시작점 | `nextBlockId` |
| **IF** | 조건 분기 (if / if-else) | `condition`, `trueBranchId`, `falseBranchId` |
| **FOR** | for 반복문 | `condition`, `init`, `increment`, `trueBranchId` |
| **WHILE** | while 반복문 | `condition`, `trueBranchId` |
| **PRINT** | 출력 | `message` |
| **VAR_DECLARE** | 변수 선언 | `variableName`, `variableType`, `initial` |
| **VAR_ASSIGN** | 변수 할당 | `variableId`, `value` |

> **참고**: 산술 연산(+, -, *, /)은 별도 블록이 아닌 **표현식 블록(ValueDto)**의 BINARY 타입으로 처리됩니다.
> 예: `VAR_ASSIGN` 블록에 `value: { valueType: "BINARY", operator: "+", left: {...}, right: {...} }` 형태로 전달.

### IF vs IF-ELSE 구분

```json
// IF만 (else 분기 없음)
{
  "blockType": "IF",
  "condition": {...},
  "trueBranchId": 4,
  "falseBranchId": null,  // null 또는 생략
  "nextBlockId": 7
}

// IF-ELSE (양쪽 분기 있음)  
{
  "blockType": "IF",
  "condition": {...},
  "trueBranchId": 4,
  "falseBranchId": 5,  // else 분기 블록 ID
  "nextBlockId": 7
}
```

## 표현식 (ValueDto)

모든 값/조건/연산은 `ValueDto` 트리 구조로 표현됩니다.

| valueType | 설명 | 예시 |
|-----------|------|------|
| **LITERAL** | 리터럴 값 | `{ "valueType": "LITERAL", "data": 10 }` |
| **VARIABLE** | 변수 참조 | `{ "valueType": "VARIABLE", "variableId": 5 }` |
| **UNARY** | 단항 연산 | `{ "valueType": "UNARY", "operator": "-", "operand": {...} }` |
| **BINARY** | 이항 연산 | `{ "valueType": "BINARY", "operator": "+", "left": {...}, "right": {...} }` |

**지원 연산자:**
- 산술: `+`, `-`, `*`, `/`, `%`
- 비교: `==`, `!=`, `>`, `<`, `>=`, `<=`
- 논리: `&&`, `||`, `!`

## API 엔드포인트

### 프로젝트
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/projects` | 전체 프로젝트 조회 |
| GET | `/api/projects/{id}` | 프로젝트 상세 조회 |
| POST | `/api/projects` | 프로젝트 생성 |
| PUT | `/api/projects/{id}` | 프로젝트 수정 |
| DELETE | `/api/projects/{id}` | 프로젝트 삭제 |

### 블록
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/blocks/project/{projectId}` | 프로젝트의 모든 블록 조회 |
| GET | `/api/blocks/{id}` | 블록 상세 조회 |
| POST | `/api/blocks/project/{projectId}` | 블록 생성 |
| PUT | `/api/blocks/{id}` | 블록 수정 |
| DELETE | `/api/blocks/{id}` | 블록 삭제 (연결된 블록/표현식 재귀 삭제) |
| POST | `/api/blocks/project/{projectId}/connect` | 블록 연결 일괄 수정 |

### 표현식
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/expressions/project/{projectId}` | 프로젝트의 모든 표현식 조회 |
| POST | `/api/expressions/project/{projectId}` | 표현식 생성 |
| DELETE | `/api/expressions/{id}` | 표현식 삭제 |
| POST | `/api/expressions/project/{projectId}/connect` | 표현식 연결 일괄 수정 |

### 실행
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/execution/projects/{projectId}/run?debug=false&trace=false` | 실행 |
| POST | `/api/execution/sessions/{sessionId}/step` | 디버그 스텝 실행 |
| POST | `/api/execution/sessions/{sessionId}/stop` | 실행 중지 |
| GET | `/api/execution/sessions/{sessionId}/variables` | 실행 중 변수 조회 |

**WebSocket 구독**: `/topic/execution/{sessionId}`

## 실행 방법

```bash
# 개발 모드 (MySQL 필요: blockcodingdb, root/1234)
./gradlew bootRun

# 빌드 후 실행
./gradlew build
java -jar build/libs/oop-team8-assigment4-0.0.1-SNAPSHOT.jar
```

**접속 URL:**
- Swagger UI: http://localhost:8080/swagger-ui.html
- 테스트 페이지: http://localhost:8080/index.html

## 샘플 데이터

DB가 비어 있을 때 4개 프로젝트가 자동 생성됩니다:

1. **Hello World**: START → PRINT("Hello World!")
2. **Conditional Test**: 변수 선언 → IF 조건 분기 → 각 분기 PRINT
3. **Loop Test**: WHILE 반복문으로 0~4 출력
4. **Arithmetic Operations**: 표현식을 활용한 4칙연산 (VAR_ASSIGN + BINARY 표현식)

## 테스트

```bash
./gradlew test
```

## 프로젝트 구조

```
src/main/java/team8/
├── Application.java
├── config/          # WebSocket, Swagger, CORS, 시드 데이터
├── controller/      # REST 컨트롤러 (Block, Execution, Project)
├── dto/             # 요청/응답 DTO
├── execution/       # 실행 엔진 (ExecutionContext)
├── model/           # 엔티티
│   ├── control/     # IF, FOR, WHILE
│   ├── expression/  # 표현식 (Literal, Variable, Unary, Binary)
│   ├── output/      # PRINT
│   ├── start/       # START
│   └── variable/    # VAR_DECLARE, VAR_ASSIGN
├── repository/      # JPA 리포지토리
└── service/         # 비즈니스 로직
```

---

> 📚 **프론트엔드 연동 상세 가이드**: [docs/frontend-guide.md](./docs/frontend-guide.md)
