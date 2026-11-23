## 2.2 MyBatis

> **본 교재 참고**  
> 3장 「데이터베이스 연동하기」의 3.3 「MyBatis」

MyBatis는 자바 메서드와 SQL을 연결하는 SQL 매핑 프레임워크입니다. JDBC에서는 SQL을 실행하고 ResultSet을 읽어 회원 객체를 직접 만들었습니다. MyBatis를 사용하면 개발자는 실행할 SQL과 반환할 객체의 구조를 정하고, 반복되는 SQL 실행과 결과 변환은 프레임워크에 맡길 수 있습니다.

이 절에서는 기존 회원 4명을 조회하고 정혁을 등록·수정·삭제합니다. Mapper 메서드를 호출하면 XML의 어떤 SQL이 실행되는지, 그 결과가 어떤 자바 값으로 반환되는지 연결해 읽습니다. 테스트 메서드는 기능을 하나씩 실행하는 도구로 사용하며 결과는 한글 로그로 확인합니다.

**준비**: spring-mybatis를 열고 MariaDB 환경변수를 설정합니다. 공통 SQL로 준비한 DB를 그대로 사용합니다. 정혁이 DB에 남아 있다면 중복 등록이 되지 않도록 실습 전 상태를 확인합니다.

### 2.2.1 Mapper와 XML 연결

> **본 교재 참고**  
> 3.3.1 「XML 기반 SQL 매핑」

**프로젝트 생성과 의존성 선택**

Spring Initializr에서 다음 의존성을 선택하고, 생성한 프로젝트 폴더를 `spring-mybatis`로 준비합니다.

| 의존성 | 역할 |
|---|---|
| MyBatis Framework | Mapper 메서드와 SQL을 연결하고 결과를 객체로 변환합니다. |
| MariaDB Driver | MariaDB에 연결합니다. |
| Lombok | 회원 객체와 로그 코드를 간단히 작성합니다. |

