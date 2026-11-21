# spring-mybatis

JDBC에서 직접 처리했던 SQL 실행과 결과 매핑을 Mapper와 XML로 분리합니다.
교재는 **『이것이 스프링 부트다 with 자바』**의 3.3 「MyBatis」를 참고합니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| 회원 데이터 준비 | [schema.sql](sql/schema.sql), [data.sql](sql/data.sql) | 3.1.1 「데이터베이스 준비」, 3.3.3 「예제 애플리케이션」 | JDBC와 같은 회원 4명을 사용합니다. |
| Mapper 인터페이스 | [MemberMapper](src/main/java/com/example/demo/mapper/MemberMapper.java) | 3.3.1 「XML 기반 SQL 매핑」 | `@Mapper`가 붙은 인터페이스에 사용할 메서드를 선언합니다. |
| CRUD SQL과 파라미터 | [MemberMapper.xml](src/main/resources/mapper/MemberMapper.xml) | 3.3.1 「XML 기반 SQL 매핑」 | 조회·등록·수정·삭제 SQL과 `#{}` 파라미터를 연결합니다. |
| 조회 결과 매핑과 생성 ID | [Member](src/main/java/com/example/demo/model/Member.java), [MemberMapper.xml](src/main/resources/mapper/MemberMapper.xml) | 3.3.1 「XML 기반 SQL 매핑」 | `resultMap`, `useGeneratedKeys`, `keyProperty`를 확인합니다. |
| 정렬 조건 전달 | [MemberMapper](src/main/java/com/example/demo/mapper/MemberMapper.java), [MemberMapper.xml](src/main/resources/mapper/MemberMapper.xml) | 3.3.1 「XML 기반 SQL 매핑」 | `@Param`과 `${}`로 정렬 조건을 전달합니다. XML의 SQL 인젝션 관련 주석도 함께 확인합니다. |
| DB와 Mapper 연결 설정 | [application.properties](src/main/resources/application.properties) | 3.3.1 「XML 기반 SQL 매핑」 | MariaDB 연결과 Mapper XML 탐색 위치를 지정합니다. |
| 기능별 실행과 결과 확인 | [MemberMapperTests](src/test/java/com/example/demo/mapper/MemberMapperTests.java) | 3.3.3 「예제 애플리케이션」, 실행 문법은 4.3.1 「테스트 클래스 작성」 | 개별 `@Test` 메서드를 실행하고 한글 로그로 결과를 확인합니다. |

## 실행 방법

- MariaDB 연결 환경변수를 준비하고, 회원 데이터가 없다면 `sql/schema.sql`과 `sql/data.sql`을 순서대로 실행합니다. JDBC 실습의 DB를 재사용할 수도 있습니다.
- `MemberMapperTests`에서 원하는 메서드를 하나씩 실행합니다. 등록·조회·수정·삭제, 이메일 조회, 정렬을 독립적으로 실행할 수 있습니다.
- 신규 회원은 정혁 / `JungHyuk@campus.com` / 10살입니다. 데이터 변경은 테스트 종료 후 롤백됩니다.
- 이 단계의 `@Test`는 실습을 실행하기 위한 도구입니다. `assertThat()` 자동 검증 대신 로그로 관찰하며, 검증 문법은 REST 모듈에서 다룹니다.

[전체 강의 안내](../README.md) · [이전: spring-jdbc](../spring-jdbc/README.md) · [다음: spring-jpa](../spring-jpa/README.md)
