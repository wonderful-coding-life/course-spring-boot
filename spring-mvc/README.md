# spring-mvc

Thymeleaf 화면으로 Member 엔티티를 직접 사용하는 등록·조회·수정·삭제를 실습합니다. 이어서 /signup 회원가입에서 MemberForm과 입력 검증을 도입합니다. 이어서 게시글 조회·페이징과 공통 오류 처리를 확인합니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| MVC와 화면 연결 | [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [회원 목록](src/main/resources/templates/member-list.html) | 5.1.1 「웹 애플리케이션 아키텍처와 MVC 패턴」, 5.2.1 「타임리프 프로젝트 생성」 | Controller가 Model에 데이터를 넣고 HTML 화면 이름을 반환합니다. |
| 회원 CRUD | [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [MemberRepository](src/main/java/com/example/demo/repository/MemberRepository.java), [등록 화면](src/main/resources/templates/member-add.html), [수정 화면](src/main/resources/templates/member-edit.html) | 5.4.6 「관리자 기능 구현」 | Member 엔티티를 직접 받아 저장하며 목록·상세·등록·수정·삭제와 리다이렉트를 실습합니다. |
| 회원가입과 입력 검증 | [MemberForm](src/main/java/com/example/demo/form/MemberForm.java), [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [회원가입 화면](src/main/resources/templates/signup.html) | 5.4.4 「사용자 로그인 구현」의 회원가입 폼, 5.4.5 「게시판 웹 화면 구현」의 입력 검증 | `@Valid`, `BindingResult`, `th:errors`로 이름·이메일·비밀번호와 이메일 중복·비밀번호 확인을 검증합니다. |
| 게시글 상세·목록 | [ArticleController](src/main/java/com/example/demo/controller/ArticleController.java), [ArticleService](src/main/java/com/example/demo/service/ArticleService.java), [ArticleResponse](src/main/java/com/example/demo/dto/ArticleResponse.java) | 5.4.3 「모델 객체와 서비스 계층 작성」, 5.4.5 「게시판 웹 화면 구현」 | 작성자 이름을 포함한 DTO를 화면에 전달합니다. |
| 게시글 페이징 | [게시글 목록](src/main/resources/templates/article-list.html) | 5.4.5 「게시판 웹 화면 구현」 | `Pageable`, `Page`와 이전·다음·페이지 번호 링크를 연결합니다. |
| 공통 화면 | [common.html](src/main/resources/templates/common.html) | 5.2.4 「타임리프의 레이아웃 관리」 | 메뉴·페이징·하단 안내를 화면 조각으로 재사용합니다. 페이징에는 페이지 정보와 목록 주소를 전달합니다. |
| H2와 초기 SQL | [설정](src/main/resources/application.properties), [schema.sql](src/main/resources/schema.sql), [data.sql](src/main/resources/data.sql) | 3.1.1 「데이터베이스 준비」, 3.4 「JPA」의 H2 사용 설명 | 메모리 DB에 테이블과 샘플 데이터를 자동으로 준비합니다. |

## 주요 실습 코드

- 회원 처리: `src/main/java/com/example/demo/controller/MemberController.java`
- 입력 폼: `src/main/java/com/example/demo/form/MemberForm.java`
- 등록·수정 화면: `src/main/resources/templates/member-add.html`, `member-edit.html`
- DB 초기화: `src/main/resources/schema.sql`, `data.sql`
- 실습 교재: `practice-book/04-spring-mvc.md`의 4.2~4.6절

## 실행과 실습 순서

1. 다른 8080 포트 앱을 종료하고 DemoApplication을 실행합니다. 외부 DB나 환경변수는 필요하지 않습니다.
2. http://localhost:8080/members 에서 회원 4명을 확인합니다.
3. /members/new에서 정혁 / JungHyuk@campus.com / password123으로 등록합니다. 기본 등록은 입력 검증 없이 Member를 저장합니다.
4. /members/edit?id=실제ID에서 이름을 정혁수로 수정하고, 비밀번호도 변경합니다. ID는 @RequestParam으로 받고 저장 시 hidden으로 전달합니다.
5. 상세 화면의 /members/delete?id=실제ID 링크로 새 회원을 삭제합니다. 게시글이 있는 샘플 회원은 외래 키 제약으로 삭제되지 않으며 409 오류 화면이 표시됩니다.
6. /signup에서 빈 이름·잘못된 이메일·중복 이메일·8자 미만 비밀번호·확인 불일치 오류를 확인합니다.
7. 비밀번호와 확인 값에 모두 password123을 입력하여 정혁을 다시 등록합니다.
8. /articles에서 게시글 목록·상세와 페이징을 확인합니다. 게시글은 100개이며 목록은 10개씩 표시합니다.

기본 등록 화면은 GET /members/new, 저장은 POST /members입니다. 생성·수정은 Member를 직접 받아 save()로 저장하며 별도의 입력 검증을 하지 않습니다. 삭제는 GET /members/delete에서 @RequestParam으로 ID를 받아 조회한 회원을 delete()로 삭제합니다. 수정 폼에는 기존 비밀번호가 표시되고 제출한 값으로 저장합니다. 목록·상세에는 비밀번호를 표시하지 않습니다.

회원가입은 GET·POST /signup과 signup.html로 구성합니다. MemberForm의 @NotBlank·@Email·@Size와 @Valid를 사용하고, BindingResult로 비밀번호 확인 불일치와 이메일 중복 오류도 안내합니다. 확인 값은 DB에 저장하지 않으며 가입 오류 시 비밀번호 입력란은 비워서 표시합니다. 저장 후 redirect:/로 이동하면 홈 컨트롤러가 회원 목록으로 안내합니다.

이번 단계에서는 비밀번호를 문자열 그대로 저장하며 해시는 이후 spring-security에서 적용합니다.

## H2와 공통 오류 처리

앱을 시작할 때 schema.sql과 MVC용 data.sql을 실행합니다. 회원의 age 대신 password 컬럼을 사용하며 샘플 비밀번호는 password123입니다. 앱을 다시 실행하면 회원 4명과 게시글 100개로 초기화됩니다.

http://localhost:8080/h2-console 에서 실행 로그의 jdbc:h2:mem:... 주소로 접속합니다. 사용자 이름은 sa이고 DB 접속 비밀번호는 비어 있습니다.

없는 회원·게시글 조회는 404 화면으로 안내합니다. 회원가입의 입력 오류와 이메일 중복은 폼에 표시하고, 기본 CRUD의 중복 이메일 등 DB 제약 위반은 공통 오류 처리에서 409로 안내합니다. 예상하지 못한 예외는 500으로 처리합니다. 오류 상세 원인은 서버 로그에서 확인합니다.

[전체 강의 안내](../README.md) · [이전: spring-thymeleaf](../spring-thymeleaf/README.md)
