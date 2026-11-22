# 2장 데이터베이스 연동

> **본 교재 참고**  
> 3장 「데이터베이스 연동하기」의 3.1 「JDBC」, 3.3 「MyBatis」, 3.4 「JPA」

회원 정보를 자바 객체에만 보관하면 프로그램이 종료될 때 그 값도 사라집니다. 관계형 데이터베이스는 데이터를 테이블에 저장해 프로그램을 다시 실행한 뒤에도 사용할 수 있게 합니다. 회원 테이블의 한 행은 회원 한 명을 나타내고, 이름·이메일·나이 같은 속성은 컬럼으로 구분합니다. 자바 프로그램은 SQL을 실행해 이 데이터를 조회하거나 변경합니다.

CRUD는 생성(Create), 조회(Read), 수정(Update), 삭제(Delete)의 네 가지 기본 작업입니다. 데이터를 저장하고 활용하려면 자바 프로그램에서 이러한 작업을 데이터베이스에 요청해야 합니다.

자바 프로그램에서 관계형 데이터베이스와 연동하는 방법은 다양합니다. SQL을 직접 실행하는 JDBC부터 객체의 저장과 조회를 지원하는 Spring Data JPA까지, 개발자가 작성하는 코드와 데이터 처리 방식에 따라 다음과 같은 기술을 사용할 수 있습니다.

- **JDBC**는 자바에서 관계형 데이터베이스에 접근하기 위한 표준 API입니다. 개발자가 SQL을 작성하고, 데이터베이스 연결과 SQL 실행, 조회 결과를 자바 객체로 변환하는 코드를 직접 작성합니다.
- **Spring JDBC**는 JDBC를 사용할 때 반복해서 작성하는 코드를 줄여 주는 스프링의 데이터 접근 기능입니다. `JdbcTemplate` 등을 사용하면 연결과 자원 정리, 예외 처리의 상당 부분을 맡길 수 있으며, 개발자는 SQL과 조회 결과를 객체로 변환하는 코드에 집중할 수 있습니다.
- **Spring Data JDBC**는 객체와 테이블의 매핑 정보를 바탕으로 JDBC를 통한 데이터 저장과 조회를 지원합니다. 데이터 접근 기능을 정의하는 Repository 인터페이스를 작성하면 기본 CRUD 기능을 제공하므로, 기본 작업마다 SQL과 실행 코드를 직접 작성하는 수고를 줄일 수 있습니다.
- **MyBatis**는 개발자가 작성한 SQL과 자바 메서드를 연결하고, 조회 결과를 객체로 변환해 주는 SQL 매핑 프레임워크입니다. SQL은 XML이나 애너테이션으로 직접 작성하면서, JDBC의 반복적인 실행 코드와 결과 변환 처리를 줄일 수 있습니다.
- **Spring Data JPA**는 객체와 관계형 데이터베이스를 매핑하는 표준인 JPA를 Repository 방식으로 편리하게 사용하도록 지원합니다. 개발자는 엔티티의 매핑 정보와 Repository 인터페이스를 작성하며, Hibernate 같은 JPA 구현체가 매핑 정보를 바탕으로 필요한 SQL을 생성하고 실행합니다.

이 장에서는 JDBC, MyBatis, Spring Data JPA를 사용해 같은 회원 데이터의 생성·조회·수정·삭제를 실습합니다. JDBC로 데이터 처리의 기본 흐름을 익힌 뒤, MyBatis와 Spring Data JPA를 사용하면서 직접 작성하는 코드가 어떻게 달라지는지 살펴봅니다.

| 학습 단계 | 개발자가 주로 작성하는 것 | 프레임워크에 맡기는 것 |
|---|---|---|
| JDBC | 연결, SQL 실행, 조회 결과의 객체 변환 | JDBC 드라이버를 통한 DB 통신 |
| MyBatis | SQL과 Mapper 인터페이스·매핑 | SQL 실행과 결과 매핑 지원 |
| Spring Data JPA | 엔티티와 Repository | 기본 CRUD 구현과 JPA 구현체를 통한 SQL 생성·실행 |

**이 장의 실습 순서**

1. 2.1 JDBC
2. 2.2 MyBatis
3. 2.3 JPA

