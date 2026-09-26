# 할 일 REST API

Spring Boot 기반 할 일 백엔드 과제입니다. 현재 프로젝트 구성 단계이며 할 일 API는 이후 구현합니다.

## 실행 방법

- JDK 21을 설치하고 `JAVA_HOME`을 JDK 21 경로로 설정합니다.
- Gradle은 포함된 Wrapper를 사용하므로 별도 설치가 필요 없습니다.
- DB는 내장 H2를 사용하므로 별도 설치가 필요 없습니다.
- 최초 실행 시 의존성 다운로드를 위한 인터넷 연결이 필요합니다.

프로젝트 루트에서 실행합니다.

```bash
./gradlew bootRun
```

기본 서버 주소는 `http://localhost:8080`입니다. 아직 API가 없어 해당 주소 요청에는 404가 반환됩니다.

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
현재 기본 애플리케이션 컨텍스트 로딩 테스트가 포함되어 있습니다.
