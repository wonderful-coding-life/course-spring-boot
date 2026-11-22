# 3장 RESTful API

> **본 교재 참고**  
> 4장 「RESTful API 작성하기」의 4.1 「RESTful API란 무엇인가?」, 4.2 「JPA로 RESTful API 서버 만들기」, 4.3 「테스트」

RESTful API는 클라이언트가 HTTP로 자원을 조회하고 변경할 수 있게 제공하는 인터페이스입니다. 회원은 members, 게시글은 articles라는 URL로 표현합니다. 서버는 요청의 URL·HTTP 메서드·입력 데이터를 보고 작업을 수행하고 상태 코드와 데이터를 응답합니다.

이 장은 Member로 기본 API를 만든 뒤 Article에서 Service와 DTO를 배웁니다. 여러 회원 가입을 묶는 트랜잭션을 실습하고, 마지막에 결과를 자동으로 검증하는 테스트를 작성합니다.

**준비**: spring-rest를 열고 MariaDB 환경변수를 설정합니다. 공통 schema.sql·data.sql로 회원 4명과 게시글 100개를 준비합니다. DemoApplication을 실행하고 [members.http](../spring-rest/http/members.http), [articles.http](../spring-rest/http/articles.http)를 IDE에서 엽니다. 웹 앱을 동시에 같은 8080 포트로 실행하지 않습니다.

## 3.1 HTTP와 API의 기본 개념

> **본 교재 참고**  
> 4.1.1 「웹 애플리케이션의 진화」, 4.1.2 「RESTful API 설계 원칙」; 프로젝트 생성은 4.2.1 「프로젝트 생성 및 JPA 리포지터리 작성」

**프로젝트 생성과 의존성 선택**

Spring Initializr에서 다음 의존성을 선택하고, 생성한 프로젝트 폴더를 `spring-rest`로 준비합니다.

| 의존성 | 역할 |
|---|---|
| Spring Web | Controller에서 HTTP 요청을 받고 JSON을 응답합니다. |
| Spring Data JPA | Repository로 회원과 게시글을 저장하고 조회합니다. |
| MariaDB Driver | 기존 실습용 MariaDB에 연결합니다. |
| Lombok | 생성자와 getter 같은 반복 코드를 줄여 줍니다. |

![REST API 프로젝트 생성 설정과 Spring Web·Spring Data JPA·MariaDB Driver·Lombok 의존성 선택](images/03-restful-api/spring-initializr.png)

요청은 메서드·URL·헤더·본문으로 구성됩니다. GET은 조회, POST는 등록·처리, PUT은 전체 수정, PATCH는 부분 수정, DELETE는 삭제에 사용합니다. 이 예제에서 PATCH는 생략하거나 null인 필드를 그대로 두는 간단한 규칙을 사용합니다.

응답에는 HTTP 상태와 헤더·본문이 있습니다. 200은 성공, 201은 등록 성공, 204는 성공했지만 본문 없음, 404는 대상 없음입니다. JSON은 객체를 `{ }`, 목록을 `[ ]`로 표현하는 데이터 형식입니다. 자바 객체 자체를 네트워크로 보내는 것이 아니라 JSON으로 변환해 전송합니다.

### 3.1.1 HTTP Client와 응답 ID 재사용

http 파일의 요청 왼쪽 실행 버튼을 눌러 서버에 요청합니다. 먼저 앱이 실행 중이어야 합니다. 파일의 `@baseUrl`은 주소를 반복 작성하지 않기 위한 변수입니다.

[members.http — 요청 2](../spring-rest/http/members.http)

```http
### 2. 회원 등록 (201 Created)
POST {{baseUrl}}/members
Content-Type: application/json
Accept: application/json

{
  "name": "정혁",
  "email": "JungHyuk@campus.com",
  "age": 10
}

> {%
    // 등록 응답의 ID를 다음 요청에서 사용할 memberId 변수에 저장한다.
    client.global.set("memberId", response.body.id);
%}
```

Content-Type은 보내는 본문의 형식, Accept는 받고 싶은 응답 형식입니다. 응답 처리의 `client.global.set()`은 실제 생성 ID를 다음 요청에서 재사용하도록 합니다. 이 작은 스크립트는 HTTP Client의 기능이며 서버 Controller 코드가 아닙니다. 아래 요청은 파일의 기존 위치에서 순서대로 실행합니다.

정혁을 등록하면 201과 회원 JSON을 받습니다. ID를 5라고 외우지 말고 응답에서 확인합니다. 마지막 삭제 요청까지 실행하면 같은 이메일로 다시 실습할 수 있습니다.

## 3.2 Member로 기본 CRUD API

> **본 교재 참고**  
> 4.2.2 「회원 관리 RESTful API 작성」

Controller는 웹 요청을 받는 진입점입니다. @RestController는 반환 객체를 응답 본문으로 변환합니다. Member 예제에서는 Service·DTO 없이 Repository를 직접 사용해 요청과 CRUD 연결에 집중합니다.

[MemberController](../spring-rest/src/main/java/com/example/demo/controller/MemberController.java)의 @RequestMapping("/members")가 모든 메서드의 공통 주소입니다. @RequiredArgsConstructor가 final 필드를 받는 생성자를 만들고 스프링이 Repository와 MemberService를 주입합니다. Member 엔티티의 구조는 JPA 절과 같고, 현재 Repository는 REST에 필요한 이메일 검색만 추가합니다.

[MemberRepository.java](../spring-rest/src/main/java/com/example/demo/repository/MemberRepository.java)

```java
package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 기본 CRUD는 JpaRepository의 findAll, findById, save, delete를 그대로 사용한다.
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일이 일치하는 회원을 조회한다. 없으면 빈 Optional을 반환한다.
    Optional<Member> findByEmail(String email);
}
```