공통 준비를 마친 뒤 JDBC, MyBatis, JPA 순서로 데이터베이스 연동을 실습합니다.

**학습 전 준비**

> **본 교재 참고**  
> 3.1.1 「데이터베이스 준비」

이 장의 실행과 테스트는 외부 MariaDB를 사용합니다. DB 도구에서 사용할 데이터베이스와 접속 계정을 준비합니다. 계정에 테이블 생성과 조회·등록·수정·삭제 권한이 필요합니다. IDE에서 실행할 때는 실행 설정의 환경변수에 다음 값을 지정합니다.

| 이름 | 지정할 값의 예 |
|---|---|
| `DB_URL` | `jdbc:mariadb://localhost:3306/lecture` |
| `DB_USER` | 실습 DB 사용자 이름 |
| `DB_PASS` | 해당 사용자의 비밀번호 |

접속 URL은 어느 DB 서버의 어떤 데이터베이스를 사용할지 지정합니다. 위 예시의 localhost는 현재 컴퓨터, 3306은 DB 서버의 포트, lecture는 사용할 데이터베이스 이름입니다. 강의 환경에서 준비한 실제 주소와 계정으로 설정합니다.

환경변수는 실행되는 프로그램에 전달하는 설정값입니다. 애플리케이션과 테스트의 실행 설정에 각각 적용해야 합니다. MyBatis·JPA·REST에서는 application.properties의 `${DB_URL}` 같은 표현식으로 값을 읽고, JDBC 예제에서는 `System.getenv()`로 직접 읽습니다.

**공통 스키마 준비**

빈 DB에서 schema.sql(`spring-jpa/sql/schema.sql`)을 먼저 실행합니다. JDBC·MyBatis·JPA·REST·MVC의 해당 SQL 파일 내용은 현재 동일합니다. JDBC·MyBatis에서는 member만 실습하고, article은 JPA 이후에 사용합니다.

[schema.sql](../spring-jpa/sql/schema.sql)

```sql
-- MariaDB/MySQL과 H2에서 공통으로 사용하는 실습 스키마이다.
-- 회원 테이블을 먼저 만든 뒤, 회원을 참조하는 게시글 테이블을 만든다.
CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    email VARCHAR(256) NOT NULL UNIQUE,
    age INTEGER
);

CREATE TABLE IF NOT EXISTS article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(256),
    description VARCHAR(4096),
    created TIMESTAMP,
    updated TIMESTAMP,
    member_id BIGINT,
    FOREIGN KEY (member_id) REFERENCES member(id)
);
```

- `PRIMARY KEY`는 각 행을 구별하는 기본 키입니다.
- `AUTO_INCREMENT`는 새 행의 ID를 DB가 부여하도록 합니다. 직접 ID를 정해 넣을 필요가 없습니다.
- `NOT NULL`은 필수값, `UNIQUE`는 중복 불가 조건입니다. 같은 이메일로 회원을 두 번 등록할 수 없습니다.
- `article.member_id`는 작성자 회원의 ID를 가리키는 외래 키입니다. 참조하는 게시글이 남아 있는 회원은 바로 삭제할 수 없습니다.
- `IF NOT EXISTS`는 없는 테이블만 생성합니다. 이미 있는 컬럼·제약을 변경하지는 않습니다.

**기본 키와 외래 키로 작성자 연결하기**

회원의 id는 회원을 구별하는 기본 키입니다. 게시글의 member_id는 그 회원을 가리키는 외래 키입니다. 다음 표는 두 테이블이 연결되는 방식을 설명하기 위한 예시이며, 숫자는 실제 실습 ID를 지정하는 값이 아닙니다.

| member.id | 이름 |
|---|---|
| 12 | 윤서준 |
| 27 | 윤광철 |

| article.id | 제목 | member_id |
|---|---|---|
| 201 | 비 오는 날 창가에서 읽은 책 | 12 |
| 202 | 새벽 시장에서 장을 봤어요 | 12 |

두 게시글의 member_id가 모두 12이므로 작성자는 윤서준입니다. 게시글마다 회원 이름을 반복해서 저장하는 대신 회원 ID로 연결합니다. 이 관계에서 회원 한 명은 여러 게시글의 작성자가 될 수 있습니다. 실제 코드에서도 이메일로 회원을 찾은 뒤 그 회원의 ID를 사용합니다.

