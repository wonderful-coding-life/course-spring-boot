# 스프링 부트 강의 실습

교재 **『이것이 스프링 부트다 with 자바』**와 함께 사용하는 예제 프로젝트입니다.
교재의 페이지 번호 대신 **장·절 번호와 제목**으로 학습 위치를 찾습니다.
각 프로젝트 README에는 학습 주제, 관련 코드, 교재의 장·절을 정리했습니다.

학습 순서에 따라 개념·실습 코드·코드 설명·예상 실행 결과를 읽으려면 [실습 교재](practice-book/README.md)를 참고합니다. 장별로 파일을 나누고, 데이터베이스 연동은 JDBC·MyBatis·JPA별로 구성했습니다.

## 학습 순서와 교재 연결

| 순서 | 프로젝트 | 학습 내용 | 교재에서 찾아볼 곳 |
|---|---|---|---|
| 1 | [spring-core](spring-core/README.md) | 빈, 의존성 주입, 롬복, 로그 | 1장 「스프링 부트 이해하기」의 1.2, 2장 「스프링 부트 시작하기」의 2.2 |
| 2 | [spring-jdbc](spring-jdbc/README.md) | JDBC로 SQL 실행과 객체 매핑 | 3장 「데이터베이스 연동하기」의 3.1 「JDBC」 |
| 3 | [spring-mybatis](spring-mybatis/README.md) | Mapper와 XML로 SQL 분리, 회원 CRUD | 3.3 「MyBatis」 |
| 4 | [spring-jpa](spring-jpa/README.md) | 엔티티, Repository, 관계 매핑, 페이징 | 3.4 「JPA」 |
| 5 | [spring-rest](spring-rest/README.md) | REST API, HTTP 헤더·JSON·XML, Service·DTO, 트랜잭션, 테스트 | 4장 「RESTful API 작성하기」의 4.1~4.3 |
| 6 | [spring-thymeleaf](spring-thymeleaf/README.md) | 값 출력, 조건·반복, 링크, 폼, 공통 화면 | 5장 「스프링 부트로 게시판 만들기」의 5.2 「템플릿 엔진과 타임리프」, 폼은 5.4.4~5.4.5 |
| 7 | [spring-mvc](spring-mvc/README.md) | Member 화면 CRUD, 회원 등록의 기본·검증 버전, 게시글 조회·페이징, 공통 예외 처리, H2 | 5.1.1 「웹 애플리케이션 아키텍처와 MVC 패턴」, 5.4 「스프링 부트 게시판 애플리케이션 만들기」 |
| 8 | [spring-security](spring-security/README.md) | 필터 체인의 인증·인가, 이메일 로그인, 비밀번호 해시, 권한별 상품 CRUD, 로그인 정보·CSRF | 5.3 「스프링 시큐리티」 |

각 폴더는 독립적인 Gradle 프로젝트입니다. 실행 명령은 해당 프로젝트 폴더에서 사용합니다.

## 공통 실습 환경

- Java 21과 IntelliJ IDEA를 사용합니다. 각 프로젝트의 `build.gradle`이 실제 의존성과 버전의 기준입니다.
- JDBC·MyBatis·JPA·REST는 외부 MariaDB를, MVC·Security는 H2 메모리 DB를 사용합니다.
- MariaDB를 사용하는 프로젝트는 `DB_URL`, `DB_USER`, `DB_PASS` 환경변수가 필요합니다. `DB_URL`에는 MariaDB JDBC URL을 지정합니다.
- MariaDB 프로젝트의 `sql/schema.sql`과 `sql/data.sql`은 DB 도구에서 직접 실행합니다. MVC·Security의 `src/main/resources/schema.sql`과 `data.sql`은 애플리케이션 시작 시 자동 실행됩니다.
- JDBC·MyBatis·JPA·REST·MVC는 내용이 동일한 공통 `schema.sql`, `data.sql`을 사용합니다. 어느 프로젝트의 파일이든 배포용으로 사용할 수 있습니다.
- 독립 실습 시에는 빈 DB에 스키마와 데이터를 준비합니다. 공통 SQL로 준비한 MariaDB를 다음 모듈에서 재사용한다면 `data.sql`을 다시 실행하지 않습니다. 재실행하면 이메일이 중복되거나 게시글이 추가로 등록됩니다.
- 회원 샘플은 윤서준·윤광철·공미영·김도윤이며 이메일 도메인은 `@campus.com`입니다.
- 게시글은 100개이며 윤서준·윤광철·공미영·김도윤이 순서대로 25개씩 작성합니다. JDBC·MyBatis에서는 회원 테이블만 실습합니다.
- 실제 회원 ID는 이메일로 한 번씩 조회해 세션 변수에 저장합니다. MariaDB/MySQL과 H2에서 지원하며 현재 연결에서만 유지됩니다. `SET`과 `INSERT`는 같은 DB 연결에서 실행합니다.
- 게시글 작성자는 외래 키로 연결되어 있어 바로 삭제할 수 없습니다. 회원 삭제 실습은 새로 등록한 회원으로 진행합니다.
- `schema.sql`의 `IF NOT EXISTS`는 이미 있는 테이블의 컬럼이나 제약을 변경하지 않습니다.
- Security는 로그인용 회원과 상품 테이블을 준비하는 전용 SQL을 `src/main/resources`에서 자동 실행합니다. 회원 이메일은 기존 샘플을 사용하고 BCrypt 비밀번호 해시와 권한을 추가합니다.

## 실습 결과를 확인하는 방법

- `spring-core`, `spring-jdbc`: 애플리케이션을 실행하고 로그를 확인합니다.
- `spring-mybatis`, `spring-jpa`: `@Test` 메서드를 하나씩 실행하고 한글 로그를 확인합니다. 변경 내용은 테스트 후 롤백됩니다.
- `spring-rest`: IntelliJ HTTP Client로 API를 호출하고, 통합·단위 테스트에서 자동 검증을 실습합니다.
- `spring-thymeleaf`: DB 없이 애플리케이션을 실행하고, 브라우저에서 화면 출력과 폼 제출 결과를 확인합니다.
- `spring-mvc`: 브라우저에서 회원 등록의 기본·검증 버전을 비교하고, 게시글 페이징·공통 오류 화면을 확인합니다. 검증 버전은 이름·이메일·나이와 이메일 중복을 확인합니다. 앱을 다시 실행하면 H2 샘플 데이터로 시작합니다.
- `spring-security`: 홈 → 이메일 로그인 → 상품 목록 순서로 진행하며, 일반 회원의 조회와 관리자의 등록·수정·삭제 권한을 비교합니다. 상품 목록의 수정·삭제는 GET 링크이며, 등록·수정 저장은 POST 폼을 사용합니다. 로그아웃 확인 후 홈으로 돌아갑니다. 계정과 실습 비밀번호는 프로젝트 README를 참고합니다.

표의 교재 연결은 학습한 개념을 다시 찾아보기 위한 안내입니다.
