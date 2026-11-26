# spring-rest

Member로 기본 REST API를 익힌 뒤 Article로 Service·DTO·페이징을 설명합니다.
이후 일괄 가입으로 트랜잭션을 설명하고, 회원 Repository로 테스트 기본 문법을 익힌 뒤 게시글 단건 조회로 단위·통합 테스트를 실습합니다.
교재는 **『이것이 스프링 부트다 with 자바』**의 4장 「RESTful API 작성하기」를 참고합니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| 엔티티·Repository 준비 | [Member](src/main/java/com/example/demo/entity/Member.java), [Article](src/main/java/com/example/demo/entity/Article.java), [MemberRepository](src/main/java/com/example/demo/repository/MemberRepository.java), [ArticleRepository](src/main/java/com/example/demo/repository/ArticleRepository.java) | 4.2.1 「프로젝트 생성 및 JPA 리포지터리 작성」, 복습은 3.4 「JPA」 | JPA 모듈의 엔티티를 재사용하고 Repository는 필요한 메서드만 선언합니다. |
| 회원 CRUD와 부분 수정 | [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [members.http](http/members.http) | 4.1.2 「RESTful API 설계 원칙」, 4.2.2 「회원 관리 RESTful API 작성」 | Member 엔티티를 직접 받고 반환합니다. GET·POST·PUT·PATCH·DELETE를 실습합니다. |
| URL과 요청 JSON의 값 받기 | [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [members.http](http/members.http) | 4.2.2 「회원 관리 RESTful API 작성」, 4.2.4 「게시글 RESTful API 구현」 | `@PathVariable`, `@RequestBody`와 이름 검색의 `@RequestParam`을 비교합니다. `GET /members`에서 name을 생략하면 전체 회원을, 지정하면 이름이 일치하는 회원 목록을 조회합니다. |
| HTTP 헤더와 JSON·XML | [build.gradle](build.gradle), [members-xml.http](http/members-xml.http) | 4.1 「RESTful API란 무엇인가?」, 4.2.2 「회원 관리 RESTful API 작성」 | `Content-Type`과 `Accept`를 비교하고 같은 회원 API에서 XML 등록·단건 조회를 실습합니다. |
| Service와 요청·응답 DTO | [ArticleController](src/main/java/com/example/demo/controller/ArticleController.java), [ArticleService](src/main/java/com/example/demo/service/ArticleService.java), [요청 DTO](src/main/java/com/example/demo/dto/ArticleRequest.java), [응답 DTO](src/main/java/com/example/demo/dto/ArticleResponse.java) | 4.2.3 「서비스 계층 분리」, 4.2.4 「게시글 RESTful API 구현」 | 게시글에 요청·응답 DTO를 사용합니다. 응답에는 `writerName`을 포함합니다. |
| 게시글 CRUD·페이징 | [ArticleController](src/main/java/com/example/demo/controller/ArticleController.java), [ArticleService](src/main/java/com/example/demo/service/ArticleService.java), [articles.http](http/articles.http) | 4.2.4 「게시글 RESTful API 구현」 | `Pageable`과 `Page<ArticleResponse>`, `page.map()`으로 목록을 응답합니다. |
| 일괄 가입과 롤백 | [MemberService](src/main/java/com/example/demo/service/MemberService.java), [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [members-batch.http](http/members-batch.http) | 4.2.5 「트랜잭션 처리」 | DTO 없이 회원 배열을 받습니다. 정혁 등록 뒤 중복 이메일로 실패할 때 전체 롤백을 확인합니다. |
| 테스트 기본 문법과 자동 검증 | [MemberRepositoryTests](src/test/java/com/example/demo/repository/MemberRepositoryTests.java), [테스트 DB 설정](src/test/resources/application.properties) | 4.3.1 「테스트 클래스 작성」 | H2에 회원 4명을 준비하고 삭제하며 `@SpringBootTest`·`@Test`·`@BeforeEach`·`@AfterEach`·`@DisplayName`·`@Disabled`와 AssertJ 검증 문법을 익힙니다. |
| HTTP 통합 테스트 | [ArticleControllerIntegrationTests](src/test/java/com/example/demo/controller/ArticleControllerIntegrationTests.java) | 4.3.2 「통합 테스트」 | H2에 회원·게시글을 직접 준비하고 MockMvc로 상태 코드·제목·작성자 이름을 검증합니다. 종료 후 게시글과 회원 순서로 삭제합니다. |
| Service 단위 테스트 | [ArticleServiceTests](src/test/java/com/example/demo/service/ArticleServiceTests.java) | 4.3.3 「단위 테스트」 | `@Mock`·`@InjectMocks`·`MockitoExtension`으로 스프링·DB 없이 DTO 변환을 검증합니다. |
| 오류 메시지와 테스트 실행 | [application.properties](src/main/resources/application.properties), [테스트 DB 설정](src/test/resources/application.properties), [build.gradle](build.gradle) | 4.2.2 「회원 관리 RESTful API 작성」, 4.3 「테스트」 | 앱은 MariaDB를, Spring 테스트는 H2를 사용합니다. 기본 test·build에 통합 테스트도 포함하며, `@Disabled`가 붙은 추가 실습만 건너뜁니다. |

## 실행 순서

1. MariaDB 연결 환경변수 `DB_URL`, `DB_USER`, `DB_PASS`를 준비합니다.
2. 빈 DB에 [schema.sql](sql/schema.sql), [data.sql](sql/data.sql)을 순서대로 실행합니다. 앞 모듈에서 공통 SQL로 준비한 DB를 재사용한다면 `data.sql`을 다시 실행하지 않습니다.
3. `DemoApplication`을 실행한 뒤 [members.http](http/members.http)로 회원 API를 실습합니다.
4. 회원 삭제까지 마쳤다면 [members-xml.http](http/members-xml.http)로 XML 등록·조회를 실행합니다. XML 등록의 JSON 응답에서 ID를 저장하고, 조회는 XML로 받습니다. 마지막 삭제까지 마칩니다.
5. [articles.http](http/articles.http)로 게시글 API를 실습합니다. 응답에서 실제 ID를 저장해 다음 요청에 재사용합니다.
6. [members-batch.http](http/members-batch.http)는 정혁이 아직 등록되지 않은 상태에서 실행합니다. 중복 이메일 실패 후 정혁의 가입도 취소되는지 확인합니다.
7. MemberRepositoryTests로 기본 문법을 익힌 뒤 단위 테스트와 HTTP 통합 테스트를 실행합니다. 테스트는 앱을 실행하거나 MariaDB를 준비하지 않아도 됩니다.

기본 JSON 변환에 XML 변환을 더하기 위해 Jackson 3용 `tools.jackson.dataformat:jackson-dataformat-xml`을 사용합니다. Gradle을 다시 불러오고 앱을 재시작합니다. 요청 `Content-Type`은 보내는 본문의 형식, `Accept`는 받을 응답 형식입니다. HTTP 메시지 구조와 실행 결과는 [실습 교재 3장](../practice-book/03-restful-api.md)에 정리했습니다.

`ArticleServiceTests`는 스프링 컨테이너와 DB 없이 Mockito로 실행합니다. `MemberRepositoryTests`와 `ArticleControllerIntegrationTests`는 테스트용 H2를 사용하고 필요한 데이터를 직접 준비·정리합니다. H2를 사용해도 실제 계층을 연결하므로 통합 테스트이며, MariaDB 고유의 SQL이나 제약 조건까지 검증하는 것은 아닙니다.
Gradle의 `test`·`build`는 태그로 제외하는 테스트 없이 실행합니다. 모듈 폴더에서 `./gradlew.bat test`를 실행하면 5개 성공과 `@Disabled` 추가 실습 1개 건너뜀을 확인할 수 있습니다. 테스트 메서드 이름은 testXXX 형식을 사용합니다.

## 공통 SQL

JDBC·MyBatis·JPA·MVC와 동일한 SQL 파일을 사용합니다. 회원 4명과 게시글 100개를 준비하며, 게시글은 윤서준·윤광철·공미영·김도윤이 순서대로 25개씩 작성합니다.

각 회원 ID는 이메일로 한 번씩 조회해 세션 변수에 저장합니다. MariaDB/MySQL과 H2에서 지원하며 현재 연결에서만 유지되므로 `SET`과 `INSERT`를 같은 연결에서 실행합니다.
게시글 작성자는 외래 키로 연결되어 있으므로 회원 삭제 API 실습은 새로 등록한 회원으로 진행합니다.

[전체 강의 안내](../README.md) · [이전: spring-jpa](../spring-jpa/README.md)
