# 2장 데이터베이스 연동

> **본 교재 참고**  
> 3장 「데이터베이스 연동하기」의 3.1 「JDBC」, 3.3 「MyBatis」, 3.4 「JPA」

회원 정보를 자바 객체에만 저장하면 프로그램이 종료될 때 사라집니다. 데이터베이스는 데이터를 표 형태의 테이블에 보관합니다. 테이블의 한 행은 회원 한 명, 컬럼은 이름·이메일·나이 같은 속성에 해당합니다. 자바 프로그램은 SQL을 실행해 데이터를 조회하고 변경합니다.

CRUD는 생성(Create), 조회(Read), 수정(Update), 삭제(Delete)의 네 가지 기본 작업입니다. 이 장에서는 같은 회원 데이터를 JDBC, MyBatis, JPA로 다루며 자바 코드와 DB 사이를 연결하는 방법을 익힙니다.

| 학습 단계 | 개발자가 주로 작성하는 것 | 프레임워크에 맡기는 것 |
|---|---|---|
| JDBC | 연결, SQL 실행, 조회 결과의 객체 변환 | JDBC 드라이버를 통한 DB 통신 |
| MyBatis | SQL과 Mapper 인터페이스·매핑 | SQL 실행과 결과 매핑 지원 |
| JPA | 엔티티와 Repository | 엔티티 매핑을 바탕으로 한 SQL 생성·실행 |

**이 장의 파일**

- [2.1 JDBC](02-1-jdbc.md)
- [2.2 MyBatis](02-2-mybatis.md)
- [2.3 JPA](02-3-jpa.md)

**학습 전 준비**

> **본 교재 참고**  
> 3.1.1 「데이터베이스 준비」

이 장의 실행과 테스트는 외부 MariaDB를 사용합니다. DB 도구에서 사용할 데이터베이스와 접속 계정을 준비합니다. 계정에 테이블 생성과 조회·등록·수정·삭제 권한이 필요합니다. IDE에서 실행할 때는 실행 설정의 환경변수에 다음 값을 지정합니다.

| 이름 | 지정할 값의 예 |
|---|---|
| `DB_URL` | `jdbc:mariadb://localhost:3306/lecture` |
| `DB_USER` | 실습 DB 사용자 이름 |
| `DB_PASS` | 해당 사용자의 비밀번호 |

이 값은 예시이며 실제 접속 주소와 계정을 사용합니다. 환경변수는 앱뿐 아니라 IDE의 테스트 실행 설정에도 적용되어야 합니다. 실제 비밀번호를 소스에 적지 않습니다. 기본 애플리케이션 properties에 환경변수를 선언한 모듈에서는 스프링이 그 값을 읽고, JDBC 예제는 `System.getenv()`로 직접 읽습니다.

**공통 스키마 준비**

빈 DB에서 [schema.sql](../spring-jpa/sql/schema.sql)을 먼저 실행합니다. JDBC·MyBatis·JPA·REST·MVC의 해당 SQL 파일 내용은 현재 동일합니다. JDBC·MyBatis에서는 member만 실습하고, article은 JPA 이후에 사용합니다.

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

**샘플 데이터 준비**

[data.sql 전체 파일](../spring-jpa/sql/data.sql)을 UTF-8로 열어 같은 연결에서 한 번 실행합니다. 다음은 파일 중 회원 등록 부분입니다.

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

이어서 회원별 25개씩, 총 100개의 게시글을 등록합니다. `@seojun_id` 같은 세션 변수는 MariaDB/MySQL과 H2에서 지원하며 현재 DB 연결에서만 유지됩니다. SET을 실행한 연결과 INSERT를 실행하는 연결이 같아야 합니다. 게시글 제목 100개를 외우지 말고 검색과 관계 조회로 결과를 확인합니다.

**실행과 예상 결과**

DB 도구에서 다음 SQL을 실행해 준비 상태를 확인할 수 있습니다. 이 조회문은 준비 결과를 확인하기 위한 학습용 SQL입니다.

```sql
SELECT COUNT(*) FROM member;
SELECT COUNT(*) FROM article;

SELECT m.name, COUNT(*) AS article_count
FROM member m JOIN article a ON a.member_id = m.id
GROUP BY m.id, m.name;
```

회원은 4명, 게시글은 100개, 회원별 게시글은 25개입니다. 순서를 정하지 않은 SELECT 결과의 행 순서는 가정하지 않습니다.

같은 MariaDB를 다음 모듈에서 이어 쓰면 data.sql을 다시 실행하지 않습니다. 다시 넣으면 이메일 중복으로 실패하거나 게시글이 추가될 수 있습니다. 실습은 각 기능을 실행하기 전에 필요한 데이터가 있는 상태에서 시작합니다. 새 회원은 정혁 / `JungHyuk@campus.com` / 10살을 사용합니다.

[목차](README.md) · [이전: 1장](01-spring-core.md) · [다음: 2.1 JDBC](02-1-jdbc.md)