### 3.2.1 회원 목록·단건 조회

URL 안의 ID는 @PathVariable로 받습니다. Repository의 결과가 없으면 HTTP 404 예외를 발생시킵니다.

**실습 코드**

[MemberController.java — findAll()](../spring-rest/src/main/java/com/example/demo/controller/MemberController.java)

```java
// GET /members: 전체 회원을 조회한다.
@GetMapping
public List<Member> findAll() {
    return memberRepository.findAll();
}
```

[MemberController.java — findById()](../spring-rest/src/main/java/com/example/demo/controller/MemberController.java)

```java
// GET /members/회원ID: URL의 ID로 회원을 조회한다. 없으면 404로 응답한다.
@GetMapping("/{id}")
public Member findById(@PathVariable("id") Long id) {
    return memberRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
}
```

[members.http — 요청 1](../spring-rest/http/members.http)

```http
### 1. 전체 회원 조회 (200 OK)
GET {{baseUrl}}/members
Accept: application/json
```

[members.http — 요청 3](../spring-rest/http/members.http)

```http
### 3. 등록한 회원 조회 (200 OK)
GET {{baseUrl}}/members/{{memberId}}
Accept: application/json
```

**코드 설명**

@GetMapping에 주소가 없으면 공통 주소 /members를 사용합니다. `/{id}`가 있으면 /members/실제ID입니다. 전체 조회는 List를 JSON 배열로, 단건 조회는 Member를 JSON 객체로 변환합니다. ResponseStatusException은 상태와 안내 사유를 전달합니다.

**실행과 예상 결과**

members.http의 목록 요청을 실행합니다. 단건 요청은 등록 응답으로 memberId를 저장한 뒤 실행합니다.

목록은 200과 회원 배열, 단건은 200과 이름·이메일·나이·ID를 반환합니다. 실습 데이터가 초기 상태라면 목록은 4명입니다.

### 3.2.2 회원 등록

요청 JSON을 자바 객체로 받으려면 @RequestBody를 사용합니다. DB가 생성할 ID는 클라이언트가 정하지 않습니다.

**실습 코드**

[MemberController.java — create()](../spring-rest/src/main/java/com/example/demo/controller/MemberController.java)

```java
// POST /members: 요청 JSON을 Member로 받아 등록하고, 201로 응답한다.
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Member create(@RequestBody Member member) {
    // 신규 등록의 ID는 DB가 생성한다. 요청에 ID가 있어도 사용하지 않는다.
    member.setId(null);
    return memberRepository.save(member);
}
```

**코드 설명**

JSON의 name·email·age가 Member의 속성으로 들어갑니다. setId(null)은 요청에 ID가 들어 있어도 신규 등록의 식별자로 사용하지 않도록 합니다. @ResponseStatus(CREATED)는 실제 201 응답을 지정합니다. 반환한 저장 결과가 응답 JSON입니다.

**실행과 예상 결과**

members.http의 회원 등록 요청을 실행하고 응답 본문과 memberId 변수 저장을 확인합니다.

정혁 / JungHyuk@campus.com / 10과 실제 ID를 받습니다. DB에도 한 행이 추가됩니다. 이 HTTP 요청은 앞 절의 롤백 테스트와 달리 실제 저장됩니다.

### 3.2.3 회원 전체 수정

PUT 예제는 이름·이메일·나이를 모두 받아 기존 회원 정보를 교체합니다. 먼저 URL의 ID로 존재하는 회원을 찾습니다.

**실습 코드**

[MemberController.java — update()](../spring-rest/src/main/java/com/example/demo/controller/MemberController.java)

```java
// PUT /members/회원ID: 기존 회원의 이름, 이메일, 나이를 요청 내용으로 변경한다.
@PutMapping("/{id}")
public Member update(@PathVariable("id") Long id, @RequestBody Member member) {
    Member found = memberRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

    found.setName(member.getName());
    found.setEmail(member.getEmail());
    found.setAge(member.getAge());
    return memberRepository.save(found);
}
```

[members.http — 요청 4](../spring-rest/http/members.http)

```http
### 4. 회원 수정 (200 OK)
# PUT은 이름, 이메일, 나이를 모두 보낸다. 나이를 11살로 변경한다.
PUT {{baseUrl}}/members/{{memberId}}
Content-Type: application/json
Accept: application/json

{
  "name": "정혁",
  "email": "JungHyuk@campus.com",
  "age": 11
}
```

**코드 설명**

URL ID가 수정 대상을 결정합니다. 조회한 객체에 요청 값을 복사하고 save()합니다. JSON의 ID로 수정 대상을 바꾸는 구조가 아닙니다. 여기서는 별도 DTO 검증 없이 세 필드를 모두 전달하는 실습 규칙을 따릅니다.

**실행과 예상 결과**

등록한 정혁에 대해 수정 요청과 다음 조회 요청을 실행합니다.

200 응답에서 나이가 11로 바뀝니다. 이름과 이메일은 요청한 값으로 유지됩니다.

### 3.2.4 회원 부분 수정

PATCH는 필요한 필드만 전달하는 예제로 구성합니다. 이 구현의 규칙을 HTTP PATCH의 모든 구현 방식과 동일하다고 생각하지는 않습니다.

**실습 코드**

[MemberController.java — patch()](../spring-rest/src/main/java/com/example/demo/controller/MemberController.java)

```java
// PATCH /members/회원ID: 이름, 이메일, 나이 중 전달한 값만 수정한다.
@PatchMapping("/{id}")
public Member patch(@PathVariable("id") Long id, @RequestBody Member member) {
    Member found = memberRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

    // 이 예제에서는 생략하거나 null로 보낸 필드는 기존 값을 유지한다.
    if (member.getName() != null) {
        found.setName(member.getName());
    }
    if (member.getEmail() != null) {
        found.setEmail(member.getEmail());
    }
    if (member.getAge() != null) {
        found.setAge(member.getAge());
    }
    return memberRepository.save(found);
}
```

