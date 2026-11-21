# 스프링 부트 강의 실습

교재 **『이것이 스프링 부트다 with 자바』**와 함께 사용하는 예제 프로젝트입니다.
교재의 페이지 번호 대신 **장·절 번호와 제목**으로 학습 위치를 찾습니다.
각 프로젝트 README에는 학습 주제, 관련 코드, 교재의 장·절을 정리했습니다.

## 학습 순서와 교재 연결

| 순서 | 프로젝트 | 학습 내용 | 교재에서 찾아볼 곳 |
|---|---|---|---|
| 1 | [spring-core](spring-core/README.md) | 빈, 의존성 주입, 롬복, 로그 | 1장 「스프링 부트 이해하기」의 1.2, 2장 「스프링 부트 시작하기」의 2.2 |
| 2 | [spring-jdbc](spring-jdbc/README.md) | JDBC로 SQL 실행과 객체 매핑 | 3장 「데이터베이스 연동하기」의 3.1 「JDBC」 |
| 3 | [spring-mybatis](spring-mybatis/README.md) | Mapper와 XML로 SQL 분리, 회원 CRUD | 3.3 「MyBatis」 |
| 4 | [spring-jpa](spring-jpa/README.md) | 엔티티, Repository, 관계 매핑, 페이징 | 3.4 「JPA」 |
| 5 | [spring-rest](spring-rest/README.md) | REST API, Service·DTO, 트랜잭션, 테스트 | 4장 「RESTful API 작성하기」의 4.1~4.3 |

각 폴더는 독립적인 Gradle 프로젝트입니다. 실행 명령은 해당 프로젝트 폴더에서 사용합니다.

## 공통 실습 환경

- Java 21과 IntelliJ IDEA를 사용합니다. 각 프로젝트의 `build.gradle`이 실제 의존성과 버전의 기준입니다.
- 데이터베이스는 외부 MariaDB를 사용합니다.
- DB를 사용하는 프로젝트는 `DB_URL`, `DB_USER`, `DB_PASS` 환경변수가 필요합니다. `DB_URL`에는 MariaDB JDBC URL을 지정합니다.
- `sql/schema.sql`과 `sql/data.sql`은 DB 도구에서 직접 실행합니다. 해당 폴더의 SQL이 애플리케이션 시작 시 자동 실행되는 구성은 아닙니다.
- 독립 실습 시에는 해당 프로젝트의 스키마와 데이터를 준비합니다. 앞 모듈과 같은 DB를 재사용한다면 이미 등록한 회원 데이터를 다시 INSERT하지 않습니다.
- 회원 샘플은 윤서준·윤광철·공미영·김도윤이며 이메일 도메인은 `@campus.com`입니다.
- JPA·REST의 게시글은 100개이며 회원별로 25개씩 작성합니다. 실제 회원 ID는 이메일로 조회해 SQL 변수에 저장합니다. `SET`과 `INSERT`는 같은 DB 연결에서 실행합니다.

## 실습 결과를 확인하는 방법

- `spring-core`, `spring-jdbc`: 애플리케이션을 실행하고 로그를 확인합니다.
- `spring-mybatis`, `spring-jpa`: `@Test` 메서드를 하나씩 실행하고 한글 로그를 확인합니다. 변경 내용은 테스트 후 롤백됩니다.
- `spring-rest`: IntelliJ HTTP Client로 API를 호출하고, 통합·단위 테스트에서 자동 검증을 실습합니다.

표의 교재 연결은 학습한 개념을 다시 찾아보기 위한 안내입니다.