> **스프링 부트 버전 선택 시 주의**
>
> Spring Initializr에서는 선택한 스프링 부트 버전에 따라 `MyBatis Framework`를 선택할 수 없는 경우가 있습니다. MyBatis의 스프링 부트 연동 라이브러리인 MyBatis Spring Boot Starter는 스프링 부트와 별도로 개발·배포되므로, 새로운 스프링 부트 버전의 출시와 이를 지원하는 Starter의 출시 또는 Initializr의 호환 정보 반영 사이에 시차가 생길 수 있습니다. Spring Initializr는 등록된 호환 범위에 따라 의존성 선택을 제한합니다.
>
> 이 경우에는 강의에서 안내한 스프링 부트 버전을 선택해 실습을 진행합니다. 최신 버전을 사용하려면 MyBatis Spring Boot Starter 공식 문서(https://github.com/mybatis/spring-boot-starter#requirements)에서 지원하는 스프링 부트 버전을 먼저 확인합니다.

![MyBatis 프로젝트 생성 설정과 MyBatis Framework·MariaDB Driver·Lombok 의존성 선택](images/02-2-mybatis/spring-initializr.png)

Mapper는 데이터 접근 메서드를 선언한 인터페이스입니다. MyBatis는 이 인터페이스의 호출을 처리할 객체를 준비하고, 메서드에 연결된 SQL을 실행합니다. XML의 namespace에는 인터페이스의 패키지를 포함한 이름을 쓰고, SQL의 id에는 메서드 이름을 씁니다.

예를 들어 MemberMapper의 findByEmail()을 호출하면 같은 이름의 select 요소를 찾아 SQL을 실행합니다. SQL의 `#{email}`에는 메서드에 전달한 이메일이 들어가고, 조회한 컬럼 값은 resultMap에 따라 Member의 속성으로 옮겨집니다.

**SQL 매핑 방식**

MyBatis는 Mapper 인터페이스의 메서드와 SQL을 연결할 때 XML 방식과 애너테이션 방식을 지원합니다. XML 방식은 별도의 매퍼 파일에 SQL을 작성하고, 애너테이션 방식은 인터페이스의 메서드에 `@Select`, `@Insert`, `@Update`, `@Delete` 등을 붙여 SQL을 작성합니다.

이 실습에서는 SQL과 자바 코드를 분리하여 살펴볼 수 있도록 XML 방식을 사용합니다. 애너테이션 방식의 작성법과 예제는 본 교재의 3.3.2 「애노테이션 기반 SQL 매핑」(131쪽)을 참고합니다.

**실습 코드**

[MemberMapper.java](../spring-mybatis/src/main/java/com/example/demo/mapper/MemberMapper.java)

```java
package com.example.demo.mapper;

import com.example.demo.model.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberMapper {
    List<Member> findAll();
    Member findById(Long id);
    Member findByEmail(String email);
    int insert(Member member);
    int update(Member member);
    int deleteById(Long id);
    List<Member> findAllOrderBy(@Param("order") String order, @Param("dir") String dir);
}
```

[application.properties](../spring-mybatis/src/main/resources/application.properties)

```properties
spring.application.name=spring-mybatis
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
mybatis.mapper-locations=classpath:mapper/*.xml
```

[MemberMapper.xml — memberResultMap](../spring-mybatis/src/main/resources/mapper/MemberMapper.xml)

```xml
<resultMap id="memberResultMap" type="com.example.demo.model.Member">
        <result column="id" property="id" />
        <result column="name" property="name" />
        <result column="email" property="email" />
        <result column="age" property="age" />
    </resultMap>
```

**코드 설명**

`@Mapper`는 MyBatis가 사용할 인터페이스를 표시합니다. `mybatis.mapper-locations`는 XML을 찾을 위치입니다. `resultMap`의 `column`은 조회 결과의 컬럼 이름, `property`는 `Member`의 자바 속성 이름입니다.

Member 모델(`spring-mybatis/src/main/java/com/example/demo/model/Member.java`)은 JDBC처럼 id·name·email·age를 담습니다. 인터페이스와 XML에서 철자가 다르면 호출할 SQL을 찾지 못합니다.

**ResultMap과 자동 매핑**

MyBatis는 SELECT로 조회한 결과를 자바 객체에 담을 때, 기본적으로 조회 결과의 컬럼 이름과 객체의 속성 이름을 비교하여 같은 이름의 속성에 값을 설정합니다. 예를 들어 `email` 컬럼의 값은 `Member` 객체의 `email` 속성에 자동으로 매핑됩니다.

`resultMap`은 조회 결과의 컬럼과 객체의 속성 사이의 대응 관계를 명시하는 설정입니다. 컬럼 이름과 속성 이름이 다를 때는 `column`에 조회 결과의 컬럼 이름을, `property`에 객체의 속성 이름을 지정하여 연결할 수 있습니다.

**독립 예제: 이름이 다른 컬럼과 속성 매핑**

실습 테이블과 별개로, 이메일 주소를 저장하는 컬럼 이름이 `contact`이고 자바 객체의 속성 이름은 `email`인 경우를 가정합니다. 다음과 같이 `resultMap`을 작성하면 `contact` 컬럼의 값을 `email` 속성에 담을 수 있습니다.

```xml
<resultMap id="contactResultMap"
           type="com.example.demo.model.Member">
    <!-- 조회 결과의 contact 컬럼을 객체의 email 속성에 연결한다. -->
    <result column="contact" property="email" />
</resultMap>

<select id="findMembersWithContact" resultMap="contactResultMap">
    SELECT id, name, contact, age
    FROM member_contact
</select>
```

이 예제에서는 `member_contact` 테이블에 `id`, `name`, `contact`, `age` 컬럼이 있다고 가정합니다. `contact`는 명시한 설정에 따라 `email`로 매핑되고, 이름이 같은 `id`, `name`, `age`는 기본 자동 매핑으로 처리됩니다. 매핑을 설명하기 위한 독립 예제이므로 실습 프로젝트에 테이블이나 Mapper 메서드를 추가하지 않습니다.

앞서 작성한 `memberResultMap`은 매핑 구조를 익히기 위해 이름이 같은 컬럼과 속성도 모두 명시했습니다. 자동 매핑이 활성화된 단순 조회에서는 이처럼 모든 항목을 나열할 필요 없이, 이름이 달라 별도 설정이 필요한 항목만 명시할 수 있습니다.

**독립 예제: resultType 명시와 반환 타입 추론**

조회 결과의 컬럼 이름과 객체의 속성 이름이 같다면, 별도의 `resultMap` 없이 `resultType`으로 각 행을 담을 객체의 타입을 지정할 수 있습니다. 다음은 기존 `member` 테이블과 `Member`를 사용하는 설명용 독립 예제로, 두 XML 작성 방식은 같은 Mapper 메서드에 대한 대안입니다. 실습 프로젝트에 별도의 메서드를 추가하거나 두 XML을 함께 등록하지 않습니다.

Mapper 인터페이스에 다음 메서드가 선언되어 있다고 가정합니다. XML의 `namespace`는 이 메서드가 속한 Mapper 인터페이스의 전체 이름과 일치해야 합니다.

```java
List<Member> findAllWithResultType();
```

먼저 `resultType`을 명시하면 MyBatis는 지정한 타입의 객체를 생성하고, 조회 결과의 `id`, `name`, `email`, `age`를 같은 이름의 속성에 자동으로 매핑합니다. 반환값이 `List<Member>`이더라도 `resultType`에는 목록 타입이 아니라 각 행을 담을 `Member` 타입을 지정합니다.

```xml
<select id="findAllWithResultType"
        resultType="com.example.demo.model.Member">
    SELECT id, name, email, age
    FROM member
</select>
```

MyBatis 3.5.12부터는 `resultType`과 `resultMap`을 모두 생략하면 연결된 Mapper 메서드의 반환 타입으로 결과 타입을 추론할 수 있습니다. 다음 XML에서는 `id`와 같은 이름의 메서드를 찾아 반환 타입인 `List<Member>`에서 각 행을 담을 타입이 `Member`임을 알아냅니다.

```xml
<select id="findAllWithResultType">
    SELECT id, name, email, age
    FROM member
</select>
```

이 예제에서는 `resultType`을 명시하거나 생략해도 동일하게 회원 목록을 조회할 수 있습니다. 다만 반환 타입 추론은 객체의 타입을 결정하는 기능이므로, 이름이 다른 컬럼과 속성을 자동으로 연결해 주지는 않습니다. 앞의 `contact`와 `email`처럼 이름이 다르면 `resultMap`으로 연결하거나 `SELECT contact AS email`처럼 SQL 별칭으로 조회 결과의 이름을 맞추어야 합니다.

`resultType`은 결과 객체의 타입을 지정하고, `resultMap`은 미리 정의한 매핑 설정을 참조합니다. 하나의 `<select>`에 두 속성을 함께 지정하지 않으며, 둘 다 생략할 때는 Mapper 메서드의 반환 타입을 추론할 수 있어야 합니다.

**실행과 예상 결과**

설정과 namespace를 확인하고 테스트 클래스를 실행할 준비를 합니다. 클래스와 각 메서드는 이미 프로젝트에 작성되어 있으므로 해당 위치를 열어 함께 읽습니다.

정상적으로 연결되면 MemberMapper를 주입받아 SQL을 호출할 수 있습니다.

### 2.2.2 기능별 실습을 실행하는 테스트

> **본 교재 참고**  
> 3.3.3 「예제 애플리케이션」

MemberMapperTests(`spring-mybatis/src/test/java/com/example/demo/mapper/MemberMapperTests.java`)를 엽니다. `@SpringBootTest`는 앱의 객체들을 준비하고, `@Autowired`는 Mapper를 주입합니다. `@Test`가 붙은 메서드 왼쪽 실행 버튼을 누르면 해당 기능을 실행합니다.

테스트 클래스의 선언부와 주입 필드는 다음과 같습니다. 메서드를 제외한 구조를 발췌했으며, 뒤의 실습 메서드는 이 클래스 안에 들어갑니다.

```java
@Slf4j
@SpringBootTest
@Transactional
class MemberMapperTests {
    @Autowired
    private MemberMapper memberMapper;
}
```

각 테스트는 공통 샘플 회원 4명이 있는 상태에서 개별 실행합니다. 클래스의 @Transactional에 의해 테스트에서 수행한 DB 변경은 종료 후 롤백됩니다. 롤백은 수행한 변경을 취소해 데이터를 원래 상태로 되돌리는 것입니다. 따라서 등록 테스트에서 만든 정혁이 다음 테스트에 남아 있다고 가정하지 않습니다. 자동 증가 ID는 롤백 후에도 건너뛸 수 있습니다.

이 단계의 목표는 Mapper 호출과 결과를 이해하는 것입니다. 성공 표시만 보지 말고 로그에 출력된 행 수와 데이터를 직접 확인합니다. 자동 검증은 3장에서 배웁니다.

### 2.2.3 회원 목록·단건 조회

SELECT의 결과가 여러 행이면 List로, 한 행이면 Member로 받습니다. WHERE 조건의 값은 #{}로 전달합니다. ID는 데이터가 다시 입력될 때 달라질 수 있으므로 샘플 이메일로 찾은 실제 ID를 사용합니다.

**실습 코드**

[MemberMapper.xml — findAll](../spring-mybatis/src/main/resources/mapper/MemberMapper.xml)

```xml
<select id="findAll" resultMap="memberResultMap">
        SELECT id, name, email, age FROM member
    </select>
```

[MemberMapper.xml — findById](../spring-mybatis/src/main/resources/mapper/MemberMapper.xml)

```xml
<select id="findById" resultMap="memberResultMap">
        SELECT id, name, email, age FROM member WHERE id = #{id}
    </select>
```

[MemberMapper.xml — findByEmail](../spring-mybatis/src/main/resources/mapper/MemberMapper.xml)

```xml
<select id="findByEmail" resultMap="memberResultMap">
        SELECT id, name, email, age FROM member WHERE email = #{email}
    </select>
```

[MemberMapperTests.java — testFindById()](../spring-mybatis/src/test/java/com/example/demo/mapper/MemberMapperTests.java)

```java
@Test
void testFindById() {
    // 샘플 이메일로 찾은 회원의 실제 ID를 사용해 조회한다.
    Member member = memberMapper.findByEmail("SeojunYoon@campus.com");
    Member found = memberMapper.findById(member.getId());
    log.info("ID로 조회한 회원: {}", found);
}
```

[MemberMapperTests.java — testFindByEmail()](../spring-mybatis/src/test/java/com/example/demo/mapper/MemberMapperTests.java)

```java
@Test
void testFindByEmail() {
    // 기존 샘플 회원의 이메일로 조회한다.
    Member found = memberMapper.findByEmail("SeojunYoon@campus.com");
    log.info("이메일로 조회한 회원: {}", found);

    // 등록되지 않은 이메일이면 null을 반환한다.
    log.info("없는 이메일로 조회한 결과: {}", memberMapper.findByEmail("missing@example.com"));
}
```

**코드 설명**

XML의 `#{id}`, `#{email}`은 입력값을 안전하게 바인딩하는 자리입니다. `resultMap="memberResultMap"`으로 결과 행을 Member에 연결합니다. testFindById는 이메일 조회 결과의 getId()를 다시 조회 조건으로 사용합니다. findByEmail은 조회 결과가 없으면 null입니다. findAll()은 등록 실습에서 목록 크기를 비교할 때도 호출됩니다.

**실행과 예상 결과**

testFindById와 testFindByEmail을 각각 실행합니다.

윤서준의 이메일과 이름이 출력됩니다. `missing@example.com` 조회는 null입니다. 샘플 회원이 없다면 먼저 공통 데이터를 준비합니다.

**SQL에서 사용할 매개변수 이름 지정하기**

Mapper 인터페이스의 메서드 매개변수에 `@Param("이름")`을 붙이면 SQL에서 사용할 이름을 지정할 수 있습니다. 자바 매개변수 이름과 SQL에서 사용할 이름을 다르게 정할 때도 사용하며, `org.apache.ibatis.annotations.Param`을 import합니다. 다음은 매개변수 이름 지정 방식을 보여 주는 참고 예제입니다.

```java
Member findByEmail(@Param("email") String emailAddress);
```

```sql
SELECT id, name, email, age FROM member WHERE email = #{email}
```

이 경우 SQL에서는 자바 매개변수 이름인 `emailAddress` 대신 `@Param`에 지정한 `email`을 사용합니다. 현재처럼 String이나 Long 값 하나를 전달하는 단순 바인딩에서는 `@Param`을 생략할 수 있습니다.

여러 매개변수를 전달할 때는 각각 `@Param`으로 이름을 지정하면 SQL에서 사용할 이름을 명확히 할 수 있습니다. 2.2.7절의 정렬 예제에서 이 방식을 확인합니다.

### 2.2.4 회원 등록과 생성된 ID

등록 결과에는 실제 저장된 행 수와 DB가 부여한 ID가 있습니다. 등록에 성공했는지 관찰할 때 두 값을 함께 확인하면 좋습니다.

**실습 코드**

[MemberMapper.xml — insert](../spring-mybatis/src/main/resources/mapper/MemberMapper.xml)

```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO member (name, email, age) VALUES (#{name}, #{email}, #{age})
    </insert>
```

[MemberMapperTests.java — testInsertMember()](../spring-mybatis/src/test/java/com/example/demo/mapper/MemberMapperTests.java)

```java
@Test
void testInsertMember() {
    log.info("등록 전 회원 수: {}", memberMapper.findAll().size());

    // 새 회원을 등록하면 DB에서 생성한 ID가 member에 들어간다.
    Member member = Member.builder()
            .name("정혁")
            .email("JungHyuk@campus.com")
            .age(10)
            .build();
    int insertedRows = memberMapper.insert(member);
    log.info("등록한 행 수: {}, 생성된 회원 ID: {}", insertedRows, member.getId());
    log.info("등록한 회원: {}", member);
    log.info("등록 후 회원 수: {}", memberMapper.findAll().size());
}
```

**코드 설명**

`#{name}` 등은 전달한 Member의 속성을 읽습니다. `useGeneratedKeys="true"`는 DB가 만든 키를 받아오고 `keyProperty="id"`는 그 값을 같은 객체의 id에 넣습니다. insert()의 반환값 int는 ID가 아니라 영향받은 행 수입니다.

회원을 등록한 뒤에는 생성된 ID로 해당 회원을 다시 조회하거나, 게시글을 저장할 때 작성자를 지정할 수 있습니다. 생성된 ID가 등록에 사용한 객체에 저장되므로, 이후 처리에서 `member.getId()`로 바로 사용할 수 있습니다.

**실행과 예상 결과**

testInsertMember를 개별 실행합니다. 테스트가 끝나면 등록이 취소되므로, 여기서는 테스트 안에서 조회한 회원 수와 데이터를 로그로 확인합니다.

예상 로그에서는 등록한 행 수와 회원 수의 변화를 확인합니다. 생성 ID는 실행한 DB에 따라 달라지므로 아래에서는 자리표시자로 표현했습니다.

```text
등록 전 회원 수: 4
등록한 행 수: 1, 생성된 회원 ID: <생성된 ID>
등록 후 회원 수: 5
```

테스트 안에서는 정혁을 포함해 5명이 조회됩니다. 테스트가 종료되면 등록이 롤백되어 기존 회원 4명만 남습니다. 생성된 ID가 몇 번인지는 등록한 행 수 1과 별개의 값입니다.

### 2.2.5 회원 정보 수정

수정은 어떤 행을 바꿀지 지정하는 WHERE와 바꿀 값을 지정하는 SET으로 구성됩니다. 객체의 값을 바꿨다고 DB가 자동으로 변경되는 것은 아니므로 이 Mapper의 update()를 호출합니다.

**실습 코드**

[MemberMapper.xml — update](../spring-mybatis/src/main/resources/mapper/MemberMapper.xml)

```xml
<update id="update">
        UPDATE member SET name = #{name}, email = #{email}, age = #{age} WHERE id = #{id}
    </update>
```

[MemberMapperTests.java — testUpdateMember()](../spring-mybatis/src/test/java/com/example/demo/mapper/MemberMapperTests.java)

```java
@Test
void testUpdateMember() {
    // 기존 샘플 회원을 조회해 나이를 변경한다. 테스트 후에는 롤백된다.
    Member member = memberMapper.findByEmail("SeojunYoon@campus.com");
    log.info("수정 전 회원: {}", member);

    member.setAge(11);
    int updatedRows = memberMapper.update(member);
    log.info("수정한 행 수: {}", updatedRows);
    log.info("수정 후 회원: {}", memberMapper.findById(member.getId()));
}
```

**코드 설명**

샘플 윤서준을 이메일로 조회하고 setAge(11)로 값을 바꿉니다. XML은 전달받은 객체의 ID로 행을 찾고 이름·이메일·나이를 저장합니다. 반환값 updatedRows로 수정된 행 수를 확인합니다.

**실행과 예상 결과**

testUpdateMember를 단독 실행합니다.

수정 전 나이는 10, 수정 후 나이는 11, 수정 행 수는 1입니다. 테스트가 끝나면 변경은 롤백됩니다.

**객체의 프로퍼티를 SQL에 전달하기**

현재 `update(Member member)`는 Member 객체 하나를 `@Param` 없이 전달합니다. 이 경우 SQL에서는 `#{name}`, `#{email}`, `#{age}`처럼 프로퍼티 이름만 사용합니다. 프로퍼티는 객체가 제공하는 속성이며, 예를 들어 `name`은 `getName()`으로 읽는 값에 해당합니다. 자바 매개변수 이름이 `member`라고 해서 자동으로 `#{member.name}`을 사용하는 것은 아닙니다.

객체에 `@Param("member")`로 이름을 지정하면 SQL에서는 `#{member.name}`처럼 지정한 이름과 프로퍼티를 함께 사용합니다. 다음은 객체에 이름을 지정하는 방식을 보여 주는 참고 예제입니다.

```java
int update(@Param("member") Member member);
```

```sql
UPDATE member
SET name = #{member.name}, email = #{member.email}, age = #{member.age}
WHERE id = #{member.id}
```

`@Param`을 붙이면 객체가 하나뿐이어도 지정한 이름을 붙여 `#{member.name}`처럼 접근합니다. 현재 실습에서는 기존의 `update(Member member)`와 `#{name}` 방식을 유지합니다.

### 2.2.6 회원 삭제

샘플 회원들은 게시글의 작성자입니다. 외래 키 관계가 있는 회원을 삭제하면 제약 위반이 발생할 수 있습니다. 삭제 연습에서는 게시글이 없는 회원을 새로 준비합니다.

**실습 코드**

[MemberMapper.xml — deleteById](../spring-mybatis/src/main/resources/mapper/MemberMapper.xml)

```xml
<delete id="deleteById">
        DELETE FROM member WHERE id = #{id}
    </delete>
```

[MemberMapperTests.java — testDeleteMember()](../spring-mybatis/src/test/java/com/example/demo/mapper/MemberMapperTests.java)

```java
@Test
void testDeleteMember() {
    // 기존 회원은 게시글이 있을 수 있으므로, 삭제할 회원을 따로 등록한다.
    Member member = Member.builder()
            .name("정혁")
            .email("JungHyuk@campus.com")
            .age(10)
            .build();
    memberMapper.insert(member);
    log.info("삭제 전 회원: {}", memberMapper.findById(member.getId()));

    // 삭제 후 같은 ID로 조회하면 null이 나온다.
    int deletedRows = memberMapper.deleteById(member.getId());
    log.info("삭제한 행 수: {}", deletedRows);
    log.info("삭제 후 조회 결과: {}", memberMapper.findById(member.getId()));
    log.info("삭제 후 회원 수: {}", memberMapper.findAll().size());
}
```

**코드 설명**

테스트 안에서 정혁을 등록하고 생성된 ID로 삭제합니다. deleteById()의 반환값은 삭제 행 수입니다. 같은 ID를 다시 조회해 null인지 로그로 확인합니다. 등록 테스트를 먼저 실행해 놓아야 하는 구조가 아닙니다.

**실행과 예상 결과**

testDeleteMember를 단독 실행합니다.

삭제 행 수 1, 삭제 후 조회 결과 null, 초기 샘플 상태 기준 회원 수 4가 출력됩니다.

### 2.2.7 정렬 조건과 파라미터 방식

데이터 값과 SQL의 컬럼 이름은 성격이 다릅니다. ORDER BY의 컬럼 이름을 문자열 치환으로 넣는 예제를 통해 두 방식을 구분합니다.

**실습 코드**

[MemberMapper.xml — findAllOrderBy](../spring-mybatis/src/main/resources/mapper/MemberMapper.xml)

```xml
<select id="findAllOrderBy" resultMap="memberResultMap">
        SELECT id, name, email, age FROM member ORDER BY ${order} ${dir}
    </select>
```

[MemberMapperTests.java — testFindAllOrderBy()](../spring-mybatis/src/test/java/com/example/demo/mapper/MemberMapperTests.java)

```java
@Test
void testFindAllOrderBy() {
    // id, desc를 전달해 기존 회원을 ID가 큰 순서로 조회한다.
    List<Member> members = memberMapper.findAllOrderBy("id", "desc");
    log.info("ID 내림차순으로 조회한 회원 수: {}", members.size());
    for (Member member : members) {
        log.info("회원 ID: {}, 이름: {}, 나이: {}", member.getId(), member.getName(), member.getAge());
    }
}
```

**코드 설명**

`@Param("order")`, `@Param("dir")`로 두 인자의 이름을 지정합니다. `#{}`는 값 바인딩이지만 `${}`는 문자열을 SQL에 그대로 넣습니다. 현재 코드는 정해진 `id`, `desc`를 전달하는 학습 예제입니다. 사용자 입력을 그대로 전달하면 SQL 구조까지 바뀔 수 있으므로 실제 입력을 받는 경우 허용한 컬럼·정렬 방향만 선택하도록 해야 합니다.

**실행과 예상 결과**

testFindAllOrderBy를 실행하고 ID 순서를 확인합니다. 이 실습에서는 코드에 지정한 id와 desc로 정렬 결과를 관찰합니다.

회원 4명이 ID 내림차순으로 출력됩니다. 처음 순서대로 등록한 DB라면 김도윤 → 공미영 → 윤광철 → 윤서준입니다.

### 2.2.8 학습 확인

- Mapper 인터페이스와 XML의 namespace·id가 어떻게 연결되는지 설명합니다.
- insert()의 반환값과 member.getId()의 차이를 설명합니다.
- 현재 테스트 하나를 실행한 뒤 DB 값이 원래대로 돌아오는 이유를 설명합니다.
- JDBC에서 직접 작성했던 while·ResultSet 변환 코드가 어디로 이동했는지 정리합니다.