[members.http — 요청 6](../spring-rest/http/members.http)

```http
### 6. 회원 일부 수정 (200 OK)
# PATCH는 바꿀 필드만 보낸다. 이름을 정현으로 바꾸고 이메일과 나이는 유지한다.
PATCH {{baseUrl}}/members/{{memberId}}
Content-Type: application/json
Accept: application/json

{
  "name": "정현"
}
```

**코드 설명**

각 값이 null이 아닐 때만 기존 객체에 적용합니다. JSON에서 생략한 값과 명시적으로 null로 보낸 값을 이 예제는 구별하지 않습니다. 따라서 이 메서드로 값을 null로 지우는 기능은 제공하지 않습니다.

**실행과 예상 결과**

부분 수정과 그 다음 조회를 실행합니다.

이름은 정현으로 바뀌고 이메일과 앞에서 수정한 나이 11은 유지됩니다.

### 3.2.5 회원 삭제

삭제할 회원을 조회해 확인한 뒤 삭제합니다. 성공 응답에 데이터가 필요하지 않으면 204를 사용합니다.

**실습 코드**

[MemberController.java — delete()](../spring-rest/src/main/java/com/example/demo/controller/MemberController.java)

```java
// DELETE /members/회원ID: 회원을 삭제하고, 본문 없이 204로 응답한다.
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable("id") Long id) {
    Member member = memberRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    memberRepository.delete(member);
}
```

[members.http — 요청 8](../spring-rest/http/members.http)

```http
### 8. 회원 삭제 (204 No Content)
# 이 파일에서 등록한 회원만 삭제한다. 응답 본문은 없다.
DELETE {{baseUrl}}/members/{{memberId}}
```

[members.http — 요청 9](../spring-rest/http/members.http)

```http
### 9. 삭제한 회원 조회 (404 Not Found)
# 삭제 후에는 같은 ID로 조회할 수 없으므로 404가 정상적인 결과다.
GET {{baseUrl}}/members/{{memberId}}
Accept: application/json
```

**코드 설명**

@DeleteMapping은 DELETE 요청을 처리하고, NO_CONTENT는 204를 지정합니다. 이 실습에서는 작성한 게시글이 없는 새 회원을 삭제합니다. 기존 작성자 삭제는 외래 키 제약으로 실패할 수 있습니다.

**실행과 예상 결과**

members.http의 삭제와 삭제 후 조회를 순서대로 실행합니다.

삭제 응답은 본문 없는 204입니다. 삭제한 ID를 다시 조회하면 404입니다. 404는 이 실습에서 의도한 확인 결과입니다.

### 3.2.6 이메일 검색과 오류 사유

경로 변수는 /members/ID처럼 자원 위치를 표현하고, 쿼리 파라미터는 ?email=값처럼 검색 조건을 전달합니다.

**실습 코드**

[MemberController.java — findByEmail()](../spring-rest/src/main/java/com/example/demo/controller/MemberController.java)

```java
// GET /members/search?email=이메일: ? 뒤의 email 값을 받아 조회한다.
@GetMapping("/search")
public Member findByEmail(@RequestParam("email") String email) {
    return memberRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
}
```

[members.http — 요청 10](../spring-rest/http/members.http)

```http
### 10. 이메일로 샘플 회원 조회 (200 OK)
# ?email=값을 @RequestParam("email")으로 받는다. 등록한 정혁과 관계없이 실행할 수 있다.
GET {{baseUrl}}/members/search?email=SeojunYoon@campus.com
Accept: application/json
```

[application.properties](../spring-rest/src/main/resources/application.properties)

```properties
spring.application.name=demo

# 실행과 테스트 모두 DB_URL에 지정한 MariaDB를 사용한다.
# sql/schema.sql과 sql/data.sql을 먼저 직접 실행해 테이블과 샘플 데이터를 준비한다.
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}

# 기본값 never는 예외 메시지에 담길 수 있는 내부 정보나 민감한 정보의 노출을 막기 위한 것이다.
# 실습에서는 오류 원인을 확인할 수 있도록 응답의 message 항목에 예외 사유를 포함한다.
spring.web.error.include-message=always
```

**코드 설명**

@RequestParam("email")이 URL의 email 값을 받습니다. spring.web.error.include-message=always는 예외 사유를 오류 응답의 message에 포함하도록 합니다. 기본적으로 메시지 노출이 제한되는 이유는 내부 정보가 섞일 수 있기 때문입니다.

**실행과 예상 결과**

샘플 이메일 검색을 실행합니다. 이어서 등록되지 않은 이메일로 바꾸어 비교합니다.

등록된 이메일은 200과 회원 객체입니다. 미등록 이메일은 404와 `회원을 찾을 수 없습니다.`라는 사유를 확인합니다. 기본 오류 응답의 다른 필드는 실행 환경에 따라 달라질 수 있습니다.

## 3.3 Service와 DTO로 게시글 API 작성

> **본 교재 참고**  
> 4.2.3 「서비스 계층 분리」, 4.2.4 「게시글 RESTful API 구현」

여러 Repository를 호출하거나, 작성자 확인과 응답 변환 같은 처리가 생기면 Controller가 길어집니다. Service는 요청 처리에 필요한 작업을 모으고, Controller는 입력과 응답에 집중하도록 역할을 나눕니다.