이메일의 UNIQUE 제약은 중복 가입을 막고, 기본 키는 각 회원을 식별합니다. 외래 키는 존재하는 회원을 작성자로 연결하도록 하며, 게시글이 참조하는 회원을 바로 삭제하지 못하게 합니다.

**샘플 데이터 실행 순서**

data.sql 전체 파일(`spring-jpa/sql/data.sql`)을 UTF-8로 열어 같은 연결에서 한 번 실행합니다. 다음은 파일 중 회원 등록 부분입니다.

[data.sql — 일부 발췌](../spring-jpa/sql/data.sql)

```sql
INSERT INTO member(name, email, age) VALUES('윤서준', 'SeojunYoon@campus.com', 10);
INSERT INTO member(name, email, age) VALUES('윤광철', 'KwangcheolYoon@campus.com', 43);
INSERT INTO member(name, email, age) VALUES('공미영', 'MiyeongKong@campus.com', 23);
INSERT INTO member(name, email, age) VALUES('김도윤', 'DoyunKim@campus.com', 10);
```

회원 샘플은 다음과 같습니다.

| 이름 | 이메일 | 나이 |
|---|---|---|
| 윤서준 | SeojunYoon@campus.com | 10 |
| 윤광철 | KwangcheolYoon@campus.com | 43 |
| 공미영 | MiyeongKong@campus.com | 23 |
| 김도윤 | DoyunKim@campus.com | 10 |

파일은 회원 이메일로 실제 ID를 한 번씩 조회해 변수에 넣습니다.

[data.sql — 일부 발췌](../spring-jpa/sql/data.sql)

```sql
-- 회원 4명이 먼저 등록되어 있어야 한다. 이메일로 실제 회원 ID를 한 번씩 조회한다.
-- @로 시작하는 세션 변수는 MariaDB/MySQL과 H2에서 지원하며, 현재 연결에서만 유지되고 연결 종료 시 사라진다.
-- 아래 SET과 INSERT는 같은 DB 연결에서 실행한다.
SET @seojun_id = (SELECT id FROM member WHERE email = 'SeojunYoon@campus.com');
SET @kwangcheol_id = (SELECT id FROM member WHERE email = 'KwangcheolYoon@campus.com');
SET @miyeong_id = (SELECT id FROM member WHERE email = 'MiyeongKong@campus.com');
SET @doyun_id = (SELECT id FROM member WHERE email = 'DoyunKim@campus.com');
```

이어서 회원별 25개씩, 총 100개의 게시글을 등록합니다. `@seojun_id` 같은 세션 변수는 현재 DB 연결에서 조회한 회원 ID를 잠시 보관합니다. SET과 INSERT를 같은 연결에서 실행해야 게시글에 올바른 작성자 ID가 들어갑니다. 검색과 관계 조회 실습에서는 이 연결을 이용해 결과를 확인합니다.

**실행과 예상 결과**

DB 도구에서 다음 SQL을 실행해 준비 상태를 확인할 수 있습니다. 이 조회문은 준비 결과를 확인하기 위한 학습용 SQL입니다.

```sql
SELECT COUNT(*) FROM member;
SELECT COUNT(*) FROM article;

SELECT m.name, COUNT(*) AS article_count
FROM member m JOIN article a ON a.member_id = m.id
GROUP BY m.id, m.name;
```

처음 두 조회의 결과는 각각 회원 4명과 게시글 100개입니다. 마지막 조회는 작성자별 게시글 수를 확인하기 위한 SQL이며, 다음과 같은 결과를 얻습니다. 행의 표시 순서는 달라질 수 있습니다.

| name | article_count |
|---|---|
| 윤서준 | 25 |
| 윤광철 | 25 |
| 공미영 | 25 |
| 김도윤 | 25 |

같은 MariaDB를 다음 모듈에서 이어 쓰면 data.sql을 다시 실행하지 않습니다. 다시 넣으면 이메일 중복으로 실패하거나 게시글이 추가될 수 있습니다. 실습은 각 기능을 실행하기 전에 필요한 데이터가 있는 상태에서 시작합니다. 새 회원은 정혁 / `JungHyuk@campus.com` / 10살을 사용합니다.
