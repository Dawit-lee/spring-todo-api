# 할 일 REST API

Spring Boot 기반 할 일 REST API입니다. 회원 기능 없이 할 일을 생성·조회·수정·삭제하고 완료 여부를 변경합니다.

## 실행 방법

- JDK 21을 설치하고 `JAVA_HOME`을 JDK 21 경로로 설정합니다.
- Gradle은 포함된 Wrapper를 사용하므로 별도 설치가 필요 없습니다.
- DB는 내장 H2를 사용하므로 별도 설치가 필요 없습니다.
- 최초 실행 시 의존성 다운로드를 위한 인터넷 연결이 필요합니다.

프로젝트 루트에서 실행합니다.

```bash
./gradlew bootRun
```

기본 서버 주소는 `http://localhost:8080`이며, 목록 API는 `GET /api/todos`입니다.

Windows에서는 `gradlew.bat bootRun`을 사용합니다.

## 기술 구성과 선택 이유

- Java 21, Spring Boot 3.5.16: 과제의 Spring Boot 3.x 요구사항을 충족합니다.
- Spring Web: REST API 구현에 사용합니다.
- Spring Data JPA: 엔티티와 Repository를 통한 DB 접근에 사용합니다.
- Jakarta Bean Validation: 요청 DTO의 제목 등 입력값 검증에 사용합니다.
- H2: 별도 DB 준비 없이 실행할 수 있습니다. 파일 모드로 `data/` 아래에 저장하므로 서버 재시작 후에도 데이터가 유지됩니다. 생성된 DB 파일은 Git에서 제외합니다.
- `ddl-auto=update`: 과제 개발 단계에서 엔티티에 맞춰 테이블을 생성·갱신합니다.
- `open-in-view=false`: DB 접근을 서비스의 트랜잭션 안에서 처리하도록 설정합니다.

## 테스트

```bash
./gradlew test
```

테스트는 별도 메모리 H2 DB를 사용하므로 로컬 실행 데이터에 영향을 주지 않습니다.
애플리케이션 컨텍스트 로딩 테스트와 `@DataJpaTest` 기반 저장·조회·수정·삭제 및 제목 제약조건 테스트가 포함되어 있습니다.
`@SpringBootTest`·MockMvc 통합 테스트에서는 정상 CRUD 흐름, 완료·미완료 전환, 입력 검증, 없는 ID의 조회·수정·삭제, 잘못된 JSON·경로·HTTP 메서드·Content-Type을 검증합니다.

## 할 일 데이터 모델

| 필드 | 타입 | 규칙 |
| --- | --- | --- |
| id | Long | DB가 자동 생성하는 식별자 |
| title | String | 필수, 공백만 입력 불가, 최대 200자 |
| completed | boolean | 생성 시 미완료(false), 완료·미완료 변경 가능 |
| createdAt | Instant | 최초 저장 시 서버에서 생성, 이후 변경하지 않는 UTC 시각 |

`TodoRepository`는 `JpaRepository<Todo, Long>`을 상속해 기본 CRUD 기능을 사용합니다.
엔티티와 요청 DTO에 제목 검증을 적용했습니다. Controller는 HTTP 요청·응답, Service는 트랜잭션과 업무 처리, Repository는 DB 접근을 담당합니다.
요청에는 `CreateTodoRequest`·`UpdateTodoRequest`, 응답에는 `TodoResponse`를 사용하며 JPA 엔티티를 직접 노출하지 않습니다.

## API 명세

요청 본문은 `Content-Type: application/json`을 사용합니다. ID는 정수(Long)입니다.

| 기능 | 메서드 | 주소 | 요청 본문 | 성공 응답 | 오류 |
| --- | --- | --- | --- | --- | --- |
| 생성 | POST | `/api/todos` | `{"title":"Spring 공부"}` | 201, 할 일 객체 + Location 헤더 | 400 |
| 목록 | GET | `/api/todos` | 없음 | 200, 할 일 객체 배열(없으면 `[]`) | — |
| 단건 | GET | `/api/todos/{id}` | 없음 | 200, 할 일 객체 | 400, 404 |
| 수정·완료 변경 | PUT | `/api/todos/{id}` | `{"title":"Spring 복습","completed":true}` | 200, 수정된 할 일 객체 | 400, 404 |
| 삭제 | DELETE | `/api/todos/{id}` | 없음 | 204, 본문 없음 | 400, 404 |

성공 응답의 할 일 객체는 다음 모양입니다.

```json
{"id":1,"title":"Spring 공부","completed":false,"createdAt":"2026-09-26T13:13:06.042815Z"}
```