DTO는 요청 또는 응답에 필요한 값만 담는 객체입니다. 엔티티는 DB 매핑 구조를 표현하고 DTO는 API가 주고받을 구조를 표현합니다. 게시글 등록에는 작성자 ID가 필요하지만 수정에는 필요하지 않습니다. 응답에는 회원 전체 대신 작성자 ID·이름을 포함합니다.

### 3.3.1 요청·응답 DTO 준비

[ArticleCreateRequest.java](../spring-rest/src/main/java/com/example/demo/dto/ArticleCreateRequest.java)

```java
package com.example.demo.dto;

import lombok.Data;

// 등록 요청에는 작성자 ID와 제목, 본문만 받는다. 게시글 ID와 날짜는 서버에서 만든다.
@Data
public class ArticleCreateRequest {
    private Long memberId;
    private String title;
    private String description;
}
```

[ArticleUpdateRequest.java](../spring-rest/src/main/java/com/example/demo/dto/ArticleUpdateRequest.java)

```java
package com.example.demo.dto;

import lombok.Data;

// 수정할 때는 제목과 본문만 받는다. 기존 작성자는 바꾸지 않는다.
@Data
public class ArticleUpdateRequest {
    private String title;
    private String description;
}
```

[ArticleResponse.java](../spring-rest/src/main/java/com/example/demo/dto/ArticleResponse.java)

```java
package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Entity를 그대로 반환하지 않고, 응답에 필요한 정보만 담는다.
@Getter
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String description;
    private Long memberId;
    // Member 객체 전체 대신 작성자 이름을 제공한다.
    private String writerName;
    private LocalDateTime created;
    private LocalDateTime updated;
}
```

등록 DTO는 memberId·title·description, 수정 DTO는 title·description을 받습니다. 응답 DTO에는 게시글 ID·날짜·작성자 이름도 포함합니다. 같은 게시글이라도 용도별로 모양이 다릅니다. API가 받지 않는 값을 DTO에 넣지 않으면 역할을 더 쉽게 구분할 수 있습니다.

### 3.3.2 게시글 등록과 응답 변환

Controller → Service → Repository 순서로 요청을 전달합니다. Service는 작성자가 실제로 존재하는지 확인하고 게시글과 연결합니다.

**실습 코드**

[ArticleController.java — create()](../spring-rest/src/main/java/com/example/demo/controller/ArticleController.java)

```java
// POST /articles: 요청 DTO로 게시글을 등록하고 201로 응답한다.
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ArticleResponse create(@RequestBody ArticleCreateRequest request) {
    return articleService.create(request);
}
```

[ArticleService.java — create()](../spring-rest/src/main/java/com/example/demo/service/ArticleService.java)

```java
public ArticleResponse create(ArticleCreateRequest request) {
    // 요청의 회원 ID로 실제 작성자를 찾아 게시글과 연결한다.
    Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없습니다."));

    Article article = Article.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .member(member)
            .build();
    Article saved = articleRepository.save(article);
    return toResponse(saved);
}
```

[ArticleService.java — toResponse()](../spring-rest/src/main/java/com/example/demo/service/ArticleService.java)

```java
private ArticleResponse toResponse(Article article) {
    // 관계 매핑된 회원에서 ID와 이름을 꺼내 응답에 담는다.
    return ArticleResponse.builder()
            .id(article.getId())
            .title(article.getTitle())
            .description(article.getDescription())
            .memberId(article.getMember().getId())
            .writerName(article.getMember().getName())
            .created(article.getCreated())
            .updated(article.getUpdated())
            .build();
}
```

[articles.http — 요청 1](../spring-rest/http/articles.http)

```http
### 1. 작성자로 사용할 기존 회원 조회 (200 OK)
GET {{baseUrl}}/members
Accept: application/json

> {%
    // 회원 목록의 첫 회원 ID를 작성자 ID로 저장한다.
    client.global.set("articleMemberId", response.body[0].id);
%}
```

[articles.http — 요청 2](../spring-rest/http/articles.http)

```http
### 2. 게시글 등록 (201 Created)
# 회원 객체 전체 대신 작성자 ID와 제목, 본문을 보낸다.
POST {{baseUrl}}/articles
Content-Type: application/json
Accept: application/json

{
  "memberId": {{articleMemberId}},
  "title": "토요일 오전에 같이 산책하실 분",
  "description": "오전 열 시에 공원 입구에서 만나요. 한 바퀴 걷고 차도 마시면 좋겠습니다."
}

> {%
    // 등록 응답의 게시글 ID를 이후 조회, 수정, 삭제 요청에서 재사용한다.
    client.global.set("articleId", response.body.id);
%}
```

**코드 설명**

요청의 memberId로 Member를 조회합니다. Article에 Member 객체를 연결해 저장하고, 저장 결과를 toResponse()로 바꿉니다. writerName은 관계 객체의 name입니다. Entity→DTO 변환은 DB 구조와 응답 구조를 연결하는 코드입니다. 현재 ArticleService에는 클래스 단위 @Transactional을 붙이지 않았고 save() 등 기본 Repository 메서드를 사용합니다.

**실행과 예상 결과**

articles.http를 위에서부터 실행합니다. 첫 요청이 작성자 실제 ID를 저장하고 등록 응답이 articleId를 저장합니다.

201과 새 게시글 ID, 제목·본문·작성자 ID·작성자 이름·생성일·수정일을 받습니다. 생성일과 수정일은 엔티티의 PrePersist에서 준비됩니다.

### 3.3.3 게시글 단건 조회

조회할 때도 Entity를 그대로 응답하지 않고 같은 응답 DTO를 사용합니다. 어떤 필드가 응답에 노출되는지 일정하게 유지할 수 있습니다.

**실습 코드**

[ArticleController.java — findById()](../spring-rest/src/main/java/com/example/demo/controller/ArticleController.java)

