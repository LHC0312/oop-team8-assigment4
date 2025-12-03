# Block Coding Backend

Spring Boot 4.0.0 기반의 블록 코딩 백엔드입니다. Java 17, Gradle을 사용하며 WebSocket(STOMP)으로 실행 로그/디버그 메시지를 스트리밍합니다.

## 기술 스택
- Spring Boot 4.0.0 (webmvc, websocket, data-jpa)
- Java 17, Gradle
- DB: MySQL(dev, `application-dev.yml`) / PostgreSQL 드라이버 포함
- Swagger(OpenAPI): `/swagger-ui.html`
- SockJS + STOMP WebSocket: 엔드포인트 `/ws`, 구독 채널 `/topic/execution/{sessionId}`

## 핵심 설계 포인트
- **실행 블록 11종**: START, IF, FOR, WHILE, PRINT, ADD, SUBTRACT, MULTIPLY, DIVIDE, VAR_DECLARE, VAR_ASSIGN
- **값 블록(Value Block) 트리**: 리터럴/변수/단항/이항 모두 블록이며 `blockId`를 가진다. 실행 블록 안에 중첩 객체로 들어간다(문자열 표현식 사용 없음).
- **타입 강제**: 변수 선언 시 `variableType` 필수(`number|string|boolean`). 산술/비교는 숫자 전용, 문자열+숫자 조합은 즉시 오류.
- **변수 무결성**: 변수는 별도 엔티티(`variables` 테이블)로 관리되며 프로젝트 내 이름은 유니크. 블록/값 블록은 `variableId`로 참조(이름은 보조용)하며, 산술 결과도 `resultVariableId`를 필수로 지정.
- **실행 제어**: 디버그/트레이스 실행, 디버그 스텝 실행, 강제 중지, 실행 중 변수 조회.
- **분기 보정**: 분기 내부 마지막 블록의 `nextBlockId`가 비어 있으면 WHILE/FOR는 부모로, IF/ELSE는 부모의 `nextBlockId`로 연결.
- **시드 데이터**: 애플리케이션 기동 시 데이터가 비어 있으면 4개 프로젝트 자동 생성.

## 실제 프로젝트 구조
```
src/main/java/team8
├── Application.java
├── config/                # WebSocket, Swagger, CORS, 데이터 시드
│   ├── DataInitializer.java
│   ├── SwaggerConfig.java
│   ├── WebConfig.java
│   └── WebSocketConfig.java
├── controller/            # REST 컨트롤러
│   ├── BlockController.java
│   ├── ExecutionController.java
│   └── ProjectController.java
├── dto/                   # DTO 요청/응답
│   ├── BlockCreateRequest.java
│   ├── BlockDto.java
│   ├── ExpressionCreateRequest.java
│   ├── ExpressionDto.java
│   ├── ProjectCreateRequest.java
│   ├── ProjectDto.java
│   └── ValueDto.java
├── execution/             # 실행 엔진
│   ├── ExecutionContext.java
│   ├── ExecutionResult.java
│   └── OutputMessage.java
├── model/
│   ├── Block.java
│   ├── Project.java
│   ├── arithmetic/  (Add, Subtract, Multiply, Divide, ArithmeticBlock)
│   ├── control/     (ControlBlock, IfBlock, ForBlock, WhileBlock)
│   ├── expression/  (ExpressionBlock + Literal/Variable/Unary/Binary)  ※ 값 블록
│   ├── output/      (OutputBlock, PrintBlock)
│   ├── start/       (StartBlock)
│   └── variable/    (VariableBlock, VariableDeclareBlock, VariableAssignBlock)
├── repository/           # Block/Project/Expression JPA 리포지토리
├── service/
│   ├── BlockExecutionService.java
│   ├── BlockService.java
│   ├── BlockServiceHelper.java
│   └── ProjectService.java
└── resources/
    ├── application.yml (active profile 설정)
    ├── application-dev.yml (MySQL 설정)
    ├── application-prod.yml
    └── static/index.html (간단한 테스트 클라이언트)
```

## 값 블록(ValueDto) 구조
- 공통 필드: `blockId`, `valueType`(LITERAL|VARIABLE|UNARY|BINARY)
- LITERAL: `data` (number/string/boolean) → DB에는 literalType과 함께 저장
- VARIABLE: `variableName`
- UNARY: `operator`, `operand`
- BINARY: `operator`, `left`, `right`

### 블록 DTO 구조 (요약)
- 공통: `blockType`, `positionX`, `positionY`, `order`, `nextBlockId`
- IF/WHILE/FOR: `condition`(ValueDto), `trueBranchId`, `falseBranchId`(IF), FOR는 `init`, `increment`
- PRINT: `message`(ValueDto)
- 산술(ADD/SUBTRACT/MULTIPLY/DIVIDE): `operand1`, `operand2`(ValueDto), `resultVariable`
- 변수: VAR_DECLARE(`variableName`, `variableType`, `initial` ValueDto), VAR_ASSIGN(`variableName`, `value` ValueDto)

