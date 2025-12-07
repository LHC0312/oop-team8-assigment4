# 프론트엔드 연동 가이드

> 블록 코딩 백엔드 API 연동을 위한 상세 기술 문서

---

## 목차

1. [시스템 개요](#1-시스템-개요)
2. [블록 타입 상세](#2-블록-타입-상세)
3. [표현식 블록 (ValueDto)](#3-표현식-블록-valuedto)
4. [API 상세](#4-api-상세)
5. [WebSocket 실시간 통신](#5-websocket-실시간-통신)
6. [실제 구현 시나리오](#6-실제-구현-시나리오)
7. [오류 처리](#7-오류-처리)

---

## 1. 시스템 개요

### 1.1 아키텍처

```
┌─────────────────┐     REST API      ┌──────────────────┐
│   프론트엔드     │ ◄─────────────────► │   Spring Boot    │
│   (React 등)    │                    │   Backend        │
└────────┬────────┘                    └────────┬─────────┘
         │                                      │
         │  WebSocket (STOMP)                   │
         └──────────────────────────────────────┘
                /topic/execution/{sessionId}
```

### 1.2 핵심 개념

| 개념 | 설명 |
|------|------|
| **Block** | 실행 단위. START, IF, WHILE 등 7종 |
| **Expression** | 값/연산 표현. LITERAL, VARIABLE, BINARY 등 |
| **Project** | 블록들의 컨테이너. 프로젝트별로 독립 실행 |
| **Variable** | 프로젝트 내 고유한 변수. ID로 참조 |

---

## 2. 블록 타입 상세

### 2.1 블록 공통 구조

모든 블록 응답(BlockDto)은 다음 공통 필드를 포함합니다:

```json
{
  "id": 1,
  "blockType": "START",
  "positionX": 100,
  "positionY": 200,
  "order": 1,
  "nextBlockId": 2,
  "conditionExpressionId": null,
  "messageExpressionId": null,
  "valueExpressionId": null,
  "variableId": null
}
```

> **Note**: **요청(Request)** 시에는 `condition`, `message` 등에 표현식 객체(`ValueDto`)를 담아 보내면 한 번에 생성되지만, **응답(Response)** 시에는 `conditionExpressionId`와 같이 **ID만 반환**됩니다. 표현식의 상세 정보는 별도로 `/api/expressions/project/{projectId}`를 통해 조회해야 합니다.

### 2.2 START 블록

프로그램의 진입점입니다. 프로젝트당 하나만 존재해야 합니다.

**생성 요청:**
```json
POST /api/blocks/project/{projectId}

{
  "blockType": "START",
  "positionX": 100,
  "positionY": 100,
  "nextBlockId": null
}
```

**응답:**
```json
{
  "id": 1,
  "blockType": "START",
  "positionX": 100,
  "positionY": 100,
  "nextBlockId": null
}
```

---

### 2.3 IF 블록 (조건 분기)

조건에 따라 true/false 분기를 실행합니다.

#### IF만 (else 없음)

`falseBranchId`가 `null`이면 조건이 거짓일 때 `nextBlockId`로 진행합니다.

```json
POST /api/blocks/project/{projectId}

{
  "blockType": "IF",
  "condition": {
    "valueType": "BINARY",
    "operator": ">",
    "left": { "valueType": "VARIABLE", "variableId": 5 },
    "right": { "valueType": "LITERAL", "data": 10 }
  },
  "trueBranchId": 10,
  "falseBranchId": null,
  "nextBlockId": 15,
  "positionX": 100,
  "positionY": 200
}
```

**실행 흐름:**
```
condition == true  → trueBranchId (10) 실행 → nextBlockId (15)로 합류
condition == false → nextBlockId (15)로 바로 이동
```

#### IF-ELSE (양쪽 분기)

`falseBranchId`를 지정하면 else 분기가 생깁니다.

```json
{
  "blockType": "IF",
  "condition": {
    "valueType": "BINARY",
    "operator": ">",
    "left": { "valueType": "VARIABLE", "variableId": 5 },
    "right": { "valueType": "LITERAL", "data": 10 }
  },
  "trueBranchId": 10,
  "falseBranchId": 12,
  "nextBlockId": 15,
  "positionX": 100,
  "positionY": 200
}
```

}
```

**응답 (Response):**
```json
{
  "id": 10,
  "blockType": "IF",
  "conditionExpressionId": 105,
  "trueBranchId": 10,
  "falseBranchId": 12,
  "nextBlockId": 15,
  "positionX": 100,
  "positionY": 200
}
```

**실행 흐름:**
```
condition == true  → trueBranchId (10) 실행 → nextBlockId (15)로 합류
condition == false → falseBranchId (12) 실행 → nextBlockId (15)로 합류
```

---

### 2.4 WHILE 블록 (반복문)

조건이 참인 동안 `trueBranchId` 블록을 반복 실행합니다.

```json
POST /api/blocks/project/{projectId}

{
  "blockType": "WHILE",
  "condition": {
    "valueType": "BINARY",
    "operator": "<",
    "left": { "valueType": "VARIABLE", "variableId": 5 },
    "right": { "valueType": "LITERAL", "data": 5 }
  },
  "trueBranchId": 10,
  "nextBlockId": 20,
  "positionX": 100,
  "positionY": 300
}
```

}
```

**응답 (Response):**
```json
{
  "id": 20,
  "blockType": "WHILE",
  "conditionExpressionId": 205,
  "trueBranchId": 10,
  "nextBlockId": 20,
  "positionX": 100,
  "positionY": 300
}
```

**실행 흐름:**
```
조건 검사 → true면 trueBranchId 실행 → 조건 재검사 (반복)
         → false면 nextBlockId로 이동
```

---

### 2.5 FOR 블록 (반복문)

초기화, 조건, 증감 표현식을 포함한 반복문입니다.

```json
POST /api/blocks/project/{projectId}

{
  "blockType": "FOR",
  "init": {
    "valueType": "BINARY",
    "operator": "=",
    "left": { "valueType": "VARIABLE", "variableId": 5 },
    "right": { "valueType": "LITERAL", "data": 0 }
  },
  "condition": {
    "valueType": "BINARY",
    "operator": "<",
    "left": { "valueType": "VARIABLE", "variableId": 5 },
    "right": { "valueType": "LITERAL", "data": 10 }
  },
  "increment": {
    "valueType": "BINARY",
    "operator": "+",
    "left": { "valueType": "VARIABLE", "variableId": 5 },
    "right": { "valueType": "LITERAL", "data": 1 }
  },
  "trueBranchId": 15,
  "nextBlockId": 25,
  "positionX": 100,
  "positionY": 400
}
```

}
```

**응답 (Response):**
```json
{
  "id": 30,
  "blockType": "FOR",
  "initExpressionId": 301,
  "conditionExpressionId": 302,
  "incrementExpressionId": 303,
  "trueBranchId": 15,
  "nextBlockId": 25,
  "positionX": 100,
  "positionY": 400
}
```

---

### 2.6 PRINT 블록 (출력)

표현식을 문자열로 평가하여 출력합니다.

```json
POST /api/blocks/project/{projectId}

{
  "blockType": "PRINT",
  "message": { "valueType": "LITERAL", "data": "Hello World!" },
  "nextBlockId": 5,
  "positionX": 100,
  "positionY": 200
}
```

**변수 출력 예시:**
```json
{
  "blockType": "PRINT",
  "message": { "valueType": "VARIABLE", "variableId": 10 },
  "nextBlockId": 5
}
```

**문자열 연결 출력 예시:**
```json
{
  "blockType": "PRINT",
  "message": {
    "valueType": "BINARY",
    "operator": "+",
    "left": { "valueType": "LITERAL", "data": "결과: " },
    "right": { "valueType": "VARIABLE", "variableId": 10 }
  },
  "nextBlockId": 5
}
```

---

### 2.7 VAR_DECLARE 블록 (변수 선언)

새 변수를 생성합니다. 응답에서 `variableId`를 받아 이후 참조에 사용합니다.

```json
POST /api/blocks/project/{projectId}

{
  "blockType": "VAR_DECLARE",
  "variableName": "counter",
  "variableType": "number",
  "initial": { "valueType": "LITERAL", "data": 0 },
  "nextBlockId": 3,
  "positionX": 100,
  "positionY": 200
}
```

**응답:**
```json
{
  "id": 2,
  "blockType": "VAR_DECLARE",
  "variableName": "counter",
  "variableType": "number",
  "variableId": 15,
  "initialExpressionId": 101, // 생성된 표현식 ID
  "nextBlockId": 3
}
```

> ⚠️ **중요**: 응답의 `variableId`를 저장하여 이후 VAR_ASSIGN, 표현식에서 사용하세요. `initial` 객체는 반환되지 않으므로 `initialExpressionId`를 통해 참조해야 합니다.

**지원 타입:**
| variableType | 설명 | 예시 |
|--------------|------|------|
| `number` | 숫자 | 0, 3.14, -100 |
| `string` | 문자열 | "hello" |
| `boolean` | 불리언 | true, false |

---

### 2.8 VAR_ASSIGN 블록 (변수 할당)

기존 변수에 새 값을 할당합니다. **산술 연산 결과 저장도 이 블록을 사용합니다.**

**단순 할당:**
```json
POST /api/blocks/project/{projectId}

{
  "blockType": "VAR_ASSIGN",
  "variableId": 15,
  "value": { "valueType": "LITERAL", "data": 100 },
  "nextBlockId": 5,
  "positionX": 100,
  "positionY": 300
}
```

**산술 연산 결과 할당 (a + b → result):**
```json
{
  "blockType": "VAR_ASSIGN",
  "variableId": 20,
  "value": {
    "valueType": "BINARY",
    "operator": "+",
    "left": { "valueType": "VARIABLE", "variableId": 15 },
    "right": { "valueType": "VARIABLE", "variableId": 16 }
  },
  "nextBlockId": 6
}
```

**증감 연산 (i = i + 1):**
```json
{
  "blockType": "VAR_ASSIGN",
  "variableId": 15,
  "value": {
    "valueType": "BINARY",
    "operator": "+",
    "left": { "valueType": "VARIABLE", "variableId": 15 },
    "right": { "valueType": "LITERAL", "data": 1 }
  },
  "nextBlockId": 7
}
```

---

## 3. 표현식 블록 (ValueDto)

### 3.1 표현식 개요

표현식은 값을 나타내는 트리 구조입니다. 블록 생성 시 `condition`, `message`, `value`, `initial` 등의 필드에 사용됩니다.

### 3.2 LITERAL (리터럴)

```json
{ "valueType": "LITERAL", "data": 42 }        // 숫자
{ "valueType": "LITERAL", "data": "hello" }   // 문자열
{ "valueType": "LITERAL", "data": true }      // 불리언
```

### 3.3 VARIABLE (변수 참조)

```json
{ "valueType": "VARIABLE", "variableId": 15 }
```

> `variableName`도 지원하지만 `variableId` 사용을 권장합니다.

### 3.4 UNARY (단항 연산)

```json
{
  "valueType": "UNARY",
  "operator": "-",
  "operand": { "valueType": "VARIABLE", "variableId": 15 }
}
```

**지원 연산자:**
- `-` (부호 반전)
- `!` (논리 NOT)

### 3.5 BINARY (이항 연산)

```json
{
  "valueType": "BINARY",
  "operator": "+",
  "left": { "valueType": "VARIABLE", "variableId": 15 },
  "right": { "valueType": "LITERAL", "data": 10 }
}
```

**지원 연산자:**

| 분류 | 연산자 | 설명 |
|------|--------|------|
| 산술 | `+` | 덧셈 (숫자) 또는 문자열 연결 |
| 산술 | `-` | 뺄셈 |
| 산술 | `*` | 곱셈 |
| 산술 | `/` | 나눗셈 |
| 산술 | `%` | 나머지 |
| 비교 | `==` | 같음 |
| 비교 | `!=` | 다름 |
| 비교 | `>` | 초과 |
| 비교 | `<` | 미만 |
| 비교 | `>=` | 이상 |
| 비교 | `<=` | 이하 |
| 논리 | `&&` | AND |
| 논리 | `\|\|` | OR |

### 3.6 중첩 표현식 예시

```json
// (a + b) * 2
{
  "valueType": "BINARY",
  "operator": "*",
  "left": {
    "valueType": "BINARY",
    "operator": "+",
    "left": { "valueType": "VARIABLE", "variableId": 10 },
    "right": { "valueType": "VARIABLE", "variableId": 11 }
  },
  "right": { "valueType": "LITERAL", "data": 2 }
}
```

### 3.7 표현식 좌표

표현식에도 UI 위치를 저장할 수 있습니다:

```json
{
  "valueType": "BINARY",
  "operator": "+",
  "left": { "valueType": "VARIABLE", "variableId": 10, "positionX": 50, "positionY": 60 },
  "right": { "valueType": "LITERAL", "data": 1, "positionX": 120, "positionY": 60 },
  "positionX": 30,
  "positionY": 40
}
```

---

## 4. API 상세

### 4.1 블록 생성

```http
POST /api/blocks/project/{projectId}
Content-Type: application/json

{
  "blockType": "PRINT",
  "message": { "valueType": "LITERAL", "data": "Hello" },
  "positionX": 100,
  "positionY": 200
}
```

**응답 (201 Created):**
```json
{
  "id": 5,
  "blockType": "PRINT",
  "positionX": 100,
  "positionY": 200,
  "messageExpressionId": 101 // 생성된 메시지 표현식의 ID
}
```

### 4.2 블록 수정

```http
PUT /api/blocks/{id}
Content-Type: application/json

{
  "nextBlockId": 10,
  "positionX": 150
}
```

> `null`이 아닌 필드만 업데이트됩니다.

### 4.3 블록-표현식 연결 (중요)

블록의 슬롯(condition, message 등)에 표현식을 연결하거나 해제합니다.

```http
POST /api/blocks/project/{projectId}/connect-expressions
Content-Type: application/json

[
  {
    "blockId": 5,
    "slot": "message",
    "expressionId": 101
  },
  {
    "blockId": 6,
    "slot": "condition",
    "expressionId": null  // 연결 해제
  }
]
```

**지원 슬롯(slot):**
- `condition`: IF, WHILE, FOR
- `message`: PRINT
- `value`: VAR_ASSIGN
- `initial`: VAR_DECLARE
- `init`, `increment`: FOR

> **에러 처리**:
> - 잘못된 슬롯이나 블록 ID: **400 Bad Request**
> - 존재하지 않는 표현식 ID: **404 Not Found**

### 4.4 블록 연결 일괄 수정 (Flow)

블록 간의 실행 흐름(next, true/false branch)을 수정합니다.

```http
POST /api/blocks/project/{projectId}/connect
Content-Type: application/json

[
  { "blockId": 1, "nextBlockId": 2 },
  { "blockId": 5, "trueBranchId": 10, "falseBranchId": 12 }
]
```

### 4.4 블록 삭제

```http
DELETE /api/blocks/{id}
```

> ⚠️ 해당 블록과 연결된 표현식, 후속 블록이 **재귀적으로 삭제**됩니다.

### 4.5 표현식 독립 생성

블록과 별개로 표현식만 먼저 생성할 수 있습니다.

```http
POST /api/expressions/project/{projectId}
Content-Type: application/json

{
  "valueType": "BINARY",
  "operator": "+",
  "left": { "valueType": "VARIABLE", "variableId": 10 },
  "right": { "valueType": "LITERAL", "data": 1 }
}
```

**응답:**
```json
{
  "blockId": 150,
  "valueType": "BINARY",
  "operator": "+",
  "left": { "blockId": 148, "valueType": "VARIABLE", "variableId": 10 },
  "right": { "blockId": 149, "valueType": "LITERAL", "data": 1 }
}
```

### 4.6 표현식 수정 (New)

표현식의 위치 등을 업데이트합니다.

```http
PUT /api/expressions/{expressionId}
Content-Type: application/json

{
  "positionX": 200,
  "positionY": 300
}
```

### 4.7 표현식 연결 갱신

표현식 간의 관계(부모-자식)를 수정합니다. `null`을 보내면 연결이 해제됩니다.

```http
POST /api/expressions/project/{projectId}/connect
Content-Type: application/json

[
  {
    "expressionId": 150,
    "leftExpressionId": 148,
    "rightExpressionId": null // 오른쪽 연결 해제
  }
]
```

### 4.6 실행

```http
POST /api/execution/projects/{projectId}/run?debug=false&trace=true
```

**쿼리 파라미터:**
| 파라미터 | 설명 |
|----------|------|
| `debug` | `true`면 각 블록에서 대기 (스텝 실행 필요) |
| `trace` | `true`면 각 블록 실행 정보 WebSocket으로 전송 |

**응답:**
```json
{
  "sessionId": "abc-123",
  "status": "RUNNING"
}
```

### 4.7 디버그 스텝 실행

```http
POST /api/execution/sessions/{sessionId}/step
```

### 4.8 실행 중 변수 조회

```http
GET /api/execution/sessions/{sessionId}/variables
```

**응답:**
```json
{
  "i": 3,
  "sum": 6
}
```

---

## 5. WebSocket 실시간 통신

### 5.1 연결

```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/execution/{sessionId}', (message) => {
    const data = JSON.parse(message.body);
    console.log(data);
  });
});
```

### 5.2 메시지 타입

| type | 설명 | 예시 |
|------|------|------|
| `TRACE` | 블록 실행 추적 | `{ type: "TRACE", blockId: 5, blockType: "PRINT" }` |
| `PRINT` | 출력 메시지 | `{ type: "PRINT", message: "Hello World!" }` |
| `DEBUG_WAIT` | 디버그 대기 상태 | `{ type: "DEBUG_WAIT", blockId: 5 }` |
| `ERROR` | 오류 발생 | `{ type: "ERROR", message: "Division by zero" }` |
| `COMPLETE` | 실행 완료 | `{ type: "COMPLETE" }` |

---

## 6. 실제 구현 시나리오

### 6.1 Hello World

```javascript
// 1. 프로젝트 생성
const project = await fetch('/api/projects', {
  method: 'POST',
  body: JSON.stringify({ name: 'Hello', description: 'Test' })
}).then(r => r.json());

// 2. START 블록 생성
const start = await fetch(`/api/blocks/project/${project.id}`, {
  method: 'POST',
  body: JSON.stringify({ blockType: 'START', positionX: 100, positionY: 100 })
}).then(r => r.json());

// 3. PRINT 블록 생성
const print = await fetch(`/api/blocks/project/${project.id}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'PRINT',
    message: { valueType: 'LITERAL', data: 'Hello World!' },
    positionX: 100,
    positionY: 200
  })
}).then(r => r.json());

// 4. START → PRINT 연결
await fetch(`/api/blocks/${start.id}`, {
  method: 'PUT',
  body: JSON.stringify({ nextBlockId: print.id })
});

// 5. 실행
const result = await fetch(`/api/execution/projects/${project.id}/run`)
  .then(r => r.json());
```

### 6.2 조건문 (IF-ELSE)

```javascript
// 1. 변수 선언 (x = 10)
const varDeclare = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'VAR_DECLARE',
    variableName: 'x',
    variableType: 'number',
    initial: { valueType: 'LITERAL', data: 10 },
    positionX: 100,
    positionY: 200
  })
}).then(r => r.json());

const xId = varDeclare.variableId;

// 2. IF 블록 생성 (x > 5)
const ifBlock = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'IF',
    condition: {
      valueType: 'BINARY',
      operator: '>',
      left: { valueType: 'VARIABLE', variableId: xId },
      right: { valueType: 'LITERAL', data: 5 }
    },
    positionX: 100,
    positionY: 300
  })
}).then(r => r.json());

// 3. true/false 분기 PRINT 블록 생성
const truePrint = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'PRINT',
    message: { valueType: 'LITERAL', data: 'x는 5보다 큽니다' }
  })
}).then(r => r.json());

const falsePrint = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'PRINT',
    message: { valueType: 'LITERAL', data: 'x는 5 이하입니다' }
  })
}).then(r => r.json());

// 4. IF 블록에 분기 연결
await fetch(`/api/blocks/${ifBlock.id}`, {
  method: 'PUT',
  body: JSON.stringify({
    trueBranchId: truePrint.id,
    falseBranchId: falsePrint.id
  })
});
```

### 6.3 반복문 (WHILE)

```javascript
// 0~4 출력하는 WHILE 반복문

// 1. i = 0 선언
const varDeclare = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'VAR_DECLARE',
    variableName: 'i',
    variableType: 'number',
    initial: { valueType: 'LITERAL', data: 0 }
  })
}).then(r => r.json());

const iId = varDeclare.variableId;

// 2. WHILE (i < 5) 생성
const whileBlock = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'WHILE',
    condition: {
      valueType: 'BINARY',
      operator: '<',
      left: { valueType: 'VARIABLE', variableId: iId },
      right: { valueType: 'LITERAL', data: 5 }
    }
  })
}).then(r => r.json());

// 3. PRINT(i) 생성
const printBlock = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'PRINT',
    message: { valueType: 'VARIABLE', variableId: iId }
  })
}).then(r => r.json());

// 4. i = i + 1 생성
const incrementBlock = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'VAR_ASSIGN',
    variableId: iId,
    value: {
      valueType: 'BINARY',
      operator: '+',
      left: { valueType: 'VARIABLE', variableId: iId },
      right: { valueType: 'LITERAL', data: 1 }
    }
  })
}).then(r => r.json());

// 5. 연결: WHILE → PRINT → INCREMENT → WHILE (루프백)
await fetch(`/api/blocks/${whileBlock.id}`, {
  method: 'PUT',
  body: JSON.stringify({ trueBranchId: printBlock.id })
});

await fetch(`/api/blocks/${printBlock.id}`, {
  method: 'PUT',
  body: JSON.stringify({ nextBlockId: incrementBlock.id })
});

await fetch(`/api/blocks/${incrementBlock.id}`, {
  method: 'PUT',
  body: JSON.stringify({ nextBlockId: whileBlock.id })
});
```

### 6.4 산술 연산

산술 연산은 `VAR_ASSIGN` + `BINARY` 표현식으로 구현합니다.

```javascript
// a = 10, b = 5 선언 후 result = a + b 계산

// 1. 변수 선언
const aVar = await createVarDeclare('a', 'number', 10);
const bVar = await createVarDeclare('b', 'number', 5);
const resultVar = await createVarDeclare('result', 'number', 0);

// 2. result = a + b (VAR_ASSIGN으로 산술 결과 저장)
const addAssign = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'VAR_ASSIGN',
    variableId: resultVar.variableId,
    value: {
      valueType: 'BINARY',
      operator: '+',
      left: { valueType: 'VARIABLE', variableId: aVar.variableId },
      right: { valueType: 'VARIABLE', variableId: bVar.variableId }
    }
  })
}).then(r => r.json());

// 3. "Addition: " + result 출력
const printResult = await fetch(`/api/blocks/project/${projectId}`, {
  method: 'POST',
  body: JSON.stringify({
    blockType: 'PRINT',
    message: {
      valueType: 'BINARY',
      operator: '+',
      left: { valueType: 'LITERAL', data: 'Addition: ' },
      right: { valueType: 'VARIABLE', variableId: resultVar.variableId }
    }
  })
}).then(r => r.json());
```

---

## 7. 오류 처리

### 7.1 HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 400 | 잘못된 요청 (필수 필드 누락, 타입 불일치 등) |
| 404 | 리소스 없음 (블록/프로젝트/변수 ID 없음) |
| 500 | 서버 오류 |

### 7.2 흔한 오류

| 오류 메시지 | 원인 | 해결 |
|-------------|------|------|
| `Unknown block type: XXX` | 잘못된 blockType | 7종 블록 타입 확인 |
| `Variable not found: XXX` | 존재하지 않는 variableId | VAR_DECLARE 응답의 variableId 사용 |
| `Project not found` | 존재하지 않는 projectId | 프로젝트 먼저 생성 |
| `Expression not found` | 존재하지 않는 blockId | 표현식 재생성 |
| `Division by zero` | 0으로 나눗셈 | 분모 값 검증 |

### 7.3 타입 규칙

| 연산 | 허용 타입 | 불가 조합 |
|------|----------|----------|
| `+` 산술 | number + number | number + string ❌ |
| `+` 문자열 | string + string | - |
| `-`, `*`, `/`, `%` | number + number | 문자열 ❌ |
| 비교 (`>`, `<` 등) | number + number | 문자열 ❌ |
| 논리 (`&&`, `\|\|`) | boolean + boolean | 숫자/문자열 ❌ |

> ⚠️ PRINT 블록의 `message` 필드는 **evaluateExpressionAsString** 모드로 평가되어 문자열+숫자 연결이 허용됩니다.

---

## 부록: 빠른 참조

### 블록 타입 요약

```
START     → nextBlockId
IF        → condition, trueBranchId, falseBranchId?, nextBlockId
FOR       → init, condition, increment, trueBranchId, nextBlockId
WHILE     → condition, trueBranchId, nextBlockId
PRINT     → message, nextBlockId
VAR_DECLARE → variableName, variableType, initial, nextBlockId
VAR_ASSIGN  → variableId, value, nextBlockId
```

### 표현식 타입 요약

```
LITERAL   → data
VARIABLE  → variableId
UNARY     → operator, operand
BINARY    → operator, left, right
```
