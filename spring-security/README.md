# spring-security

상품 관리 화면으로 인증과 권한을 실습합니다. 이메일로 로그인하고 일반 회원과 관리자의 접근 범위를 비교합니다.
각 화면은 기본 HTML의 제목·표·입력 폼·링크로 구성하고, 필요한 링크와 폼을 직접 작성합니다.
상품 목록 상단에는 로그인 이메일과 그 옆의 로그아웃 링크를 표시합니다.
교재는 **『이것이 스프링 부트다 with 자바』**의 5.3 「스프링 시큐리티」를 참고합니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| 보안 설정과 접근 규칙 | [SecurityConfig](src/main/java/com/example/demo/config/SecurityConfig.java), [build.gradle](build.gradle) | 5.3.1 「스프링 시큐리티 설정」 | 요청이 Controller에 도달하기 전에 로그인과 권한을 확인합니다. |
| 이메일 로그인 | [SecurityConfig](src/main/java/com/example/demo/config/SecurityConfig.java), [MemberRepository](src/main/java/com/example/demo/repository/MemberRepository.java), [Member](src/main/java/com/example/demo/entity/Member.java) | 5.3.2 「인증」 | `UserDetailsService`가 이메일로 회원을 조회하고 로그인 정보를 제공합니다. |
| 비밀번호 해시 | [PasswordEncoderTests](src/test/java/com/example/demo/PasswordEncoderTests.java), [data.sql](src/main/resources/data.sql) | 5.3.2 「인증」 | `encode()`와 `matches()` 결과를 로그로 확인합니다. DB에는 BCrypt 해시를 저장합니다. |
| 일반 회원·관리자 권한 | [SecurityConfig](src/main/java/com/example/demo/config/SecurityConfig.java), [ProductController](src/main/java/com/example/demo/controller/ProductController.java) | 5.3.3 「권한」 | 목록은 로그인 사용자, 등록·수정·삭제는 관리자만 허용합니다. |
| 로그인 정보와 메뉴 표시 | [HomeController](src/main/java/com/example/demo/controller/HomeController.java), [home.html](src/main/resources/templates/home.html), [상품 목록](src/main/resources/templates/product-list.html) | 5.3.4 「로그인 사용자 정보 사용」 | `Authentication`, `sec:authentication`, `sec:authorize`로 로그인 정보와 권한별 메뉴를 확인합니다. |
| 로그인·로그아웃 화면 | [login.html](src/main/resources/templates/login.html), [logout.html](src/main/resources/templates/logout.html) | 5.3.6 「로그인/로그아웃 커스텀」 | 직접 만든 로그인 화면과 로그아웃 확인 화면을 사용합니다. 실제 처리는 Security가 담당합니다. |
| CSRF와 POST 폼 | [SecurityConfig](src/main/java/com/example/demo/config/SecurityConfig.java), [상품 등록](src/main/resources/templates/product-add.html), [상품 수정](src/main/resources/templates/product-edit.html) | 5.3.7 「CSRF와 CORS」의 CSRF 설명 | `th:action`을 사용하는 POST 폼의 CSRF 토큰을 확인합니다. |

## 실행 방법

1. 다른 웹 프로젝트가 8080 포트를 사용하고 있다면 종료합니다.
2. `DemoApplication`을 실행합니다. 외부 DB나 환경변수는 필요하지 않습니다.
3. <http://localhost:8080>에서 홈 화면을 엽니다.
4. 일반 회원과 관리자로 번갈아 로그인하며 상품 목록과 관리 기능을 확인합니다.

## 실습 계정

모든 계정의 실습 비밀번호는 **`12345678`**입니다. 이메일을 로그인 ID로 사용합니다.

| 이름 | 로그인 이메일 | DB 권한 |
|---|---|---|
| 윤서준 | `SeojunYoon@campus.com` | `ROLE_USER` |
| 윤광철 | `KwangcheolYoon@campus.com` | `ROLE_ADMIN` |
| 공미영 | `MiyeongKong@campus.com` | `ROLE_USER` |
| 김도윤 | `DoyunKim@campus.com` | `ROLE_USER` |

`hasRole("ADMIN")`은 DB의 `ROLE_ADMIN` 권한을 확인합니다. 이 예제에서는 관리자에게 `ROLE_ADMIN` 하나를 지정합니다.

## 실습 순서