### 응답 예시 (Loop)
```json
{
  "blocks": [
    {"id":1,"blockType":"START","nextBlockId":2},
    {"id":2,"blockType":"VAR_DECLARE","variableName":"i","variableType":"number",
     "initial":{"blockId":11,"valueType":"LITERAL","data":0},
     "nextBlockId":3},
    {"id":3,"blockType":"WHILE",
     "condition":{"blockId":12,"valueType":"BINARY","operator":"<",
       "left":{"blockId":13,"valueType":"VARIABLE","variableName":"i"},
       "right":{"blockId":14,"valueType":"LITERAL","data":5}},
     "trueBranchId":4},
    {"id":4,"blockType":"PRINT",
     "message":{"blockId":15,"valueType":"VARIABLE","variableName":"i"},
     "nextBlockId":5},
    {"id":5,"blockType":"ADD",
     "operand1":{"blockId":16,"valueType":"VARIABLE","variableName":"i"},
     "operand2":{"blockId":17,"valueType":"LITERAL","data":1},
     "resultVariable":"i",
     "nextBlockId":3}
  ]
}
```

## API 개요
- 프로젝트: `GET /api/projects`, `GET /api/projects/{id}`, `GET /api/projects/search?keyword=...`, `POST /api/projects`, `PUT /api/projects/{id}`, `DELETE /api/projects/{id}`
- 블록: `GET /api/blocks/project/{projectId}`, `GET /api/blocks/{id}`, `POST /api/blocks/project/{projectId}`, `PUT /api/blocks/{id}`, `DELETE /api/blocks/{id}`
  - 블록 생성/수정 시 문자열 표현식 없이 ValueDto 트리를 전달해야 함.
- 실행: `POST /api/execution/projects/{projectId}/run?debug=false&trace=false`,
  `POST /api/execution/projects/{projectId}/run/normal`,
  `POST /api/execution/projects/{projectId}/run/trace`,
  `POST /api/execution/sessions/{sessionId}/step`,
  `POST /api/execution/sessions/{sessionId}/stop`,
  `GET /api/execution/sessions/{sessionId}/variables`
  - WebSocket 구독: `/topic/execution/{sessionId}` (`DEBUG_WAIT`, `TRACE`, `PRINT` 등 메시지 수신)

## 자동 생성 샘플 데이터 (DataInitializer)
데이터가 비어 있을 때만 아래 4개 프로젝트가 생성됩니다. 모든 조건/연산/리터럴은 Value 블록 트리로 넣습니다.
1) Hello World
   - START → PRINT("Hello World!")
2) Conditional Test
   - START → VAR_DECLARE(x: number = 10) → IF(x > 5)
   - true: PRINT("x is greater than 5") / false: PRINT("x is not greater than 5")
3) Loop Test
   - START → VAR_DECLARE(i: number = 0) → WHILE(i < 5)
   - WHILE true 분기: PRINT(i) → ADD(i = i + 1) → (다시 WHILE)
4) Arithmetic Operations
   - START → VAR_DECLARE(a=10) → VAR_DECLARE(b=5) →
     ADD(a+b → result1) → PRINT("Addition: result1") →
     SUBTRACT(a-b → result2) → PRINT("Subtraction: result2") →
     MULTIPLY(a*b → result3) → PRINT("Multiplication: result3") →
     DIVIDE(a/b → result4) → PRINT("Division: result4")

## 타입/연산 규칙
- 변수 선언 시 `variableType` 필수(`number|string|boolean`), 산술 블록은 `number` 타입만 허용.
- `+`는 숫자+숫자, 문자열+문자열만 허용. 그 외 조합은 400/실행 오류.
- 비교 연산은 숫자 전용, 논리 연산은 불리언 전용.
- Division/Modulo 0은 오류.

## 실행 방법
```bash
# 개발 프로필(dev)에서 MySQL(blockcodingdb, root/1234) 사용
./gradlew bootRun

# 빌드 후 실행
./gradlew build
java -jar build/libs/oop-team8-assigment4-0.0.1-SNAPSHOT.jar
```
- DB 커넥션은 `src/main/resources/application-dev.yml`에서 수정하세요.
- Swagger UI: http://localhost:8080/swagger-ui.html
- 정적 테스트 페이지: http://localhost:8080/index.html

## 데이터 모델 메모
- 블록은 JPA JOINED 전략을 사용해 `blocks` + 타입별 테이블에 저장됩니다.
- 값 블록(리터럴/변수/단항/이항)은 `expression_blocks` 계층 테이블에 저장되며 프로젝트와 연관됩니다.
