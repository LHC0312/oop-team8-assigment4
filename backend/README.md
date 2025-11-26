# Block Coding Application

스크래치/엔트리와 같은 블록 코딩 애플리케이션의 백엔드 시스템입니다. Spring Boot 기반으로 구축되었으며, MVC 패턴과 객체지향 설계를 적용했습니다.

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [블록 타입](#블록-타입)
- [프로젝트 구조](#프로젝트-구조)
- [API 문서](#api-문서)
- [실행 방법](#실행-방법)
- [사용 예시](#사용-예시)

## 프로젝트 개요

이 프로젝트는 블록 기반 프로그래밍을 지원하는 시각적 코딩 플랫폼의 백엔드입니다. 사용자는 블록을 연결하여 프로그램을 작성하고, 웹소켓을 통해 실시간으로 실행 결과를 확인할 수 있습니다.

### 주요 기능

- **블록 기반 프로그래밍**: 14가지 타입의 블록으로 프로그램 구성
- **실시간 실행**: WebSocket을 통한 실시간 실행 결과 스트리밍
- **단방향 연결 그래프**: 블록 간 연결을 통한 프로그램 흐름 제어
- **제어 구조 분기**: IF, FOR, WHILE 블록의 조건부 분기 지원
- **변수 및 연산**: 변수 선언/할당, 사칙연산 지원
- **REST API**: 프로젝트 및 블록 CRUD 작업 지원

## 기술 스택

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 17
- **Database**: MySQL / H2 (개발용)
- **ORM**: JPA/Hibernate
- **WebSocket**: STOMP over SockJS
- **API Documentation**: Swagger/OpenAPI 3.0
- **Build Tool**: Gradle

## 시스템 아키텍처

### 블록 상속 구조

```
Block (추상 클래스)
├── StartBlock (시작)
├── ControlBlock (제어 - 추상 클래스)
│   ├── IfBlock (조건문)
│   ├── ForBlock (반복문)
│   └── WhileBlock (반복문)
├── PrintBlock (출력)
├── AlertBlock (알림)
├── DrawBlock (그리기)
├── AddBlock (덧셈)
├── SubtractBlock (뺄셈)
├── MultiplyBlock (곱셈)
├── DivideBlock (나눗셈)
├── VariableDeclareBlock (변수 선언)
└── VariableAssignBlock (변수 할당)
```

### 연결 그래프 구조

각 블록은 다음 블록으로의 연결을 나타내는 필드를 가집니다:
- **일반 블록**: `nextBlockId` - 다음에 실행할 블록
- **제어 블록**: `trueBranchId`, `falseBranchId` - 조건에 따른 분기

## 블록 타입

### 1. 시작 블록 (Start)

| 블록 | 설명 | 필드 |
|------|------|------|
| StartBlock | 프로그램 시작점 | nextBlockId |

### 2. 제어 블록 (Control)

| 블록 | 설명 | 주요 필드 |
|------|------|-----------|
| IfBlock | 조건문 | conditionExpression, trueBranchId, falseBranchId |
| ForBlock | 반복문 | conditionExpression, initExpression, incrementExpression, trueBranchId |
| WhileBlock | 반복문 | conditionExpression, trueBranchId |

**조건식 예시**: `x > 10`, `y == 5`, `a < b`

### 3. 출력 블록 (Output)

| 블록 | 설명 | 주요 필드 |
|------|------|-----------|
| PrintBlock | 콘솔 출력 | message |
| AlertBlock | 알림 메시지 | message |
| DrawBlock | 도형 그리기 | shape, color, size |

### 4. 산술 블록 (Arithmetic)

| 블록 | 설명 | 주요 필드 |
|------|------|-----------|
| AddBlock | 덧셈 | operand1, operand2, resultVariable |
| SubtractBlock | 뺄셈 | operand1, operand2, resultVariable |
| MultiplyBlock | 곱셈 | operand1, operand2, resultVariable |
| DivideBlock | 나눗셈 | operand1, operand2, resultVariable |

### 5. 변수 블록 (Variable)

| 블록 | 설명 | 주요 필드 |
|------|------|-----------|
| VariableDeclareBlock | 변수 선언 | variableName, variableType, initialValue |
| VariableAssignBlock | 변수 할당 | variableName, valueExpression |

## 프로젝트 구조

```
backend/
├── src/main/java/team8/
│   ├── model/                    # 도메인 모델 (Entity)
│   │   ├── Block.java           # 블록 추상 클래스
│   │   ├── Project.java         # 프로젝트 엔티티
│   │   ├── start/               # 시작 블록
│   │   │   └── StartBlock.java
│   │   ├── control/             # 제어 블록
│   │   │   ├── ControlBlock.java
│   │   │   ├── IfBlock.java
│   │   │   ├── ForBlock.java
│   │   │   └── WhileBlock.java
│   │   ├── output/              # 출력 블록
│   │   │   ├── PrintBlock.java
│   │   │   ├── AlertBlock.java
│   │   │   └── DrawBlock.java
│   │   ├── arithmetic/          # 산술 블록
│   │   │   ├── AddBlock.java
│   │   │   ├── SubtractBlock.java
│   │   │   ├── MultiplyBlock.java
│   │   │   └── DivideBlock.java
│   │   └── variable/            # 변수 블록
│   │       ├── VariableDeclareBlock.java
│   │       └── VariableAssignBlock.java
│   ├── controller/              # REST API 컨트롤러
│   │   ├── ProjectController.java
│   │   ├── BlockController.java
│   │   └── ExecutionController.java
│   ├── service/                 # 비즈니스 로직
│   │   ├── ProjectService.java
│   │   ├── BlockService.java
│   │   └── BlockExecutionService.java
│   ├── repository/              # 데이터 액세스
│   │   ├── ProjectRepository.java
│   │   └── BlockRepository.java
│   ├── dto/                     # 데이터 전송 객체
│   │   ├── ProjectDto.java
│   │   ├── ProjectCreateRequest.java
│   │   ├── BlockDto.java
│   │   └── BlockCreateRequest.java
│   ├── execution/               # 실행 엔진
│   │   ├── ExecutionContext.java
│   │   ├── ExecutionResult.java
│   │   └── OutputMessage.java
│   └── config/                  # 설정
│       ├── SwaggerConfig.java
│       └── WebSocketConfig.java
└── src/main/resources/
    ├── application.yml          # 애플리케이션 설정
    └── static/
        └── index.html          # 테스트 클라이언트
```

## API 문서

### 프로젝트 API

#### 프로젝트 목록 조회
```http
GET /api/projects
```

#### 프로젝트 생성
```http
POST /api/projects
Content-Type: application/json

{
  "name": "My First Program",
  "description": "Hello World program"
}
```

#### 프로젝트 조회
```http
GET /api/projects/{id}
```

#### 프로젝트 수정
```http
PUT /api/projects/{id}
Content-Type: application/json

{
  "name": "Updated Program",
  "description": "Updated description"
}
```

#### 프로젝트 삭제
```http
DELETE /api/projects/{id}
```

### 블록 API

#### 블록 생성
```http
POST /api/blocks/project/{projectId}
Content-Type: application/json

{
  "blockType": "PRINT",
  "positionX": 100,
  "positionY": 200,
  "order": 1,
  "message": "Hello World",
  "nextBlockId": null
}
```

#### 프로젝트의 블록 목록 조회
```http
GET /api/blocks/project/{projectId}
```

#### 블록 수정
```http
PUT /api/blocks/{id}
Content-Type: application/json

{
  "blockType": "PRINT",
  "message": "Updated message",
  "nextBlockId": 2
}
```

#### 블록 삭제
```http
DELETE /api/blocks/{id}
```

### 실행 API

#### 프로젝트 실행
```http
POST /api/execution/projects/{projectId}/run
```

**응답**:
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Execution started. Subscribe to /topic/execution/{sessionId} for real-time output"
}
```

## 실행 방법

### 1. 데이터베이스 설정

**MySQL 사용 시**:
```bash
# MySQL 데이터베이스 생성
CREATE DATABASE blockcodingdb;
```

`application.yml` 설정 확인:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blockcodingdb
    username: root
    password: your_password
```

### 2. 애플리케이션 실행

```bash
# Gradle로 빌드 및 실행
./gradlew bootRun

# 또는 jar 파일로 실행
./gradlew build
java -jar build/libs/oop-team8-assigment4-0.0.1-SNAPSHOT.jar
```

### 3. API 문서 확인

애플리케이션 실행 후 Swagger UI 접속:
```
http://localhost:8080/swagger-ui.html
```

### 4. 웹 UI 접속

웹 브라우저에서 접속:
```
http://localhost:8080
```

또는

```
http://localhost:8080/index.html
```

애플리케이션이 시작되면 자동으로 4개의 테스트 프로젝트가 생성됩니다:
- **Hello World**: 간단한 출력 프로그램
- **Conditional Test**: IF 조건문 예제
- **Loop Test**: WHILE 반복문 예제
- **Arithmetic Operations**: 사칙연산 예제

## 웹 UI 사용법

### 기능

1. **프로젝트 목록**: 왼쪽 사이드바에서 모든 프로젝트를 확인할 수 있습니다.
2. **프로젝트 선택**: 프로젝트를 클릭하면 해당 프로젝트의 블록 목록이 표시됩니다.
3. **블록 정보**: 각 블록의 타입, 순서, 상세 정보, 연결 정보를 테이블 형식으로 확인할 수 있습니다.
4. **프로젝트 실행**: Run Project 버튼을 클릭하면 프로그램이 실행되고, WebSocket을 통해 실시간 실행 결과를 확인할 수 있습니다.
5. **출력 화면**: 실행 결과가 터미널 스타일의 화면에 실시간으로 표시됩니다.

### 자동 생성된 테스트 데이터

애플리케이션 시작 시 다음 4개의 프로젝트가 자동으로 생성됩니다:

1. **Hello World** (ID: 1)
   - START → PRINT "Hello World!"

2. **Conditional Test** (ID: 2)
   - START → VAR_DECLARE(x=10) → IF(x>5) → PRINT (true/false branches)

3. **Loop Test** (ID: 3)
   - START → VAR_DECLARE(i=0) → WHILE(i<5) → PRINT → ADD(i=i+1)

4. **Arithmetic Operations** (ID: 4)
   - START → VAR_DECLARE(a=10, b=5) → ADD → SUBTRACT → MULTIPLY → DIVIDE

웹 UI에서 프로젝트를 선택하고 실행하여 결과를 확인할 수 있습니다.

## 사용 예시

### 예시 1: Hello World 프로그램

```json
// 1. 프로젝트 생성
POST /api/projects
{
  "name": "Hello World",
  "description": "Simple greeting program"
}

// 2. START 블록 생성 (응답 ID: 1)
POST /api/blocks/project/1
{
  "blockType": "START",
  "positionX": 100,
  "positionY": 100,
  "order": 1,
  "nextBlockId": 2
}

// 3. PRINT 블록 생성 (응답 ID: 2)
POST /api/blocks/project/1
{
  "blockType": "PRINT",
  "positionX": 100,
  "positionY": 200,
  "order": 2,
  "message": "Hello World",
  "nextBlockId": null
}

// 4. 프로그램 실행
POST /api/execution/projects/1/run
```

### 예시 2: 조건문을 사용한 프로그램

```json
// 1. 변수 선언 (x = 10)
POST /api/blocks/project/1
{
  "blockType": "VAR_DECLARE",
  "variableName": "x",
  "initialValue": "10",
  "nextBlockId": 2
}

// 2. IF 조건문
POST /api/blocks/project/1
{
  "blockType": "IF",
  "conditionExpression": "x > 5",
  "trueBranchId": 3,
  "falseBranchId": 4
}

// 3. 조건이 참일 때 실행
POST /api/blocks/project/1
{
  "blockType": "PRINT",
  "message": "x is greater than 5",
  "nextBlockId": null
}

// 4. 조건이 거짓일 때 실행
POST /api/blocks/project/1
{
  "blockType": "PRINT",
  "message": "x is not greater than 5",
  "nextBlockId": null
}
```

### 예시 3: 반복문과 산술 연산

```json
// 1. 변수 선언 (i = 0)
{
  "blockType": "VAR_DECLARE",
  "variableName": "i",
  "initialValue": "0",
  "nextBlockId": 2
}

// 2. WHILE 반복문 (i < 5)
{
  "blockType": "WHILE",
  "conditionExpression": "i < 5",
  "trueBranchId": 3,
  "nextBlockId": null
}

// 3. PRINT (i 값 출력)
{
  "blockType": "PRINT",
  "message": "i",
  "nextBlockId": 4
}

// 4. ADD (i = i + 1)
{
  "blockType": "ADD",
  "operand1": "i",
  "operand2": "1",
  "resultVariable": "i",
  "nextBlockId": 2
}
```

## WebSocket 실시간 출력

### 연결 방법

```javascript
// SockJS + STOMP 사용
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // 실행 세션 구독
    stompClient.subscribe('/topic/execution/' + sessionId, function(message) {
        const output = JSON.parse(message.body);
        console.log(output);
    });
});
```

### 출력 메시지 형식

```json
{
  "type": "PRINT",
  "data": "Hello World"
}

{
  "type": "ADD",
  "data": "result = 5.0 + 3.0 = 8.0"
}

{
  "type": "IF",
  "data": "Condition: x > 5 = true"
}

{
  "type": "COMPLETE",
  "data": "Execution completed successfully"
}

{
  "type": "ERROR",
  "data": "Execution error: Division by zero"
}
```

## 주요 특징

### 1. 객체지향 설계
- **추상화**: Block 추상 클래스를 통한 공통 인터페이스 정의
- **상속**: 블록 타입별 특화된 구현
- **다형성**: execute() 메서드를 통한 다형적 실행
- **캡슐화**: 각 블록이 자신의 실행 로직을 캡슐화

### 2. MVC 패턴
- **Model**: JPA Entity로 정의된 도메인 모델
- **View**: REST API (JSON 응답)
- **Controller**: Spring REST Controller

### 3. 실행 엔진
- **ExecutionContext**: 실행 컨텍스트 관리 (변수, 표현식 평가)
- **단방향 그래프 순회**: START 블록부터 연결 그래프를 따라 실행
- **분기 지원**: 제어 블록의 조건부 분기 처리
- **실시간 스트리밍**: WebSocket을 통한 실행 과정 전송

### 4. 확장성
- 새로운 블록 타입 추가 용이 (Block 상속)
- 표현식 평가기 확장 가능
- 다양한 출력 형식 지원

## 데이터베이스 스키마

### blocks 테이블
```sql
CREATE TABLE blocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    block_type VARCHAR(50),
    position_x INT,
    position_y INT,
    block_order INT,
    project_id BIGINT,
    next_block_id BIGINT,
    FOREIGN KEY (project_id) REFERENCES projects(id)
);
```

### projects 테이블
```sql
CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## 개발 팀

- Team 8

## 라이선스

이 프로젝트는 교육 목적으로 개발되었습니다.
