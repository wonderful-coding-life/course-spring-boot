# spring-mvc

Thymeleaf 화면으로 Member 엔티티를 사용하는 기본 CRUD를 실습합니다.
기본 회원 등록을 익힌 뒤 별도의 검증 버전에서 입력 검증을 배우고, 게시글 조회·페이징과 공통 오류 처리로 확장합니다.
교재는 **『이것이 스프링 부트다 with 자바』**의 5장 「스프링 부트로 게시판 만들기」를 참고합니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| MVC와 화면 연결 | [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [회원 목록](src/main/resources/templates/member-list.html) | 5.1.1 「웹 애플리케이션 아키텍처와 MVC 패턴」, 5.2.1 「타임리프 프로젝트 생성」 | Controller가 Model에 데이터를 넣고 HTML 화면 이름을 반환합니다. |
| 회원 CRUD | [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [MemberRepository](src/main/java/com/example/demo/repository/MemberRepository.java), [등록 화면](src/main/resources/templates/member-add.html), [수정 화면](src/main/resources/templates/member-edit.html) | 5.4.6 「관리자 기능 구현」 | Member 엔티티를 직접 받아 목록·상세·등록·수정·삭제와 리다이렉트를 실습합니다. |
| 회원 등록과 입력 검증 | [MemberForm](src/main/java/com/example/demo/form/MemberForm.java), [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [검증 버전 화면](src/main/resources/templates/member-add-validation.html) | 5.4.4 「사용자 로그인 구현」의 회원가입 폼, 5.4.5 「게시판 웹 화면 구현」의 입력 검증 | `@Valid`, `BindingResult`, `th:errors`로 이름·이메일·나이와 이메일 중복을 검증합니다. |
| 게시글 상세·목록 | [ArticleController](src/main/java/com/example/demo/controller/ArticleController.java), [ArticleService](src/main/java/com/example/demo/service/ArticleService.java), [ArticleResponse](src/main/java/com/example/demo/dto/ArticleResponse.java) | 5.4.3 「모델 객체와 서비스 계층 작성」, 5.4.5 「게시판 웹 화면 구현」 | 작성자 이름을 포함한 DTO를 화면에 전달합니다. |
| 게시글 페이징 | [게시글 목록](src/main/resources/templates/article-list.html) | 5.4.5 「게시판 웹 화면 구현」 | `Pageable`, `Page`와 이전·다음·페이지 번호 링크를 연결합니다. |
| 공통 화면 | [common.html](src/main/resources/templates/common.html) | 5.2.4 「타임리프의 레이아웃 관리」 | 메뉴·페이징·하단 안내를 화면 조각으로 재사용합니다. 페이징에는 페이지 정보와 목록 주소를 전달합니다. |
| H2와 초기 SQL | [설정](src/main/resources/application.properties), [schema.sql](src/main/resources/schema.sql), [data.sql](src/main/resources/data.sql) | 3.1.1 「데이터베이스 준비」, 3.4 「JPA」의 H2 사용 설명 | 메모리 DB에 테이블과 샘플 데이터를 자동으로 준비합니다. |

## 실행 방법

1. 다른 웹 프로젝트가 8080 포트를 사용하고 있다면 종료합니다.
2. `DemoApplication`을 실행합니다. 외부 DB나 환경변수는 필요하지 않습니다.
3. <http://localhost:8080/members>에서 회원 실습을 시작합니다.
4. <http://localhost:8080/members/new-validation>에서 회원 등록의 검증 버전을 실습합니다.
5. <http://localhost:8080/articles>에서 게시글 목록·상세와 페이징을 확인합니다.

## 실습 순서

1. 회원 목록에서 이름을 눌러 상세 화면을 확인합니다.
2. 정혁 / `JungHyuk@campus.com` / 10살로 회원을 등록합니다.
3. 등록한 회원의 이름이나 나이를 수정한 뒤 상세 화면에서 확인합니다.
4. 새로 등록한 회원을 삭제합니다. 게시글이 있는 작성자는 삭제할 수 없으며 안내 메시지가 표시됩니다.
5. `/members/new-validation`에서 빈 이름·이메일, 잘못된 이메일, 음수 나이, 기존 회원과 같은 이메일로 등록해 오류 메시지를 확인합니다.
6. 검증 버전에서 정혁 / `JungHyuk@campus.com` / 10살로 등록하고 회원 목록에서 확인합니다.
7. 게시글 제목을 눌러 본문과 작성자를 확인하고, 이전·다음·페이지 번호로 이동합니다.
8. 없는 회원·게시글 조회와 중복 이메일 등록으로 공통 오류 화면과 HTTP 상태 코드를 확인합니다.

## 회원 등록의 두 버전

| 구분 | 기본 등록 | 검증을 추가한 등록 |
|---|---|---|
| 화면 요청 | `GET /members/new` | `GET /members/new-validation` |
| 저장 요청 | `POST /members` | `POST /members/validation` |
| Controller | `MemberController.add()` | `MemberController.addValidation()` |
| 입력 객체 | `Member` 엔티티 | `MemberForm` |
| 처리 흐름 | 입력한 엔티티를 저장 | 입력값을 검증한 뒤 엔티티로 옮겨 저장 |

MemberController에서 기본 등록 메서드 다음에 검증 버전 메서드를 배치했습니다. 두 버전 모두 등록 후 회원 목록으로 이동합니다. 검증 오류가 발생하면 검증 버전의 입력 화면에서 입력값과 항목별 메시지를 다시 보여 줍니다.
이메일 UNIQUE 같은 DB 제약은 두 경로에 모두 적용됩니다.

회원 4명과 게시글 100개가 준비됩니다. 윤서준·윤광철·공미영·김도윤이 순서대로 25개씩 작성하며, 목록은 10개씩 조회합니다.
서버의 페이지 번호는 0부터 시작하고 화면에는 1부터 표시합니다.

## 공통 오류 처리

[GlobalExceptionHandler](src/main/java/com/example/demo/controller/GlobalExceptionHandler.java)의 `@ControllerAdvice`로 여러 Controller의 예외를 모아 처리하고, [error.html](src/main/resources/templates/error.html)을 표시합니다.
`@ExceptionHandler`는 처리할 예외를 지정하고, `@ResponseStatus`는 실제 HTTP 응답 상태를 설정합니다. Model의 `status`는 화면에 보여 줄 값입니다.

| 상황 | 예외 | HTTP 상태 | 화면 안내 |
|---|---|---|---|
| 없는 회원·게시글 조회 | `NoSuchElementException` | 404 | 조회할 수 없는 대상을 안내합니다. |
| 중복 이메일 등 DB 제약 위반 | `DataIntegrityViolationException` | 409 | 데이터 제약을 확인하도록 안내합니다. |
| 예상하지 못한 오류 | `Exception` | 500 | 공통 오류 메시지를 표시합니다. |

- `/members/99999`, `/articles/99999`처럼 존재하지 않는 ID를 조회해 404를 확인합니다.
- 기본 회원 등록 화면에서 `SeojunYoon@campus.com`처럼 이미 있는 이메일을 등록해 409를 확인합니다.
- 브라우저 개발자 도구의 Network에서 실제 HTTP 상태 코드를 확인합니다.
- DB 오류와 예상하지 못한 오류의 상세 원인은 서버 로그로 확인합니다.
- 회원 등록 검증 버전의 입력 검증은 `BindingResult`로 처리하며, 오류가 있으면 입력 화면에서 입력값과 항목별 메시지를 다시 보여 줍니다.

## H2 확인

JDBC·MyBatis·JPA·REST와 동일한 SQL 파일을 `src/main/resources`에 둡니다.
앱을 시작하면 `schema.sql`로 테이블을 생성한 뒤 `data.sql`로 데이터를 넣습니다. `AUTO_INCREMENT`는 H2 기본 모드에서도 지원합니다.
앱을 종료하면 메모리 DB가 사라지며 다시 실행하면 샘플 데이터로 시작합니다.

각 회원 ID는 이메일로 한 번씩 조회해 세션 변수에 저장합니다. MariaDB/MySQL과 H2에서 지원하며 현재 연결에서만 유지되므로 `SET`과 `INSERT`를 같은 연결에서 실행합니다.

<http://localhost:8080/h2-console>에서 DB를 확인할 수 있습니다.
JDBC URL에는 실행 로그의 `jdbc:h2:mem:...` 주소를 그대로 입력합니다. 자동 생성되는 주소는 실행할 때마다 달라질 수 있습니다.
사용자 이름은 `sa`, 비밀번호는 비워 둡니다.

[전체 강의 안내](../README.md) · [이전: spring-thymeleaf](../spring-thymeleaf/README.md)