1. 로그인하지 않고 `/products`에 접근하면 로그인 화면으로 이동하는지 확인합니다.
2. 잘못된 비밀번호로 로그인해 한글 실패 메시지를 확인합니다.
3. 윤서준으로 로그인하면 상품 목록으로 이동합니다. 목록 화면의 이메일과 그 옆의 로그아웃 링크를 확인합니다.
4. 상품 등록 메뉴가 보이지 않는 것을 확인한 뒤 `/products/new`를 직접 입력해도 403으로 차단되는지 확인합니다.
5. 상품 목록의 로그아웃 링크로 확인 화면을 열고, 확인 버튼을 눌러 홈으로 돌아갑니다. 윤광철로 로그인해 상품 등록·수정·삭제 후 목록으로 돌아오는지 확인합니다.
6. 로그아웃 후 상품 목록에 다시 접근해 로그인 화면으로 이동하는지 확인합니다.
7. 상품 등록·수정 저장 또는 로그아웃의 POST 폼에서 `_csrf` 숨김 입력값을 확인합니다. 토큰을 제거해 전송하면 403으로 거절됩니다.
8. `PasswordEncoderTests`를 실행해 같은 비밀번호는 `true`, 다른 비밀번호는 `false`가 출력되는지 확인합니다.

관리 링크를 숨기는 것은 화면 처리입니다. 실제 요청 허용 여부는 SecurityConfig에서 검사합니다.
로그인 상태는 세션으로 유지되며 로그아웃하면 해당 세션의 로그인 정보가 제거됩니다.

## 상품 요청과 접근 권한

| 요청 | 동작 | 접근 권한 |
|---|---|---|
| `GET /products` | 상품 목록 조회 | 로그인 사용자 |
| `GET /products/new` | 상품 등록 화면 | 관리자 |
| `POST /products` | 상품 등록 후 목록으로 이동 | 관리자 |
| `GET /products/{id}/edit` | 수정 링크로 상품 수정 화면 열기 | 관리자 |
| `POST /products/{id}/edit` | 수정 내용 저장 후 목록으로 이동 | 관리자 |
| `GET /products/{id}/delete` | 삭제 링크로 상품 삭제 후 목록으로 이동 | 관리자 |

상품 목록의 수정·삭제는 같은 줄의 일반 링크입니다. 삭제 링크에는 JavaScript나 form을 사용하지 않습니다.
등록·수정 내용의 저장은 POST 폼으로 처리합니다. 로그인·로그아웃·등록·수정 저장의 POST 폼에서 CSRF를 실습합니다.

## 로그인 요청의 흐름

- `GET /`: 로그인 링크와 로그인 상태에 따른 상품 관리 메뉴를 보여 줍니다.
- `GET /login`: HomeController가 로그인 화면을 보여 줍니다.
- `POST /login`: Security가 이메일·비밀번호·CSRF 토큰을 확인합니다. Controller에서 로그인 POST 메서드를 만들지 않습니다.
- 로그인 성공: `/products` 상품 목록으로 이동합니다. 실패: `/login?error`로 이동합니다.
- 상품 목록: 로그인 이메일, 로그아웃 링크, 상품 표를 표시합니다. 관리자에게만 등록·수정·삭제 링크가 보입니다.
- 상품 등록·수정·삭제 성공: `/products` 상품 목록으로 돌아갑니다.
- `GET /logout`: HomeController가 로그아웃 확인 화면을 보여 줍니다. 이때 로그인은 유지됩니다.
- `POST /logout`: 확인 화면의 버튼을 누르면 Security가 로그아웃을 처리하고 `/` 홈 화면으로 이동합니다.

로그인 폼의 `email`은 `usernameParameter("email")`과 연결됩니다.
POST 폼에 `th:action`을 사용하면 Thymeleaf가 CSRF 토큰을 자동으로 넣습니다.

## 403 오류 화면

권한이 부족하거나 CSRF 검증에 실패하면 403으로 응답합니다.
Spring Boot가 [error/403.html](src/main/resources/templates/error/403.html)을 자동으로 표시하므로 별도 오류 Controller나 `exceptionHandling()` 설정은 필요하지 않습니다.
오류 화면의 홈 링크로 `/`에 돌아갈 수 있습니다. SecurityConfig에서는 오류 처리를 위한 `/error` 접근을 허용합니다.

## H2와 초기 SQL

[schema.sql](src/main/resources/schema.sql)은 로그인용 `member`와 `product` 테이블을 생성하고, [data.sql](src/main/resources/data.sql)은 회원 4명과 상품 2개를 등록합니다.
상품은 아이폰(1,200,000원), 맥북(950,000원)입니다. 초기화는 SQL로 처리하며 앱을 다시 실행하면 샘플 상태로 시작합니다.

`data.sql`에는 미리 생성한 BCrypt 해시를 넣었습니다. 해시는 복호화하지 않으며, 입력한 비밀번호는 `PasswordEncoder.matches()`로 확인합니다.
같은 비밀번호를 `encode()`해도 매번 다른 해시가 나올 수 있으므로 문자열을 직접 비교하지 않습니다.

[전체 강의 안내](../README.md) · [이전: spring-mvc](../spring-mvc/README.md)