```java
// GET /articles/게시글ID: 작성자 이름을 포함한 게시글을 조회한다.
@GetMapping("/{id}")
public ArticleResponse findById(@PathVariable("id") Long id) {
    return articleService.findById(id);
}
```

[ArticleService.java — findById()](../spring-rest/src/main/java/com/example/demo/service/ArticleService.java)

```java
public ArticleResponse findById(Long id) {
    Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
    return toResponse(article);
}
```

[articles.http — 요청 3](../spring-rest/http/articles.http)

```http
### 3. 등록한 게시글 조회 (200 OK)
# 응답의 writerName에서 작성자 이름을 확인한다.
GET {{baseUrl}}/articles/{{articleId}}
Accept: application/json
```

**코드 설명**

Controller는 ID만 Service로 전달합니다. Service는 게시글을 찾고 없으면 404를 던집니다. 있으면 toResponse()에서 작성자 이름 등을 채웁니다.

**실행과 예상 결과**

등록한 글을 조회합니다.

200과 등록 결과에 대응하는 DTO JSON을 받습니다. member 객체 전체가 아니라 writerName과 memberId를 확인합니다.

### 3.3.4 게시글 수정

수정 요청의 DTO에는 바꿀 제목과 본문만 있습니다. 작성자는 그대로 유지합니다.

**실습 코드**

[ArticleController.java — update()](../spring-rest/src/main/java/com/example/demo/controller/ArticleController.java)

```java
// PUT /articles/게시글ID: 제목과 본문을 수정한다.
@PutMapping("/{id}")
public ArticleResponse update(@PathVariable("id") Long id, @RequestBody ArticleUpdateRequest request) {
    return articleService.update(id, request);
}
```

[ArticleService.java — update()](../spring-rest/src/main/java/com/example/demo/service/ArticleService.java)

```java
public ArticleResponse update(Long id, ArticleUpdateRequest request) {
    Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

    article.setTitle(request.getTitle());
    article.setDescription(request.getDescription());
    // 변경 내용을 저장하고, save가 반환한 객체로 응답을 만든다.
    Article saved = articleRepository.save(article);
    return toResponse(saved);
}
```

[articles.http — 요청 4](../spring-rest/http/articles.http)

```http
### 4. 게시글 수정 (200 OK)
# 수정 요청은 제목과 본문만 보낸다. 작성자는 바뀌지 않는다.
PUT {{baseUrl}}/articles/{{articleId}}
Content-Type: application/json
Accept: application/json

{
  "title": "일요일 오전에 같이 산책하실 분",
  "description": "토요일에 비 예보가 있어 일요일로 미뤘어요. 시간과 장소는 그대로입니다."
}
```

**코드 설명**

조회한 Article의 두 필드를 바꾸고 save()가 반환한 객체로 DTO를 만듭니다. 저장 과정에서 업데이트가 발생하면 PreUpdate가 수정일을 갱신합니다. 같은 시각의 정밀도 안에서 즉시 수정하면 표시된 시각이 같아 보일 수 있습니다.

**실행과 예상 결과**

수정 요청을 실행하고 다시 조회합니다.

제목은 `일요일 오전에 같이 산책하실 분`으로, 본문은 요청한 내용으로 바뀝니다. 작성자와 생성일은 유지되고 수정일은 업데이트 시점에 맞춰집니다.

### 3.3.5 게시글 삭제

Controller는 삭제 성공 상태를, Service는 실제 조회·삭제를 담당합니다.

**실습 코드**

[ArticleController.java — delete()](../spring-rest/src/main/java/com/example/demo/controller/ArticleController.java)

```java
// DELETE /articles/게시글ID: 삭제 후 본문 없이 204로 응답한다.
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable("id") Long id) {
    articleService.delete(id);
}
```

[ArticleService.java — delete()](../spring-rest/src/main/java/com/example/demo/service/ArticleService.java)

```java
public void delete(Long id) {
    Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
    articleRepository.delete(article);
}
```

[articles.http — 요청 8](../spring-rest/http/articles.http)

```http
### 8. 게시글 삭제 (204 No Content)
# 이 파일에서 등록한 게시글만 삭제한다. 응답 본문은 없다.
DELETE {{baseUrl}}/articles/{{articleId}}
```

**코드 설명**

존재 여부를 확인한 게시글만 삭제합니다. void 반환과 204 상태로 응답 본문을 비웁니다. articles.http에서 새로 등록한 글을 대상으로 실행합니다.

**실행과 예상 결과**

페이징까지 실습한 다음 삭제 요청과 삭제 후 조회 요청을 실행합니다.

삭제는 204, 이후 단건 조회는 404입니다. 기존 샘플 100개는 유지됩니다.

### 3.3.6 페이징 결과를 DTO로 변환

Page에는 목록뿐 아니라 전체 개수·현재 페이지 정보도 있습니다. 내용만 DTO로 바꾸면서 페이지 정보를 유지할 때 Page.map()을 사용합니다.

**실습 코드**

[ArticleController.java — findAll()](../spring-rest/src/main/java/com/example/demo/controller/ArticleController.java)

```java
// GET /articles?page=0&size=5&sort=id,desc: 페이지 번호, 크기, 정렬 조건을 받는다.
// PageableDefault는 요청에서 생략한 조건의 기본값이다. 페이지 번호는 0부터 시작한다.
@GetMapping
public Page<ArticleResponse> findAll(
        @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return articleService.findAll(pageable);
}
```

[ArticleService.java — findAll()](../spring-rest/src/main/java/com/example/demo/service/ArticleService.java)

```java
public Page<ArticleResponse> findAll(Pageable pageable) {
    Page<Article> page = articleRepository.findAll(pageable);

    // map은 현재 페이지의 게시글마다 toResponse를 호출해 응답 DTO로 바꾼다.
    // 페이지 번호, 전체 게시글 수, 전체 페이지 수는 그대로 유지한다.
    return page.map(this::toResponse);
}
```