- 생성 시 제목이 필수이며, 완료 여부는 항상 `false`로 시작합니다. ID와 생성 시각은 서버가 정합니다.
- 제목은 1~200자이며 비어 있거나 공백뿐이면 400입니다. 앞뒤 공백은 자동으로 제거하지 않습니다.
- 수정 시 제목과 `completed`가 모두 필수입니다. `completed`에 `false`를 보내면 미완료로 변경합니다. ID와 생성 시각은 수정하지 않습니다.
- 목록은 ID 오름차순입니다. 페이지 나누기와 완료 여부 필터는 아직 제공하지 않습니다.
- 없는 ID의 조회·수정·삭제는 404입니다. 요청 형식이 잘못된 경우에는 입력 검증이 먼저 실행되어 400이 반환될 수 있습니다.

### 공통 오류 응답

오류 본문은 `status`(HTTP 상태 코드), `message`(설명), `errors`(필드별 오류 배열)로 통일합니다. 필드별 오류가 없으면 빈 배열입니다.

```json
{
  "status":400,
  "message":"입력값이 올바르지 않습니다.",
  "errors":[{"field":"title","message":"제목은 비어 있거나 공백뿐일 수 없습니다."}]
}
```

| 상태 | 발생 조건 |
| --- | --- |
| 400 | 제목·완료 여부 검증 실패, 잘못된 JSON, 정수로 해석할 수 없는 ID |
| 404 | 없는 할 일 ID 또는 없는 주소 |
| 405 | 해당 주소에서 지원하지 않는 HTTP 메서드 |
| 406 | 지원하지 않는 응답 형식 요청 |
| 415 | 지원하지 않는 요청 Content-Type |
| 500 | 예상하지 못한 서버 오류(내부 예외 내용은 응답에 노출하지 않음) |

### 주소와 상태 코드 설계 이유

- `/api/todos`는 할 일 컬렉션을, `/api/todos/{id}`는 하나의 할 일을 나타내는 복수형 명사 주소입니다.
- POST는 새 리소스를 만들므로 201과 생성된 리소스 주소인 `Location`을 반환합니다.
- GET은 저장 상태를 변경하지 않고 조회하며 200을 반환합니다.
- PUT은 사용자가 수정할 수 있는 제목·완료 여부를 함께 지정합니다. 같은 요청을 반복해도 최종 상태가 같고, 수정 결과를 확인하도록 200과 객체를 반환합니다.
- DELETE는 삭제 성공 후 전달할 데이터가 없어 204를 반환합니다. 이미 삭제한 ID는 존재하지 않으므로 이후 요청은 404입니다.
- 입력 오류는 클라이언트가 고칠 수 있도록 400과 이유를, 없는 리소스는 404를 반환합니다.

## 실제 curl 실행 결과

다음은 임시 메모리 DB로 실행한 서버에서 직접 확인한 결과입니다. 가독성을 위해 Date·Transfer-Encoding 등 부가 헤더는 생략했습니다. 일반 실행 시 포트는 8080이며, ID와 시각은 실행마다 달라집니다.

검증용 서버 실행 명령:

```bash
./gradlew bootRun --args='--server.port=18081 --spring.datasource.url=jdbc:h2:mem:todo-demo'
```

### 1. 생성

```bash
curl -i -X POST http://localhost:18081/api/todos \
  -H 'Content-Type: application/json' -d '{"title":"Spring 공부"}'
```

```http
HTTP/1.1 201
Location: /api/todos/1
Content-Type: application/json

{"id":1,"title":"Spring 공부","completed":false,"createdAt":"2026-09-26T13:13:06.042815Z"}
```

### 2. 목록 조회

```bash
curl -i http://localhost:18081/api/todos
```

```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"title":"Spring 공부","completed":false,"createdAt":"2026-09-26T13:13:06.042815Z"}]
```

### 3. 완료 처리

```bash
curl -i -X PUT http://localhost:18081/api/todos/1 \
  -H 'Content-Type: application/json' -d '{"title":"Spring 공부","completed":true}'
```

```http
HTTP/1.1 200
Content-Type: application/json

{"id":1,"title":"Spring 공부","completed":true,"createdAt":"2026-09-26T13:13:06.042815Z"}
```

### 4. 삭제

```bash
curl -i -X DELETE http://localhost:18081/api/todos/1
```

```http
HTTP/1.1 204
```

### 5. 입력 오류(400)

```bash
curl -i -X POST http://localhost:18081/api/todos \
  -H 'Content-Type: application/json' -d '{"title":"   "}'
```

```http
HTTP/1.1 400
Content-Type: application/json

{"status":400,"message":"입력값이 올바르지 않습니다.","errors":[{"field":"title","message":"제목은 비어 있거나 공백뿐일 수 없습니다."}]}
```

### 6. 삭제한 할 일 조회(404)

```bash
curl -i http://localhost:18081/api/todos/1
```

```http
HTTP/1.1 404
Content-Type: application/json

{"status":404,"message":"할 일을 찾을 수 없습니다. id=1","errors":[]}
```
