# spring-jpa

회원 CRUD와 검색을 익힌 뒤, 게시글의 작성자 관계와 페이징으로 확장합니다.
교재는 **『이것이 스프링 부트다 with 자바』**의 3.4 「JPA」를 참고합니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| 엔티티와 테이블·컬럼 매핑 | [Member](src/main/java/com/example/demo/entity/Member.java), [Article](src/main/java/com/example/demo/entity/Article.java) | 3.4.1 「엔티티 객체 매핑」 | `@Entity`, `@Table`, `@Column`, `@Id`, `IDENTITY`를 사용합니다. |
| 기본 CRUD와 이메일 중복 | [MemberRepository](src/main/java/com/example/demo/repository/MemberRepository.java), [MemberRepositoryTests](src/test/java/com/example/demo/repository/MemberRepositoryTests.java) | 3.4.2 「리포지터리 작성」 | 상속 메서드로 CRUD를 실행하고, 중복 이메일은 DB의 UNIQUE 제약으로 확인합니다. |
| 메서드 이름 쿼리 | [MemberRepository](src/main/java/com/example/demo/repository/MemberRepository.java), [MemberRepositoryTests](src/test/java/com/example/demo/repository/MemberRepositoryTests.java) | 3.4.2 「리포지터리 작성」의 「메서드 이름으로 쿼리하기」 | 이름·이메일·부분 문자열·나이·복합 조건과 존재 여부를 조회합니다. |
| 같은 회원 객체의 재사용 | [MemberRepositoryTests](src/test/java/com/example/demo/repository/MemberRepositoryTests.java) | 3.4.3 「영속성 컨텍스트」 | ID와 이메일로 조회한 결과를 `==`로 비교해 동일성을 확인합니다. |
| 게시글과 작성자 연결 | [Article](src/main/java/com/example/demo/entity/Article.java), [ArticleRepositoryTests](src/test/java/com/example/demo/repository/ArticleRepositoryTests.java) | 3.4.4 「관계 매핑」 | 단방향 `@ManyToOne`, `@JoinColumn`으로 저장·조회합니다. |
| 제목·작성자로 게시글 검색 | [ArticleRepository](src/main/java/com/example/demo/repository/ArticleRepository.java), [ArticleRepositoryTests](src/test/java/com/example/demo/repository/ArticleRepositoryTests.java) | 3.4.2 「리포지터리 작성」, 3.4.4 「관계 매핑」 | 제목의 '동네' 검색과 `findByMember()`를 실습합니다. 검색 자체에는 페이징을 섞지 않습니다. |
| 전체 게시글 페이징 | [ArticleRepositoryTests](src/test/java/com/example/demo/repository/ArticleRepositoryTests.java) | 3.4.2 「리포지터리 작성」의 정렬·페이징 설명 | `PageRequest`, `Sort`, `Page`의 목록·전체 개수·페이지 수를 확인합니다. |
| 생성일·수정일 자동 설정 | [Article](src/main/java/com/example/demo/entity/Article.java) | 3.4.1 「엔티티 객체 매핑」의 「리스너」 | `@PrePersist`, `@PreUpdate`로 생성일·수정일을 설정합니다. |
| SQL과 실제 파라미터 확인 | [application.properties](src/main/resources/application.properties) | 3.4 「JPA」의 SQL 로그 설정, 2.2.3 「로그 출력」 | SQL 출력·포맷과 바인딩 TRACE 로그를 확인합니다. |

## 실행 방법

- 외부 MariaDB와 `DB_URL`, `DB_USER`, `DB_PASS`를 사용합니다. 스키마·제약은 [schema.sql](sql/schema.sql)로 준비하고 [data.sql](sql/data.sql)로 데이터를 넣습니다.
- 회원 4명과 게시글 100개를 사용하며, 회원별 게시글은 25개입니다. 회원 ID는 SQL에서 이메일로 조회하므로 고정 ID를 가정하지 않습니다.
- 두 실습 클래스의 `@Test`를 개별 실행하고 한글 로그를 확인합니다. 변경 내용은 테스트 후 롤백됩니다. 자동 검증 문법은 REST 모듈에서 배웁니다.
- 게시글 저장에서는 직접 만든 글을 사용하고, 검색·관계 조회·페이징에서는 기존 데이터를 사용합니다.

[전체 강의 안내](../README.md) · [이전: spring-mybatis](../spring-mybatis/README.md) · [다음: spring-rest](../spring-rest/README.md)