[articles.http — 요청 6](../spring-rest/http/articles.http)

```http
### 6. 첫 번째 페이지 조회 (200 OK)
# page=0은 첫 페이지, size=5는 페이지 크기, sort=id,desc는 ID 내림차순이다.
# content는 현재 목록, totalElements는 전체 게시글 수, totalPages는 전체 페이지 수다.
GET {{baseUrl}}/articles?page=0&size=5&sort=id,desc
Accept: application/json
```

**코드 설명**

@PageableDefault는 요청에서 생략한 페이지 크기와 정렬의 기본값입니다. `page.map(this::toResponse)`는 페이지 안의 각 Article에 toResponse를 적용합니다. this::toResponse는 같은 클래스의 메서드를 전달하는 자바 문법입니다. 새로운 조회를 반복 수행하는 페이징 메서드가 아니라 이미 조회한 페이지의 내용을 변환합니다.

**실행과 예상 결과**

첫 페이지와 두 번째 페이지 요청을 실행합니다.

샘플 100개만 있다면 size=5에서 totalElements는 100, totalPages는 20입니다. 새 글 등록 후 삭제 전이라면 101·21이 될 수 있습니다. content에는 최대 5개가 들어 있습니다. Page JSON의 세부 부가 필드 이름은 라이브러리 버전에 따라 달라질 수 있으므로 현재 응답을 함께 확인합니다.

## 3.4 트랜잭션과 회원 일괄 가입

> **본 교재 참고**  
> 4.2.5 「트랜잭션 처리」

트랜잭션은 여러 DB 작업을 하나의 성공·실패 단위로 묶는 기능입니다. 두 회원을 저장할 때 두 번째 저장이 실패하면 첫 번째 저장도 취소해야 할 수 있습니다. 기본적으로 @Transactional 메서드를 통과해 나오는 RuntimeException 계열 예외는 롤백 대상입니다.

### 3.4.1 중복 이메일로 전체 롤백 확인

MemberService의 일괄 가입 메서드에만 트랜잭션을 적용합니다. DTO를 추가하지 않고 Member 배열을 받으며 트랜잭션의 효과에 집중합니다.

**실습 코드**

[MemberController.java — registerAll()](../spring-rest/src/main/java/com/example/demo/controller/MemberController.java)

```java
// POST /members/batch: 회원 JSON 배열을 받아 Service에서 한 번에 가입 처리한다.
@PostMapping("/batch")
@ResponseStatus(HttpStatus.CREATED)
public void registerAll(@RequestBody List<Member> members) {
    memberService.registerAll(members);
}
```

[MemberService.java](../spring-rest/src/main/java/com/example/demo/service/MemberService.java)

```java
package com.example.demo.service;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 여러 회원의 가입을 하나로 묶는다. 중간에 예외가 발생하면 앞의 가입도 취소된다.
    @Transactional
    public void registerAll(List<Member> members) {
        for (Member member : members) {
            // 신규 회원의 ID는 DB에서 생성한다.
            member.setId(null);
            memberRepository.save(member);
        }
    }
}
```

[members-batch.http](../spring-rest/http/members-batch.http)

```http
# spring-rest 실행 후 위에서부터 요청을 실행한다.
# data.sql의 윤서준은 등록되어 있고, 정혁은 아직 등록되지 않은 상태에서 시작한다.
# members.http로 정혁을 등록했다면 그 파일의 삭제 요청까지 먼저 실행한다.
@baseUrl = http://localhost:8080

### 1. 일괄 가입 전 회원 목록 조회 (200 OK)
# 윤서준은 있고 정혁은 없는지 확인한다.
GET {{baseUrl}}/members
Accept: application/json

### 2. 정혁 가입 후 중복 이메일로 실패 (500 Internal Server Error)
# 첫 번째 정혁은 저장되지만, 두 번째 이메일은 기존 윤서준과 중복되어 예외가 발생한다.
# Service의 @Transactional 때문에 정혁의 가입도 함께 롤백된다.
# 별도 예외 처리기를 만들지 않았으므로 중복 이메일 오류는 500으로 응답한다.
POST {{baseUrl}}/members/batch
Content-Type: application/json

[
  {
    "name": "정혁",
    "email": "JungHyuk@campus.com",
    "age": 10
  },
  {
    "name": "윤서준",
    "email": "SeojunYoon@campus.com",
    "age": 10
  }
]

### 3. 롤백 후 회원 목록 조회 (200 OK)
# 정혁이 추가되지 않고 기존 회원만 남아 있는지 확인한다.
GET {{baseUrl}}/members
Accept: application/json

# 비교 실습: registerAll의 @Transactional을 주석 처리하고 재시작해 같은 요청을 실행한다.
# 이때는 정혁의 가입이 취소되지 않는다. 비교 후에는 정혁을 삭제하고 애노테이션을 복구한다.
```

**코드 설명**

Controller가 주입받은 Service의 공개 메서드를 호출하면 스프링의 트랜잭션 처리가 적용됩니다. 정혁을 저장한 뒤 기존 윤서준 이메일로 저장하면 UNIQUE 예외가 발생합니다. 예외를 삼키지 않고 밖으로 전달하므로 일괄 가입 전체가 롤백됩니다. 이 REST 프로젝트에는 이 DB 예외를 409로 바꾸는 전역 처리기가 없어서 현재 요청은 500으로 응답합니다. 다음 MVC 장의 409 예제와 구분합니다.

**실행과 예상 결과**

정혁이 아직 없는 상태에서 members-batch.http를 순서대로 실행합니다. 비교하려면 @Transactional을 잠시 주석 처리하고 재시작해 같은 요청을 실행한 뒤 원상 복구합니다.

