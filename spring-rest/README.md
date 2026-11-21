# spring-rest

Member로 기본 REST API를 익힌 뒤 Article로 Service·DTO·페이징을 설명합니다.
이후 일괄 가입으로 트랜잭션을, 게시글 단건 조회로 통합·단위 테스트를 실습합니다.
교재는 **『이것이 스프링 부트다 with 자바』**의 4장 「RESTful API 작성하기」를 참고합니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| 엔티티·Repository 준비 | [Member](src/main/java/com/example/demo/entity/Member.java), [Article](src/main/java/com/example/demo/entity/Article.java), [MemberRepository](src/main/java/com/example/demo/repository/MemberRepository.java), [ArticleRepository](src/main/java/com/example/demo/repository/ArticleRepository.java) | 4.2.1 「프로젝트 생성 및 JPA 리포지터리 작성」, 복습은 3.4 「JPA」 | JPA 모듈의 엔티티를 재사용하고 Repository는 필요한 메서드만 선언합니다. |
| 회원 CRUD와 부분 수정 | [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [members.http](http/members.http) | 4.1.2 「RESTful API 설계 원칙」, 4.2.2 「회원 관리 RESTful API 작성」 | Member 엔티티를 직접 받고 반환합니다. GET·POST·PUT·PATCH·DELETE를 실습합니다. |
| URL과 요청 JSON의 값 받기 | [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [members.http](http/members.http) | 4.2.2 「회원 관리 RESTful API 작성」, 4.2.4 「게시글 RESTful API 구현」 | `@PathVariable`, `@RequestBody`와 이메일 검색의 `@RequestParam`을 비교합니다. |
| Service와 요청·응답 DTO | [ArticleController](src/main/java/com/example/demo/controller/ArticleController.java), [ArticleService](src/main/java/com/example/demo/service/ArticleService.java), [등록 DTO](src/main/java/com/example/demo/dto/ArticleCreateRequest.java), [수정 DTO](src/main/java/com/example/demo/dto/ArticleUpdateRequest.java), [응답 DTO](src/main/java/com/example/demo/dto/ArticleResponse.java) | 4.2.3 「서비스 계층 분리」, 4.2.4 「게시글 RESTful API 구현」 | 게시글에 요청·응답 DTO를 사용합니다. 응답에는 `writerName`을 포함합니다. |
| 게시글 CRUD·페이징 | [ArticleController](src/main/java/com/example/demo/controller/ArticleController.java), [ArticleService](src/main/java/com/example/demo/service/ArticleService.java), [articles.http](http/articles.http) | 4.2.4 「게시글 RESTful API 구현」 | `Pageable`과 `Page<ArticleResponse>`, `page.map()`으로 목록을 응답합니다. |
| 일괄 가입과 롤백 | [MemberService](src/main/java/com/example/demo/service/MemberService.java), [MemberController](src/main/java/com/example/demo/controller/MemberController.java), [members-batch.http](http/members-batch.http) | 4.2.5 「트랜잭션 처리」 | DTO 없이 회원 배열을 받습니다. 정혁 등록 뒤 중복 이메일로 실패할 때 전체 롤백을 확인합니다. |
| 검증 문법과 테스트 이름 | [ArticleServiceTests](src/test/java/com/example/demo/service/ArticleServiceTests.java), [ArticleControllerIntegrationTests](src/test/java/com/example/demo/controller/ArticleControllerIntegrationTests.java) | 4.3.1 「테스트 클래스 작성」 | `assertThat()`을 실제 검증에 사용합니다. 기본 검증 문법과 `@Disabled`는 주석으로, `@DisplayName`은 한글 이름으로 소개합니다. |
| HTTP 통합 테스트 | [ArticleControllerIntegrationTests](src/test/java/com/example/demo/controller/ArticleControllerIntegrationTests.java) | 4.3.2 「통합 테스트」 | MockMvc로 기존 게시글을 조회해 상태 코드·제목·작성자 이름을 검증합니다. |
| Service 단위 테스트 | [ArticleServiceTests](src/test/java/com/example/demo/service/ArticleServiceTests.java) | 4.3.3 「단위 테스트」 | `@Mock`·`@InjectMocks`·`MockitoExtension`으로 스프링·DB 없이 DTO 변환을 검증합니다. |
| 오류 메시지와 테스트 분류 | [application.properties](src/main/resources/application.properties), [통합 테스트](src/test/java/com/example/demo/controller/ArticleControllerIntegrationTests.java), [build.gradle](build.gradle) | 4.2.2 「회원 관리 RESTful API 작성」, 4.3 「테스트」 | `ResponseStatusException`과 오류 메시지 표시를 사용합니다. 통합 테스트는 `integration` 태그로 기본 빌드에서 제외합니다. |

## 실행 순서

1. MariaDB 연결 환경변수 `DB_URL`, `DB_USER`, `DB_PASS`를 준비합니다.
2. [schema.sql](sql/schema.sql), [data.sql](sql/data.sql)을 순서대로 실행합니다. 같은 DB를 앞 모듈과 공유한다면 기존 데이터를 중복 등록하지 않습니다.
3. `DemoApplication`을 실행한 뒤 [members.http](http/members.http)로 회원 API를 실습합니다.
4. [articles.http](http/articles.http)로 게시글 API를 실습합니다. 응답에서 실제 ID를 저장해 다음 요청에 재사용합니다.
5. [members-batch.http](http/members-batch.http)는 정혁이 아직 등록되지 않은 상태에서 실행합니다. 중복 이메일 실패 후 정혁의 가입도 취소되는지 확인합니다.
6. 단위 테스트와 통합 테스트를 각각 실행하며 자동 검증을 확인합니다.

`ArticleServiceTests`는 DB가 필요하지 않습니다. `ArticleControllerIntegrationTests`는 준비된 MariaDB가 필요합니다.
Gradle의 `test`·`build`는 `integration` 태그를 제외합니다. 통합 테스트를 개별 실행할 때 IntelliJ의 실행기를 JUnit으로 사용하면 됩니다. Gradle에 실행을 위임하면 제외 설정이 적용됩니다.

[전체 강의 안내](../README.md) · [이전: spring-jpa](../spring-jpa/README.md)
