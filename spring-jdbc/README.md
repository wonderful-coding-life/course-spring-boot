# spring-jdbc

SQL을 직접 실행하고 조회 결과를 자바 객체로 옮기는 JDBC의 기본 흐름을 익힙니다.
교재는 **『이것이 스프링 부트다 with 자바』**의 3장 「데이터베이스 연동하기」를 참고합니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| 회원 테이블과 데이터 준비 | [schema.sql](sql/schema.sql), [data.sql](sql/data.sql) | 3.1.1 「데이터베이스 준비」 | 외부 MariaDB에 회원 테이블과 회원 4명을 준비합니다. |
| 조회 결과를 담는 객체 | [Member](src/main/java/com/example/demo/model/Member.java) | 3.1.2 「데이터베이스 사용」 | ResultSet의 컬럼을 회원 객체의 필드로 옮깁니다. |
| DB 연결과 전체 조회 | [MemberRepository](src/main/java/com/example/demo/repository/MemberRepository.java) | 3.1.2 「데이터베이스 사용」 | `DriverManager`, `Connection`, `Statement`, `ResultSet`을 사용합니다. |
| 파라미터가 있는 조회 | [MemberRepository](src/main/java/com/example/demo/repository/MemberRepository.java) | 3.1.2 「데이터베이스 사용」 | `PreparedStatement`의 `?`에 회원 ID를 넣습니다. |
| Repository 호출과 결과 출력 | [DemoApplication](src/main/java/com/example/demo/DemoApplication.java) | 3.1.2 「데이터베이스 사용」 | 애플리케이션에서 조회 메서드를 호출하고 로그로 확인합니다. |

## 실행 방법

1. `DB_URL`, `DB_USER`, `DB_PASS` 환경변수를 준비합니다.
2. [schema.sql](sql/schema.sql) 다음 [data.sql](sql/data.sql)을 실행합니다.
3. `DemoApplication`을 실행합니다. 현재 단건 조회 예제의 ID는 `1L`이므로 DB에서 실제 부여된 ID와 맞춰 확인합니다.

전체 조회와 ID 조회를 실습합니다. JDBC에서 직접 작성한 연결·SQL·객체 매핑 코드가 다음 MyBatis 실습에서 어떻게 줄어드는지 비교합니다.

[전체 강의 안내](../README.md) · [이전: spring-core](../spring-core/README.md) · [다음: spring-mybatis](../spring-mybatis/README.md)