트랜잭션 적용 시 요청은 실패하고 정혁도 남지 않습니다. 주석 처리 비교에서는 첫 번째 저장이 남을 수 있습니다. 비교 후 남은 정혁은 members.http 등의 API로 실제 ID를 확인해 삭제하고 다음 실습을 시작합니다.

## 3.5 자동 검증과 테스트

> **본 교재 참고**  
> 4.3 「테스트」

앞 장의 로그는 사람이 읽어 결과를 판단했습니다. 자동 검증은 예상값과 실제값이 다르면 테스트를 실패하게 합니다. 통합 테스트는 실제 여러 계층을 연결하고, 단위 테스트는 확인할 대상을 좁혀 주변 의존성을 가짜로 대체할 수 있습니다.

이 절은 Service 단위 테스트 한 개와 게시글 단건 조회 통합 테스트 한 개에 집중합니다. 테스트 데이터가 서로 다릅니다. 단위 테스트는 객체를 직접 만들고, 통합 테스트는 준비된 MariaDB를 사용합니다.

### 3.5.1 Mockito와 assertThat으로 Service 검증

> **본 교재 참고**  
> 4.3.1 「테스트 클래스 작성」, 4.3.3 「단위 테스트」

가짜 Repository가 돌려줄 값을 미리 정하면 DB가 없어도 실제 Service의 변환 코드를 실행할 수 있습니다. Mockito는 이런 가짜 객체를 만들어 주는 라이브러리입니다.

**실습 코드**

[ArticleServiceTests.java](../spring-rest/src/test/java/com/example/demo/service/ArticleServiceTests.java)

```java
package com.example.demo.service;

import com.example.demo.dto.ArticleResponse;
import com.example.demo.entity.Article;
import com.example.demo.entity.Member;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// 스프링과 DB 없이 Mockito로 Service만 확인한다.
// JUnit에 Mockito를 연결해 @Mock, @InjectMocks가 붙은 필드를 준비한다.
@ExtendWith(MockitoExtension.class)
class ArticleServiceTests {
    // 실제 Repository 대신 가짜 객체를 만든다. SQL을 실행하거나 DB에 접근하지 않는다.
    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private MemberRepository memberRepository;

    // 실제 ArticleService 객체를 만들고, 생성자에 위의 가짜 Repository들을 전달한다.
    // Service 자체는 가짜가 아니므로 실제 메서드 코드가 실행된다.
    @InjectMocks
    private ArticleService articleService;

    @Test
    // DisplayName은 실행 결과에 표시할 테스트 이름이다.
    @DisplayName("게시글을 제목과 작성자 이름이 담긴 응답 DTO로 변환한다")
    // @Disabled("사유")를 붙이면 이 테스트의 실행을 건너뛴다. 클래스에 붙이면 전체를 건너뛴다.
    void findArticleById() {
        // DB에 저장하지 않고 객체만 준비한다. ID도 테스트에서 직접 정한다.
        Member member = Member.builder()
                .id(1L).name("정혁").email("JungHyuk@campus.com").age(10).build();
        Article article = Article.builder()
                .id(1L).title("주말에 한강에서 자전거를 탔어요").member(member).build();

        // when(호출).thenReturn(결과): 가짜 객체가 특정 호출에 돌려줄 결과를 정한다.
        // 이후 findById(1L)가 호출되면 DB 조회 대신 Optional에 담은 article을 반환한다.
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // 실제 Service가 Entity를 응답 DTO로 바꾸는지 확인한다.
        ArticleResponse response = articleService.findById(1L);

        assertThat(response.getTitle()).isEqualTo("주말에 한강에서 자전거를 탔어요");
        assertThat(response.getWriterName()).isEqualTo("정혁");

        // isEqualTo(값): 예상값과 같은지 확인한다.
        // isNotNull(): null이 아닌지 확인한다.
        // isTrue() / isFalse(): 조건이 참인지 / 거짓인지 확인한다.
        // hasSize(개수): 목록 등의 크기가 예상한 개수인지 확인한다.
        // isEmpty(): 목록, 문자열 등이 비어 있는지 확인한다.
    }
}
```

**코드 설명**

| 문법 | 역할 |
|---|---|
| @ExtendWith(MockitoExtension.class) | JUnit 실행에 Mockito 기능 연결 |
| @Mock | 가짜 Repository 생성 |
| @InjectMocks | 실제 Service를 만들고 가짜 의존성 주입 |
| when(...).thenReturn(...) | 특정 호출의 반환값 지정 |
| assertThat(실제값).isEqualTo(예상값) | 두 값이 같지 않으면 실패 |

여기의 ID 1은 외부 DB의 ID가 아니라 직접 만든 가짜 객체의 값입니다. memberRepository도 Service 생성자에 필요한 가짜 의존성으로 준비합니다. assertThat의 isEmpty·hasSize 등은 코드 주석에 정리되어 있으므로 실제 사용한 isEqualTo부터 이해합니다.

**실행과 예상 결과**

ArticleServiceTests를 개별 실행합니다. 이 테스트는 DB 환경변수가 없어도 실행할 수 있습니다.

실제 Service가 반환한 제목과 작성자 이름이 각각 예상과 같으면 성공합니다. thenReturn의 글 제목만 바꾸어 보면 예상값과 달라져 실패함을 확인하고 복구합니다.

### 3.5.2 MockMvc로 게시글 조회 통합 테스트

> **본 교재 참고**  
> 4.3.2 「통합 테스트」

MockMvc는 외부 서버 포트를 열고 브라우저를 호출하는 대신 MVC 요청 처리 과정을 테스트 안에서 실행합니다. 현재 예제는 Controller·Service·Repository와 MariaDB를 실제로 연결합니다.

**실습 코드**

[ArticleControllerIntegrationTests.java](../spring-rest/src/test/java/com/example/demo/controller/ArticleControllerIntegrationTests.java)

```java
package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 실제 Controller, Service, Repository와 MariaDB를 연결해 확인한다.
// integration 태그가 붙은 테스트는 Gradle의 기본 테스트 실행에서 제외한다.
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    // DisplayName은 실행 결과에 표시할 테스트 이름이다.
    @DisplayName("게시글 조회 API가 제목과 작성자 이름을 응답한다")
    // @Disabled("사유")를 붙이면 이 테스트의 실행을 건너뛴다. 클래스에 붙이면 전체를 건너뛴다.
    void findArticleById() throws Exception {
        // data.sql로 준비한 게시글에서 실제 ID를 가져온다.
        Article article = articleRepository.findAll().getFirst();

        // perform: 서버 포트를 열지 않고 지정한 HTTP 요청 처리를 실행한다.
        // jsonPath는 응답 JSON에서 확인할 값을 선택한다.
        // $: JSON 전체의 시작점 / .필드명: 객체의 필드 / [0]: 배열의 첫 항목
        // 예: $.title은 제목, $.member.name은 중첩 객체의 이름, $.content[0].title은 목록의 첫 제목이다.
        // 배열 전체가 응답이면 $[0].title을 사용한다. [*]는 배열의 모든 항목을 뜻한다.
        // 위의 중첩 객체와 배열 경로는 문법 예시이며, 실제 응답 구조에 맞춰 사용한다.
        mockMvc.perform(get("/articles/{id}", article.getId()))
                // andDo(print()): 요청 URL, 응답 상태, 응답 본문 등을 콘솔에 출력한다.
                .andDo(print())
                // andExpect: 예상 결과와 다르면 테스트가 실패한다. 여기서는 HTTP 200을 확인한다.
                .andExpect(status().isOk())
                // value: jsonPath로 선택한 값이 괄호 안의 예상값과 같은지 확인한다.
                .andExpect(jsonPath("$.title").value(article.getTitle()))
                .andExpect(jsonPath("$.writerName").value(article.getMember().getName()));
    }
}
```

**코드 설명**

perform()이 요청을 실행하고, andDo(print())가 요청·응답을 출력합니다. andExpect()는 기대한 결과를 확인합니다. jsonPath의 $는 JSON 시작, $.title은 title 필드, $.content[0].title은 content 배열 첫 객체의 title입니다. 현재 테스트 응답은 단건 DTO이므로 title·writerName을 검사합니다.

DisplayName은 실행 창에 보일 한글 이름입니다. Disabled는 테스트를 잠시 건너뛰는 애노테이션으로 현재는 설명 주석만 있으며 실제 적용하지 않았습니다. findAll().getFirst()는 샘플 데이터가 있는 상태를 전제로 하며 특정 ID를 가정하지 않습니다.

**실행과 예상 결과**

MariaDB 환경변수를 설정하고 IntelliJ의 JUnit 실행기로 통합 테스트를 개별 실행합니다. Gradle에 실행을 위임하면 다음 태그 제외 설정이 적용됩니다.

응답 상태 200, JSON 제목·작성자 이름이 DB에서 읽은 글과 같으면 성공합니다. SQL 연결 실패는 MVC 검증 이전의 준비 환경 문제이므로 접속 설정과 초기 데이터를 먼저 확인합니다.

### 3.5.3 통합 테스트 태그와 기본 빌드

개발 중 필요한 통합 테스트라도 기본 빌드 때 외부 DB를 요구하지 않도록 분류할 수 있습니다. 태그는 분류 이름이며 integration이라는 문자열 자체가 특별한 예약어는 아닙니다.

**실습 코드**

[build.gradle](../spring-rest/build.gradle)

```groovy
plugins {
	id 'java'
	id 'org.springframework.boot' version '4.2.0'
	id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-webmvc'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
	testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
	testCompileOnly 'org.projectlombok:lombok'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
	testAnnotationProcessor 'org.projectlombok:lombok'
}

tasks.named('test') {
	useJUnitPlatform {
		// test와 build 실행 시 실제 DB가 필요한 통합 테스트를 제외한다.
		excludeTags 'integration'
	}
}
```

**코드 설명**

test 태스크의 excludeTags 'integration'이 해당 태그의 테스트를 기본 test·build에서 제외합니다. @Tag만 붙여 놓으면 자동으로 제외되지 않습니다. 현재 모듈에는 별도 integrationTest 태스크를 만들지 않았습니다.

**실행과 예상 결과**

모듈 폴더에서 `./gradlew.bat test`를 실행하면 태그 제외 설정으로 기본 테스트를 실행합니다. DB 통합 테스트는 IDE의 JUnit 실행기로 따로 실행합니다.

기본 Gradle 테스트는 실제 MariaDB를 쓰는 integration 태그 테스트를 건너뜁니다. Gradle 플러그인·의존성 다운로드 실패는 테스트의 성공·실패 판정과 별개의 실행 환경 문제입니다.

## 3.6 학습 확인

- @PathVariable·@RequestParam·@RequestBody가 각각 어디의 값을 받는지 설명합니다.
- Member API와 Article API의 계층 구조를 비교합니다.
- 일괄 저장 중 예외를 밖으로 전달하는 이유를 설명합니다.
- Mockito의 가짜 Repository와 실제 Service를 구분합니다.
- MockMvc 통합 테스트의 DB 준비가 단위 테스트와 다른 이유를 설명합니다.

[목차](README.md) · [이전: 2.3 JPA](02-3-jpa.md) · [다음: 4장 Spring MVC](04-spring-mvc.md)
