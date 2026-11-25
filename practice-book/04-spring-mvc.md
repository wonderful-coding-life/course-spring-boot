# 4장 Spring MVC

> **본 교재 참고**  
> 5장 「스프링 부트로 게시판 만들기」의 5.1 「웹 애플리케이션과 MVC 모델」, 5.2 「템플릿 엔진과 타임리프」, 5.4 「스프링 부트 게시판 애플리케이션 만들기」

앞 장에서는 클라이언트가 요청하면 서버가 JSON 데이터를 응답했습니다. 이번 장에서는 서버가 HTML을 만들어 응답하고, 사용자는 브라우저 화면에서 데이터를 조회하거나 입력합니다. Spring MVC는 이 과정을 요청을 처리하는 컨트롤러(Controller), 화면에 전달할 데이터인 모델(Model), HTML을 만드는 뷰(View)로 나누어 구성합니다.

Thymeleaf는 HTML 템플릿에 데이터를 넣어 완성된 HTML을 만드는 템플릿 엔진입니다. 회원 목록을 조회하는 요청의 흐름을 살펴보면 각 역할을 구분하기 쉽습니다.

| 순서 | 처리하는 곳 | 수행할 일 |
|---|---|---|
| 1 | 브라우저 | 회원 목록 주소로 GET 요청을 보냅니다. |
| 2 | 컨트롤러 | Repository를 호출해 회원 목록을 조회합니다. |
| 3 | Model | 조회한 목록을 members라는 이름으로 보관해 화면에 전달합니다. |
| 4 | Thymeleaf | 템플릿의 반복문에 회원 목록을 넣어 HTML을 만듭니다. |
| 5 | 브라우저 | 서버가 응답한 HTML을 화면에 표시합니다. |

브라우저는 JPA나 DB를 직접 호출하지 않습니다. 서버가 데이터 조회와 템플릿 처리를 마친 뒤 응답한 HTML을 표시합니다. REST API와 화면 요청의 차이도 반환값에서 확인할 수 있습니다.

| 구분 | REST API 예제 | MVC 화면 예제 |
|---|---|---|
| 컨트롤러 | @RestController | @Controller |
| 메서드 반환값 | 응답할 객체·DTO | 사용할 템플릿 이름 |
| 클라이언트가 받는 내용 | JSON | 서버가 만든 HTML |

**학습 순서**

1. spring-thymeleaf에서 DB 없이 값 출력·조건·반복·폼을 연습합니다.
2. spring-mvc에서 H2의 실제 데이터를 웹 화면으로 조회·등록·수정·삭제합니다.
3. 같은 회원 등록에 검증을 추가한 버전을 비교합니다.
4. 게시글 관계·페이징과 공통 오류 화면으로 확장합니다.

두 프로젝트를 동시에 8080 포트로 실행하지 않습니다. 단계가 바뀔 때 앞 앱을 종료합니다.

## 4.1 Thymeleaf 기본 문법

> **본 교재 참고**  
> 5.2.1 「타임리프 프로젝트 생성」, 5.2.3 「타임리프 표현식」

**프로젝트 생성과 의존성 선택**

먼저 Spring Initializr에서 다음 의존성을 선택하고, 생성한 프로젝트 폴더를 `spring-thymeleaf`로 준비합니다.

| 의존성 | 역할 |
|---|---|
| Spring Web | 브라우저의 요청을 Controller에서 처리합니다. |
| Thymeleaf | HTML 템플릿에 데이터를 넣어 화면을 만듭니다. |
| Lombok | 화면에 전달할 회원 객체를 간단히 작성합니다. |

![Thymeleaf 프로젝트 생성 설정과 Spring Web·Thymeleaf·Lombok 의존성 선택](images/04-spring-mvc/thymeleaf-initializr.png)

HTML의 h1·h2는 제목, p는 문단, a는 링크, table·tr·th·td는 표, form·input·button은 입력과 전송을 표현합니다. Thymeleaf의 th: 속성은 서버에서 처리한 후 최종 HTML을 만듭니다. 동적인 결과는 파일을 더블클릭하는 대신 서버 URL로 접속해 확인합니다.

이 절의 Member(`spring-thymeleaf/src/main/java/com/example/demo/model/Member.java`)는 엔티티가 아닌 단순 화면용 객체입니다. Controller가 회원 네 명을 직접 준비하므로 DB는 필요하지 않습니다. templates/common.html의 상하단 조각은 이미 준비되어 있으며 4.1.8에서 설명합니다.

### 4.1.1 Model과 화면 이름

회원 이름처럼 요청에 따라 달라지는 값을 화면에 표시하려면, 컨트롤러가 조회하거나 준비한 데이터를 템플릿에 전달해야 합니다. Model은 이 데이터를 이름과 함께 보관하여 화면에 전달하는 공간이며, Thymeleaf가 그 값을 읽어 HTML을 만듭니다.

@Controller 메서드가 "text"를 반환하면 이 예제에서는 text.html을 찾아 화면을 만듭니다. 문자열 "text" 자체를 응답 본문으로 보내는 것이 아니라 사용할 템플릿의 이름을 지정하는 것입니다.

ThymeleafController의 메서드는 다음 선언부 안에 들어갑니다. 메서드를 제외한 구조이며, DB에 연결하는 주입 필드는 필요하지 않습니다.

```java
@Controller
public class ThymeleafController {
}
```

**실습 코드**

[ThymeleafController.java — index()](../spring-thymeleaf/src/main/java/com/example/demo/controller/ThymeleafController.java)

```java
@GetMapping("/")
public String index() {
    // 반환한 이름에 해당하는 templates/index.html을 사용한다.
    return "index";
}
```

[ThymeleafController.java — sampleMembers()](../spring-thymeleaf/src/main/java/com/example/demo/controller/ThymeleafController.java)

```java
// 각 실습에서 사용할 회원 4명을 만든다. DB 연결은 필요하지 않다.
private List<Member> sampleMembers() {
    return List.of(
            Member.builder().id(1L).name("윤서준").email("SeojunYoon@campus.com").age(10).build(),
            Member.builder().id(2L).name("윤광철").email("KwangcheolYoon@campus.com").age(43).build(),
            Member.builder().id(3L).name("공미영").email("MiyeongKong@campus.com").age(23).build(),
            Member.builder().id(4L).name("김도윤").email("DoyunKim@campus.com").age(10).build()
    );
}
```

[ThymeleafController.java — text()](../spring-thymeleaf/src/main/java/com/example/demo/controller/ThymeleafController.java)

```java
@GetMapping("/text")
public String text(Model model) {
    // Model에 넣은 값은 HTML에서 같은 이름으로 사용할 수 있다.
    model.addAttribute("name", "윤서준");
    model.addAttribute("member", sampleMembers().getFirst());
    return "text";
}
```

**코드 설명**

`return "index"`는 templates/index.html, `return "text"`는 templates/text.html을 사용할 화면으로 지정합니다. model.addAttribute()의 첫 인자는 화면에서 찾을 이름이고 두 번째는 실제 데이터이며, 템플릿은 이 이름으로 값을 읽습니다.

sampleMembers()의 ID 1~4는 메모리 예제에서 직접 지정한 값입니다. DB가 생성한 ID와 구분하여, 화면에 전달하는 값을 관찰하는 데 사용합니다.

**실행과 예상 결과**

spring-thymeleaf의 DemoApplication을 실행하고 http://localhost:8080 에서 실습 메뉴를 엽니다.

값 출력·조건문·목록·링크·폼·날짜 실습으로 이동할 수 있습니다. /text에서는 윤서준 데이터를 사용합니다.

### 4.1.2 값·객체·문장 출력

HTML에 회원 이름을 직접 적어 두면 다른 회원을 보여 줄 때마다 파일을 수정해야 합니다. Model로 전달한 값을 th:text로 출력하면 같은 템플릿으로도 요청에 맞는 회원 정보를 표시할 수 있습니다.

변수 표현식 ${...}는 Model의 값을 읽고, 점(.)은 객체의 속성을 읽습니다. th:text는 태그 안의 내용을 해당 값으로 바꿉니다.

**실습 코드**

[text.html](../spring-thymeleaf/src/main/resources/templates/text.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>값과 객체 출력</title>
</head>
<body>
<header th:replace="~{common :: header}"></header>
<h2>값과 객체 출력</h2>
<!-- th:text는 태그 안의 내용을 Model의 값으로 바꾼다. -->
<p th:text="${name}">회원 이름</p>
<!-- 점(.)으로 객체의 속성에 접근한다. -->
<p th:text="${member.name}">윤서준</p>
<p th:text="${member.email}">SeojunYoon@campus.com</p>
<p th:text="${member.age}">10</p>
<!-- |...| 안에서 문장과 값을 함께 사용할 수 있다. -->
<p th:text="|${member.name}님, 환영합니다!|">회원님, 환영합니다!</p>
<footer th:replace="~{common :: footer}"></footer>
</body>
</html>
```

**코드 설명**

`${name}`은 Model에 넣은 name을 읽고, `${member.name}`은 member 객체의 이름을 읽습니다. `|...|`를 사용하면 문장 안에 값을 넣어 인사말처럼 데이터와 문구가 섞인 내용을 만들 수 있습니다.

HTML에 미리 적힌 본문은 서버가 처리하면서 실제 값으로 교체합니다. th:text는 입력값을 일반 텍스트로 출력하므로, 값에 HTML 태그가 들어 있어도 화면을 구성하는 태그로 실행하지 않습니다.

**실행과 예상 결과**

실습 메뉴에서 값과 객체 출력을 선택하거나 /text에 접속합니다.

윤서준의 이름·이메일·나이와 `윤서준님, 환영합니다!`를 확인합니다.

### 4.1.3 조건에 따라 표시

회원의 나이에 따라 서로 다른 안내 문구를 보여 주려면 조건에 맞는 내용만 화면에 포함해야 합니다. th:if는 조건이 참일 때, th:unless는 거짓일 때 해당 태그를 HTML 결과에 포함합니다.

**실습 코드**

[ThymeleafController.java — condition()](../spring-thymeleaf/src/main/java/com/example/demo/controller/ThymeleafController.java)

```java
@GetMapping("/condition")
public String condition(Model model) {
    // 회원의 나이를 20 이상으로 바꾸면 표시되는 문구가 달라진다.
    model.addAttribute("member", sampleMembers().getFirst());
    return "condition";
}
```

[condition.html](../spring-thymeleaf/src/main/resources/templates/condition.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>조건에 따라 표시</title>
</head>
<body>
<header th:replace="~{common :: header}"></header>
<h2>조건에 따라 표시</h2>
<p th:text="|${member.name}님은 ${member.age}살입니다.|">윤서준님은 10살입니다.</p>
<!-- 실습에서는 20살 이상을 성인으로 구분한다. -->
<!-- th:if는 조건이 참일 때 태그를 표시한다. -->
<p th:if="${member.age >= 20}">성인 회원입니다.</p>
<!-- th:unless는 조건이 거짓일 때 태그를 표시한다. -->
<p th:unless="${member.age >= 20}">미성년자 회원입니다.</p>
<footer th:replace="~{common :: footer}"></footer>
</body>
</html>
```

**코드 설명**

여기서는 나이가 20 이상인지에 따라 안내 문구를 선택하며, 20살은 이 예제에서 정한 표시 조건입니다. 서버가 조건에 맞는 태그만 결과에 포함하므로 브라우저에는 선택된 문구만 전달됩니다.

**실행과 예상 결과**

/condition을 엽니다. Controller에서 전달할 회원의 나이를 바꾸거나 sampleMembers의 다른 회원을 선택해 재시작하고 비교합니다.

현재 첫 회원 윤서준은 10살이므로 미성년자 문구만 표시됩니다. 43살 회원이면 성인 문구만 표시됩니다.

### 4.1.4 회원 목록 반복 출력

회원 수만큼 HTML 행을 직접 작성하면 데이터가 늘거나 줄 때마다 화면 코드도 바꾸어야 합니다. th:each를 사용하면 목록의 각 회원에 대해 같은 HTML 구조를 반복할 수 있으며, 자바 for문처럼 현재 요소를 가리킬 이름을 정해 사용합니다.

**실습 코드**

[ThymeleafController.java — list()](../spring-thymeleaf/src/main/java/com/example/demo/controller/ThymeleafController.java)

```java
@GetMapping("/list")
public String list(Model model) {
    model.addAttribute("members", sampleMembers());
    return "list";
}
```

[list.html](../spring-thymeleaf/src/main/resources/templates/list.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>회원 목록 출력</title>
</head>
<body>
<header th:replace="~{common :: header}"></header>
<h2>회원 목록</h2>
<table>
    <thead>
    <tr><th>순번</th><th>이름</th><th>이메일</th><th>나이</th></tr>
    </thead>
    <tbody>
    <!-- th:each는 회원 수만큼 행을 반복한다. status.count는 1부터 시작하는 순번이다. -->
    <tr th:each="member, status : ${members}">
        <td th:text="${status.count}">1</td>
        <td th:text="${member.name}">윤서준</td>
        <td th:text="${member.email}">SeojunYoon@campus.com</td>
        <td th:text="${member.age}">10</td>
    </tr>
    </tbody>
</table>
<footer th:replace="~{common :: footer}"></footer>
</body>
</html>
```

**코드 설명**

`member, status : ${members}`는 목록의 각 회원과 반복 상태를 제공합니다. status.count는 1부터 시작하는 순번이고 DB 회원 ID가 아닙니다. 반복을 tr에 붙였으므로 회원마다 행이 하나씩 만들어집니다.

**실행과 예상 결과**

/list를 엽니다.

회원 4명이 순번 1~4로 표시됩니다. 메모리에서 만든 List의 순서대로 윤서준·윤광철·공미영·김도윤이 나옵니다.

### 4.1.5 회원 정보 링크

목록에서 회원 이름을 눌렀을 때 그 회원의 정보를 보려면, 어떤 회원을 선택했는지 서버에 전달해야 합니다. @{...}로 만드는 링크에 회원 ID를 넣으면 컨트롤러가 @RequestParam으로 받아 해당 회원을 선택할 수 있습니다.

**실습 코드**

[ThymeleafController.java — link()](../spring-thymeleaf/src/main/java/com/example/demo/controller/ThymeleafController.java)

```java
@GetMapping("/link")
public String link(@RequestParam(name = "id", defaultValue = "1") Long id, Model model) {
    model.addAttribute("members", sampleMembers());
    // 링크의 id에 해당하는 회원을 화면에 전달한다.
    for (Member member : sampleMembers()) {
        if (member.getId().equals(id)) {
            model.addAttribute("member", member);
            break;
        }
    }
    return "link";
}
```

[link.html](../spring-thymeleaf/src/main/resources/templates/link.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>회원 정보 링크</title>
</head>
<body>
<header th:replace="~{common :: header}"></header>
<h2>회원 정보 링크</h2>
<ul>
    <li th:each="member : ${members}">
        <!-- @{...}는 URL을 만든다. 예: /link?id=1 -->
        <a th:href="@{/link(id=${member.id})}" th:text="${member.name}">윤서준</a>
    </li>
</ul>
<section th:if="${member != null}">
    <h3>선택한 회원</h3>
    <p th:text="${member.name}">윤서준</p>
    <p th:text="${member.email}">SeojunYoon@campus.com</p>
    <p th:text="|${member.age}살|">10살</p>
</section>
<footer th:replace="~{common :: footer}"></footer>
</body>
</html>
```

**코드 설명**

`@{/link(id=${member.id})}`는 /link?id=1 같은 URL을 만들며, 컨트롤러는 전달받은 ID와 같은 회원을 찾아 Model에 넣습니다. @RequestParam의 defaultValue는 id를 생략했을 때 사용할 값으로, 여기서는 화면에 제공된 회원 1~4의 링크를 눌러 선택 결과를 비교합니다.

**실행과 예상 결과**

/link에서 회원 이름을 차례로 누르고 브라우저 주소와 선택 정보가 함께 바뀌는지 확인합니다.

공미영 링크를 누르면 id=3과 공미영의 이메일·23살 정보가 표시됩니다.

### 4.1.6 입력 폼과 객체 연결

> **본 교재 참고**  
> 5.4.5 「게시판 웹 화면 구현」

회원 정보를 화면에 표시하는 데서 나아가 사용자가 입력한 이름·이메일·나이를 서버로 보내려면 입력 폼이 필요합니다. form은 입력값을 서버에 전송하며, method="post"는 POST 요청을, th:action은 보낼 주소를 지정합니다.

th:object는 폼에서 사용할 객체를 선택하고, *{속성}은 선택한 객체의 속성을 읽는 표현식입니다. 각 입력칸에 th:field를 지정하면 해당 속성과 입력값을 연결할 수 있습니다.

**실습 코드**

[ThymeleafController.java — form()](../spring-thymeleaf/src/main/java/com/example/demo/controller/ThymeleafController.java)

```java
@GetMapping("/form")
public String form(Model model) {
    // 빈 객체를 전달하면 th:field로 입력칸을 연결할 수 있다.
    model.addAttribute("member", new Member());
    return "form";
}
```

[ThymeleafController.java — submit()](../spring-thymeleaf/src/main/java/com/example/demo/controller/ThymeleafController.java)

```java
@PostMapping("/form")
public String submit(@ModelAttribute("member") Member member) {
    // 폼의 name, email, age를 객체에 담아 결과 화면에 전달한다. DB에는 저장하지 않는다.
    return "result";
}
```

[form.html](../spring-thymeleaf/src/main/resources/templates/form.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>회원 정보 입력</title>
</head>
<body>
<header th:replace="~{common :: header}"></header>
<h2>회원 정보 입력</h2>
<p>정혁 / JungHyuk@campus.com / 10살을 입력해 보세요.</p>
<!-- th:action은 전송할 URL, th:object는 입력값을 담을 객체를 지정한다. -->
<form th:action="@{/form}" th:object="${member}" method="post">
    <!-- *{name}은 선택한 member의 name이다. th:field가 id, name, value를 설정한다. -->
    <p>
        <label for="name">이름</label>
        <input type="text" th:field="*{name}" required>
    </p>
    <p>
        <label for="email">이메일</label>
        <input type="email" th:field="*{email}" required>
    </p>
    <p>
        <label for="age">나이</label>
        <input type="number" th:field="*{age}" min="0" required>
    </p>
    <button type="submit">입력 내용 확인</button>
</form>
<footer th:replace="~{common :: footer}"></footer>
</body>
</html>
```

[result.html](../spring-thymeleaf/src/main/resources/templates/result.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>입력 결과</title>
</head>
<body>
<header th:replace="~{common :: header}"></header>
<h2>입력 결과</h2>
<!-- @ModelAttribute로 전달된 객체의 값을 출력한다. -->
<dl th:object="${member}">
    <dt>이름</dt>
    <dd th:text="*{name}">정혁</dd>
    <dt>이메일</dt>
    <dd th:text="*{email}">JungHyuk@campus.com</dd>
    <dt>나이</dt>
    <dd th:text="*{age}">10</dd>
</dl>
<p>입력한 정보는 DB에 저장하지 않습니다.</p>
<a th:href="@{/form}">다시 입력하기</a>
<footer th:replace="~{common :: footer}"></footer>
</body>
</html>
```

**코드 설명**

GET /form은 빈 Member를 화면에 전달합니다. th:object로 이 객체를 선택하고 th:field를 각 입력칸에 붙이면 name·id·value가 해당 속성과 연결됩니다. 예를 들어 이름 입력칸의 name이 name이므로 서버는 전달된 값을 Member의 name 속성에 넣을 수 있습니다.

POST /form의 @ModelAttribute는 폼에서 보낸 이름·이메일·나이를 객체에 담으며, 이처럼 입력값을 객체 속성에 연결하는 과정을 바인딩이라고 합니다. 이 객체는 Model에도 들어가므로 결과 템플릿에서 입력 내용을 다시 사용할 수 있습니다.

폼 입력은 @ModelAttribute로 연결하고, 3장에서 사용한 JSON 본문은 @RequestBody로 받습니다. 같은 회원 정보를 보내더라도 본문의 형식에 따라 처리 방법을 구분합니다.

**일반 폼과 JSON의 전달 형식 비교하기**

같은 회원 정보를 보내더라도 일반 HTML 폼과 JSON 요청은 본문의 모양이 다릅니다. 파일 업로드가 없는 이 예제의 POST 폼은 기본적으로 `application/x-www-form-urlencoded` 형식을 사용하며, 입력칸의 `name`을 항목 이름으로 삼아 값을 보냅니다.

| 구분 | 일반 HTML 폼 | 3장의 JSON 요청 |
|---|---|---|
| `Content-Type` | `application/x-www-form-urlencoded` | `application/json` |
| 본문의 구성 | `항목=값`을 `&`로 연결합니다. | JSON 객체의 필드와 값으로 구성합니다. |
| 객체로 받는 방법 | `@ModelAttribute`로 바인딩합니다. | `@RequestBody`와 메시지 변환기를 사용합니다. |

다음은 이메일과 나이 두 항목만 발췌한 본문 비교 예제입니다. 첫 번째는 폼 데이터로, 이메일의 `@`는 `%40`으로 인코딩되어 전달될 수 있습니다.

```text
email=JungHyuk%40campus.com&age=10
```

같은 이메일과 나이를 JSON 본문으로 표현하면 다음과 같습니다. 폼 데이터와 항목 이름은 같지만 본문을 구성하는 문법이 다르다는 점을 비교합니다.

```json
{"email":"JungHyuk@campus.com","age":10}
```

폼의 값은 문자열로 전달되며, 스프링이 `age`를 자바 객체의 `Integer` 타입에 맞추어 변환합니다. 폼을 POST로 전송한다고 해서 본문이 자동으로 JSON이 되는 것은 아니므로, 요청 형식에 맞는 매개변수 처리 방식을 사용합니다.

**실행과 예상 결과**

/form에서 정혁 / JungHyuk@campus.com / 10을 입력하고 버튼을 누릅니다.

result.html에 입력 내용이 나타납니다. 이 프로젝트는 저장 기능이 없어서 DB에는 추가되지 않습니다. 결과 화면 새로고침 시 브라우저가 POST 재전송을 물을 수 있으며, 뒤의 실제 CRUD에서는 처리 후 리다이렉트를 배웁니다.

### 4.1.7 날짜와 시간 출력

자바의 LocalDateTime을 그대로 출력하는 대신 화면에서 읽기 쉬운 형식으로 바꿀 수 있습니다.

**실습 코드**

[ThymeleafController.java — utility()](../spring-thymeleaf/src/main/java/com/example/demo/controller/ThymeleafController.java)

```java
@GetMapping("/utility")
public String utility(Model model) {
    model.addAttribute("now", LocalDateTime.now());
    return "utility";
}
```

[utility.html](../spring-thymeleaf/src/main/resources/templates/utility.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>날짜 출력</title>
</head>
<body>
<header th:replace="~{common :: header}"></header>
<h2>현재 시간</h2>
<!-- #temporals.format은 날짜·시간을 지정한 형식의 문자열로 바꾼다. MM은 월, mm은 분이다. -->
<p th:text="${#temporals.format(now, 'yyyy-MM-dd HH:mm')}">2026-11-22 09:30</p>
<footer th:replace="~{common :: footer}"></footer>
</body>
</html>
```

**코드 설명**

#temporals.format이 날짜·시간을 문자열로 바꿉니다. yyyy는 연도, MM은 월, dd는 일, HH는 24시간제 시, mm은 분입니다. 월 MM과 분 mm을 구분합니다.

**실행과 예상 결과**

/utility를 엽니다.

현재 서버 실행 환경의 날짜와 시간이 `연도-월-일 시:분`으로 표시됩니다. 교재에 적힌 고정 예시 시각과 같아야 하는 것은 아닙니다.

### 4.1.8 공통 화면 조각

> **본 교재 참고**  
> 5.2.4 「타임리프의 레이아웃 관리」

메뉴와 하단 문구를 화면마다 복사해 두면 내용이 바뀔 때 여러 파일을 함께 수정해야 합니다. 반복되는 HTML을 공통 조각인 fragment로 모아 두면, 한곳을 수정하여 여러 화면에 같은 내용을 적용할 수 있습니다.

**실습 코드**

[common.html](../spring-thymeleaf/src/main/resources/templates/common.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>공통 화면</title>
</head>
<body>
<!-- th:fragment는 다른 화면에서 재사용할 부분에 이름을 붙인다. -->
<header th:fragment="header">
    <h1>Thymeleaf 실습</h1>
    <nav><a th:href="@{/}">실습 메뉴</a></nav>
    <hr>
</header>
<footer th:fragment="footer">
    <hr>
    <p>회원 예제로 배우는 Thymeleaf</p>
</footer>
</body>
</html>
```

**코드 설명**

`th:fragment="header"`와 footer가 이름을 정의합니다. 앞에서 본 각 화면의 `th:replace="~{common :: header}"`는 common.html의 header 조각으로 그 태그 전체를 바꿉니다. 따라서 완성된 결과의 메뉴는 여러 파일에 직접 복사할 필요가 없습니다.

**실행과 예상 결과**

common.html의 하단 안내 문구를 바꾸고 앱을 재시작하거나 템플릿을 다시 로드해 두 화면을 확인합니다.

같은 fragment를 사용하는 화면들에 변경한 문구가 나타납니다. 변경 후 문구는 원래대로 복구합니다.

## 4.2 H2와 실제 웹 실습 준비

> **본 교재 참고**  
> 3.1.1 「데이터베이스 준비」, 3.4 「JPA」; 스키마 구성은 5.4.2 「스키마 구축」

**프로젝트 생성과 의존성 선택**

이제 DB를 사용하는 별도 프로젝트를 생성합니다. Spring Initializr에서 다음 의존성을 선택하고, 생성한 프로젝트 폴더를 `spring-mvc`로 준비합니다.

| 의존성 | 역할 |
|---|---|
| Spring Web | 화면의 조회·등록·수정·삭제 요청을 처리합니다. |
| Thymeleaf | 회원과 게시글 데이터를 HTML로 표시합니다. |
| Validation | 이름·이메일·나이 등의 입력 규칙을 검사합니다. |
| Spring Data JPA | Repository로 회원과 게시글을 저장하고 조회합니다. |
| H2 Database | 메모리에서 실행되는 실습용 DB를 제공합니다. |
| Lombok | 생성자·getter 등의 반복 코드를 줄여 줍니다. |

![Spring MVC 프로젝트 생성 설정과 Spring Web·Thymeleaf·Validation·Spring Data JPA·H2 Database·Lombok 의존성 선택](images/04-spring-mvc/mvc-initializr.png)

spring-mvc는 브라우저 입력을 DB에 반영합니다. H2 메모리 DB는 별도 서버를 설치하지 않고 앱 안에서 실행됩니다. 앱을 종료하면 데이터가 사라지고 다음 실행에서 SQL로 다시 준비하므로 외부 MariaDB의 기존 데이터와 독립적으로 실습할 수 있습니다.

### 4.2.1 스키마와 데이터 자동 초기화

애플리케이션을 다시 실행할 때마다 동일한 회원과 게시글 데이터로 실습하려면 테이블과 초기 데이터를 함께 준비해야 합니다. Spring Boot는 H2와 같은 내장 메모리 DB를 사용하는 경우 애플리케이션 시작 시 schema.sql을 실행하여 테이블을 생성하고, data.sql을 실행하여 초기 데이터를 등록합니다.

이 실습 환경에서는 Hibernate도 기본적으로 엔티티를 기준으로 테이블을 자동 생성합니다. SQL 스크립트로 준비한 테이블을 Hibernate가 다시 생성하면 초기 데이터가 삭제될 수 있으므로, 다음 설정으로 Hibernate의 자동 테이블 생성 기능을 비활성화합니다.

**실습 코드**

[application.properties](../spring-mvc/src/main/resources/application.properties)

```properties
spring.application.name=demo

# 브라우저에서 /h2-console로 H2 데이터베이스를 확인한다.
spring.h2.console.enabled=true

# 테이블은 schema.sql로 생성하므로 JPA의 자동 생성을 끈다.
spring.jpa.hibernate.ddl-auto=none
```

[schema.sql](../spring-mvc/src/main/resources/schema.sql)

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

**코드 설명**

`spring.jpa.hibernate.ddl-auto=none`은 Hibernate의 자동 테이블 생성 기능을 끄는 설정입니다. 이 실습에서는 schema.sql이 테이블 생성을 담당하고, data.sql이 초기 데이터를 등록합니다. H2 기본 모드는 공통 SQL의 AUTO_INCREMENT를 지원하므로, 같은 구조의 테이블을 준비할 수 있습니다.

SQL 초기화 스크립트의 실행 범위를 정하는 `spring.sql.init.mode`의 기본값은 `embedded`입니다. 현재 실습은 H2 내장 메모리 DB를 사용하므로, `spring.sql.init.mode=always`를 별도로 설정하지 않아도 스크립트가 자동으로 실행됩니다.

data.sql은 공통 샘플(`spring-mvc/src/main/resources/data.sql`)을 사용합니다. 해당 파일을 src/main/resources에 준비하면 회원 4명과 회원별 게시글 25개가 등록되어, 앱을 시작할 때마다 같은 상태에서 실습할 수 있습니다.

**실행과 예상 결과**

spring-thymeleaf를 종료한 뒤 spring-mvc의 DemoApplication을 실행합니다. http://localhost:8080/h2-console 에서 실행 로그에 표시된 실제 jdbc:h2:mem:... 주소와 사용자 sa, 빈 비밀번호로 접속합니다.

H2 콘솔에서 다음 SQL을 실행하여 테이블 생성과 초기 데이터 등록 결과를 확인합니다.

```sql
SELECT COUNT(*) FROM member;
SELECT COUNT(*) FROM article;
```

member는 4, article은 100이 조회됩니다. H2 메모리 DB는 앱을 종료하면 데이터가 사라지고 다음 실행에서 SQL 스크립트로 다시 준비되므로, 이후 실습에서 데이터를 등록하거나 수정해도 앱을 다시 실행하면 초기 샘플 상태로 돌아갑니다.

## 4.3 Member 화면 CRUD

> **본 교재 참고**  
> 5.1.1 「웹 애플리케이션 아키텍처와 MVC 패턴」, 5.4.6 「관리자 기능 구현」

화면 CRUD에서는 입력 화면을 여는 GET과 입력값을 처리하는 POST를 나눕니다. 브라우저가 보낸 폼 값을 @ModelAttribute로 받아 회원을 저장하고, 조회 결과는 Model을 통해 화면으로 전달합니다. 기본 등록·수정에서는 Member 엔티티를 그대로 사용해 이 흐름에 집중합니다.

**시작 상태와 실행 순서**

spring-mvc를 새로 실행해 회원 4명과 게시글 100개가 준비된 상태에서 시작합니다. 기본 CRUD를 실습하는 동안에는 앱을 계속 실행해 둡니다. H2 메모리 DB는 앱을 재시작하면 초기 샘플로 돌아갑니다.

| 단계 | 수행할 작업 | 회원 상태 |
|---|---|---|
| 목록·상세 조회 | 기존 샘플을 조회합니다. | 회원 4명 |
| 기본 등록 | 정혁을 한 번 등록합니다. | 회원 5명 |
| 수정 | 등록한 정혁의 나이를 11로 바꿉니다. | 회원 5명, 정혁은 11살 |
| 삭제 | 새로 등록한 정혁을 삭제합니다. | 기존 회원 4명 |
| 삭제 제한 확인 | 게시글을 작성한 샘플 회원의 삭제를 시도합니다. | 삭제되지 않아 회원 4명 유지 |

아래 실습의 MemberController(`spring-mvc/src/main/java/com/example/demo/controller/MemberController.java`)는 MemberRepository와 ArticleRepository를 주입받습니다. 회원·게시글 엔티티 구조는 JPA 장과 같고, ArticleRepository.existsByMember()는 작성한 글이 있는지 확인할 때 사용합니다. 로그인 기능은 아직 없습니다.

MemberController의 선언부와 주입 필드는 다음과 같습니다. 메서드를 제외한 발췌이며, 목록·상세·등록·수정·삭제와 뒤의 검증 메서드를 모두 같은 클래스에서 살펴봅니다.

```java
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
}
```

### 4.3.1 회원 목록·상세 화면

Controller는 조회 결과를 Model에 넣고 화면 이름을 반환합니다. 템플릿은 이름이 일치하는 Model 값을 사용해 회원 목록과 상세 정보를 출력합니다.

**실습 코드**

홈 주소로 접속한 사용자도 회원 목록으로 이동할 수 있도록, MemberController와 같은 controller 패키지에 HomeController를 작성합니다.

[HomeController.java](../spring-mvc/src/main/java/com/example/demo/controller/HomeController.java)

```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "redirect:/members";
    }
}
```

이어서 MemberController에 회원 목록과 상세 조회 메서드를 작성하고, 각 메서드가 반환하는 이름에 맞춰 템플릿을 준비합니다.

[MemberController.java — list()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
@GetMapping("/members")
public String list(Model model) {
    model.addAttribute("members", memberRepository.findAll());
    return "member-list";
}
```

[MemberController.java — detail()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
@GetMapping("/members/{id}")
public String detail(@PathVariable("id") Long id, Model model) {
    model.addAttribute("member", findMember(id));
    return "member-detail";
}
```

[MemberController.java — findMember()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
private Member findMember(Long id) {
    // 조회 결과가 없으면 공통 예외 처리에서 404 화면을 표시한다.
    return memberRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));
}
```

[member-list.html](../spring-mvc/src/main/resources/templates/member-list.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>회원 목록</title>
</head>
<body>
<div th:replace="~{common :: header}"></div>
<h2>회원 목록</h2>
<table>
    <tr>
        <th>ID</th>
        <th>이름</th>
        <th>이메일</th>
        <th>나이</th>
        <th>수정</th>
    </tr>

    <!-- 회원 한 명마다 한 행을 출력한다. -->
    <tr th:each="member : ${members}">
        <td th:text="${member.id}">1</td>
        <td><a th:href="@{/members/{id}(id=${member.id})}" th:text="${member.name}">윤서준</a></td>
        <td th:text="${member.email}">SeojunYoon@campus.com</td>
        <td th:text="${member.age}">10</td>
        <td><a th:href="@{/members/{id}/edit(id=${member.id})}">수정</a></td>
    </tr>

</table>
<p th:if="${#lists.isEmpty(members)}">등록된 회원이 없습니다.</p>
<p><a th:href="@{/members/new}">회원 등록하기</a></p>
<div th:replace="~{common :: footer}"></div>
</body>
</html>
```

[member-detail.html](../spring-mvc/src/main/resources/templates/member-detail.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>회원 상세</title>
</head>
<body>
<div th:replace="~{common :: header}"></div>
<h2>회원 상세</h2>
<p th:if="${message != null}" th:text="${message}">안내 메시지</p>
<div th:object="${member}">
    <p>ID: <span th:text="*{id}">1</span></p>
    <p>이름: <span th:text="*{name}">윤서준</span></p>
    <p>이메일: <span th:text="*{email}">SeojunYoon@campus.com</span></p>
    <p>나이: <span th:text="*{age}">10</span></p>
</div>
<p><a th:href="@{/members/{id}/edit(id=${member.id})}">수정하기</a></p>
<!-- 데이터가 바뀌는 삭제 요청은 링크의 GET 대신 폼의 POST로 보낸다. -->
<form th:action="@{/members/{id}/delete(id=${member.id})}" method="post">
    <button type="submit">삭제하기</button>
</form>
<p><a th:href="@{/members}">목록으로</a></p>
<div th:replace="~{common :: footer}"></div>
</body>
</html>
```

**코드 설명**

HomeController의 `@GetMapping("/")`는 홈 요청을 처리합니다. `return "redirect:/members"`는 브라우저가 /members로 새 GET 요청을 보내도록 안내하며, 이 요청은 MemberController의 list()가 처리합니다.

목록의 회원 이름에는 실제 회원 ID가 들어간 상세 링크가 연결되며, 컨트롤러는 URL의 {id}를 @PathVariable로 받습니다. 목록의 #lists.isEmpty(members)는 회원 목록이 비어 있는지 확인하는 Thymeleaf 기능으로, 회원이 없을 때 안내 문구를 표시합니다.

findMember()는 회원을 조회하고, 찾지 못하면 조회 대상이 없음을 나타내는 NoSuchElementException을 발생시킵니다. 뒤에서 작성하는 공통 오류 처리기가 이 예외를 404 응답으로 바꾸어 안내 화면을 보여 줍니다.

상세 화면의 th:object는 member를 선택하고 *{name} 등으로 값을 출력합니다. 메뉴와 하단은 common.html의 공통 화면 조각을 사용합니다.

**실행과 예상 결과**

컨트롤러와 템플릿 작성을 마친 뒤 spring-mvc의 DemoApplication을 다시 실행하고 http://localhost:8080 으로 접속합니다. 브라우저 주소가 /members로 바뀌고 회원 4명의 목록이 표시되는지 확인합니다.

목록에서 이름을 눌러 상세 화면을 열면 선택한 회원의 이름·이메일·나이를 확인할 수 있습니다. 상세 화면의 목록 링크로 돌아온 뒤 http://localhost:8080/members 에 직접 접속해도 같은 목록이 표시되는지 확인합니다.

### 4.3.2 회원 등록

회원을 등록한 뒤 결과 화면을 바로 보여 주면, 사용자가 새로고침할 때 같은 등록 요청을 다시 보낼 수 있습니다. 저장을 마친 뒤에는 브라우저가 회원 목록을 새로 조회하도록 리다이렉트하여, 새로고침해도 조회 요청만 반복되도록 합니다.

등록 화면을 열 때는 빈 Member를 입력칸과 연결하고, 제출된 값을 저장한 뒤 redirect로 목록의 GET 요청으로 이동합니다.

**실습 코드**

[MemberController.java — addForm()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
@GetMapping("/members/new")
public String addForm(@ModelAttribute("member") Member member) {
    // 빈 회원 객체를 전달해 입력칸과 연결한다.
    return "member-add";
}
```

[MemberController.java — add()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
@PostMapping("/members")
public String add(@ModelAttribute("member") Member member) {
    // 입력한 값을 엔티티로 받고, ID는 DB가 새로 부여하게 한다.
    member.setId(null);
    memberRepository.save(member);
    // 저장 후 목록으로 이동해 새로고침해도 다시 등록되지 않게 한다.
    return "redirect:/members";
}
```

[member-add.html](../spring-mvc/src/main/resources/templates/member-add.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>회원 등록</title>
</head>
<body>
<div th:replace="~{common :: header}"></div>
<h2>회원 등록</h2>
<p>정혁 / JungHyuk@campus.com / 10살을 입력해 보세요.</p>
<!-- 입력값을 Member 엔티티에 담아 등록한다. -->
<form th:action="@{/members}" th:object="${member}" method="post">
    <!-- th:field는 회원 객체의 속성과 입력칸을 연결한다. -->
    <p>
        <label for="name">이름</label>
        <input type="text" th:field="*{name}">
    </p>
    <p>
        <label for="email">이메일</label>
        <input type="email" th:field="*{email}">
    </p>
    <p>
        <label for="age">나이</label>
        <input type="number" th:field="*{age}">
    </p>
    <button type="submit">등록하기</button>
</form>
<p><a th:href="@{/members}">목록으로</a></p>
<div th:replace="~{common :: footer}"></div>
</body>
</html>
```

**코드 설명**

GET /members/new의 @ModelAttribute가 빈 객체를 준비하며, 폼의 th:object 이름 member를 컨트롤러에서 사용하는 이름과 맞춥니다. POST /members는 입력값을 엔티티로 받아 ID를 비우고 저장합니다.

`return "member-add"`는 템플릿으로 화면을 만드는 응답입니다. `return "redirect:/members"`는 브라우저에 다른 주소로 다시 요청하도록 안내합니다.

**실행과 예상 결과**

정혁 / JungHyuk@campus.com / 10으로 등록합니다.

등록이 끝나면 회원 목록에 다음 행이 추가됩니다. 아래 표는 예상 화면에서 확인할 값이며, ID에는 실제 DB가 생성한 값이 표시됩니다.

| ID | 이름 | 이메일 | 나이 |
|---|---|---|---|
| 생성된 ID | 정혁 | JungHyuk@campus.com | 10 |

목록에는 기존 회원을 포함해 5명이 표시됩니다. 브라우저 개발자 도구의 Network에서는 등록 POST의 302 응답과, 이어지는 목록 GET의 200 응답을 확인할 수 있습니다.

이처럼 처리 후 다른 주소로 다시 요청하도록 안내하는 것을 리다이렉트라고 합니다. Model에 데이터를 넣고 템플릿을 바로 반환하는 경우와 달리 브라우저가 새 요청을 보냅니다. 목록 컨트롤러는 그 새 요청에서 DB를 다시 조회하고 Model을 준비합니다.

| 순서 | 요청·응답 | 결과 |
|---|---|---|
| 1 | POST /members | 회원을 저장합니다. |
| 2 | 302 이동 응답 | 브라우저에 /members를 다시 요청하도록 안내합니다. |
| 3 | GET /members | 저장된 회원을 포함한 목록을 조회해 표시합니다. |

등록 후 표시된 목록에서 새로고침하면 GET 조회만 반복됩니다. 앞의 DB 없는 폼 예제에서 POST 결과 화면을 바로 반환했을 때와 비교하면, 리다이렉트를 사용하는 이유를 이해할 수 있습니다.

### 4.3.3 회원 수정

수정 화면에는 기존 값을 채워 보여 주고, 저장 요청에서는 URL ID로 찾은 회원을 갱신합니다.

**실습 코드**

[MemberController.java — editForm()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
@GetMapping("/members/{id}/edit")
public String editForm(@PathVariable("id") Long id, Model model) {
    model.addAttribute("member", findMember(id));
    return "member-edit";
}
```

[MemberController.java — edit()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
@PostMapping("/members/{id}/edit")
public String edit(@PathVariable("id") Long id,
                   @ModelAttribute("member") Member member) {
    // URL의 ID로 회원을 찾고, 입력한 값으로 바꾸어 저장한다.
    Member found = findMember(id);
    found.setName(member.getName());
    found.setEmail(member.getEmail());
    found.setAge(member.getAge());
    memberRepository.save(found);
    return "redirect:/members";
}
```

[member-edit.html](../spring-mvc/src/main/resources/templates/member-edit.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>회원 수정</title>
</head>
<body>
<div th:replace="~{common :: header}"></div>
<h2>회원 수정</h2>
<!-- 조회한 Member 엔티티의 값을 입력칸에 표시한다. 수정 대상 ID는 URL로 보낸다. -->
<form th:action="@{/members/{id}/edit(id=${member.id})}" th:object="${member}" method="post">
    <!-- th:field는 회원 객체의 속성과 입력칸을 연결한다. -->
    <p>
        <label for="name">이름</label>
        <input type="text" th:field="*{name}">
    </p>
    <p>
        <label for="email">이메일</label>
        <input type="email" th:field="*{email}">
    </p>
    <p>
        <label for="age">나이</label>
        <input type="number" th:field="*{age}">
    </p>
    <button type="submit">수정하기</button>
</form>
<p><a th:href="@{/members/{id}(id=${member.id})}">취소하기</a></p>
<div th:replace="~{common :: footer}"></div>
</body>
</html>
```

**코드 설명**

폼의 th:field가 조회한 회원 값을 표시하고, 수정 대상의 ID는 폼을 전송할 URL에 넣습니다. 저장할 때는 그 ID로 찾은 기존 엔티티에 name·email·age를 복사하므로, POST 본문의 id를 바꾸어 수정 대상을 지정하는 구조가 아닙니다.

**실행과 예상 결과**

등록한 정혁의 나이를 11로 수정하고 목록·상세에서 확인합니다.

이름·이메일은 유지되고 나이가 11로 표시됩니다. DB 변경은 HTTP 요청이 처리되면 유지되지만 H2 앱을 재시작하면 초기화됩니다.

### 4.3.4 회원 삭제와 안내 메시지

게시글의 작성자인 회원을 삭제하면 기존 글과 작성자의 연결에 문제가 생기므로, 먼저 작성한 글이 있는지 확인해야 합니다. 삭제할 수 없는 경우에는 상세 화면으로 돌아가 그 이유를 알려 주며, 이동한 화면에서 한 번 사용할 안내 문구를 전달합니다.

**실습 코드**

[MemberController.java — delete()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
@PostMapping("/members/{id}/delete")
public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    Member member = findMember(id);
    // 게시글의 작성자 정보가 사라지지 않도록 삭제를 제한한다.
    if (articleRepository.existsByMember(member)) {
        // 다음 화면에서 한 번 표시할 메시지를 전달한다.
        redirectAttributes.addFlashAttribute("message", "작성한 게시글이 있는 회원은 삭제할 수 없습니다.");
        return "redirect:/members/" + id;
    }
    memberRepository.delete(member);
    return "redirect:/members";
}
```

**코드 설명**

기존 샘플 회원은 모두 게시글 작성자이므로, existsByMember()가 true이면 삭제하지 않고 상세 화면으로 돌아갑니다. RedirectAttributes는 리다이렉트할 다음 화면에 값을 전달하는 객체이며, addFlashAttribute()로 넣은 안내는 이동한 화면에서 한 번 사용합니다.

작성한 글이 없는 회원은 delete()로 삭제한 뒤 목록으로 이동합니다. 상세 화면의 삭제 폼은 POST 요청으로 전송합니다.

**실행과 예상 결과**

먼저 새로 등록한 정혁을 삭제합니다. 이어서 샘플 작성자를 선택해 삭제 요청을 비교합니다.

정혁은 목록에서 사라집니다. 샘플 작성자는 상세로 돌아와 `작성한 게시글이 있는 회원은 삭제할 수 없습니다.`가 보입니다. 다른 회원 실습을 위해 관련 글을 무단으로 삭제할 필요는 없습니다.

## 4.4 같은 등록 기능에 검증 추가하기

> **본 교재 참고**  
> 5.4.4 「사용자 로그인 구현」의 회원가입 폼, 5.4.5 「게시판 웹 화면 구현」의 입력 검증

3장에서는 요청 객체에 검증 규칙을 선언하고 @Valid로 검사했습니다. 이번에는 같은 검증 방식을 회원 등록 폼에 적용하고, 잘못 입력한 값을 화면에서 수정할 수 있도록 오류 메시지를 표시합니다.

두 버전은 같은 MemberController 안에서 기본 add() 다음에 배치되어 있습니다. 기본 화면은 /members/new, 검증 화면은 /members/new-validation입니다. 검증 버전은 이름·이메일·나이를 입력받습니다.

### 4.4.1 MemberForm에 검증 규칙 선언

폼 객체는 화면에서 입력받을 값과 검증 규칙을 모읍니다. DB 컬럼의 NOT NULL은 null을 막지만, 공백만 있는 이름이나 이메일 형식까지 판단하지는 않습니다. MemberForm에 화면의 입력 조건을 선언하면 DB 매핑과 입력 검증을 구분할 수 있습니다.

**실습 코드**

[MemberForm.java](../spring-mvc/src/main/java/com/example/demo/form/MemberForm.java)

```java
package com.example.demo.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 회원 등록의 검증 버전에서 사용할 입력값과 검증 규칙을 모은다.
@Data
public class MemberForm {
    @NotBlank(message = "이름을 입력하세요.")
    @Size(max = 128, message = "이름은 128자 이하로 입력하세요.")
    private String name;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식으로 입력하세요.")
    @Size(max = 256, message = "이메일은 256자 이하로 입력하세요.")
    private String email;

    @NotNull(message = "나이를 입력하세요.")
    @Min(value = 0, message = "나이는 0 이상으로 입력하세요.")
    private Integer age;
}
```

**코드 설명**

MemberForm에는 3장에서 사용한 @NotBlank와 @Email에 더해 @Size로 이름과 이메일의 길이를 제한합니다. 나이의 @NotNull은 필수 입력을, @Min(0)은 0 이상 조건을 지정하며, message는 검증에 실패했을 때 표시할 문구입니다. 검증 애너테이션의 종류와 null 처리 규칙은 3.2.8절의 참고 표에서 확인합니다.

**실행과 예상 결과**

폼과 엔티티의 필드를 비교합니다. 현재 두 객체 모두 이름·이메일·나이를 다루지만 역할과 애노테이션이 다릅니다.

검증 버전은 MemberForm으로 값을 받으며, 규칙에 맞는 데이터만 Member로 옮겨 저장합니다.

### 4.4.2 검증 결과와 입력값 유지

잘못된 입력이 있을 때 입력 내용을 모두 지우면 사용자는 처음부터 다시 작성해야 합니다. 입력값을 유지하면서 어느 항목을 고쳐야 하는지 알려 줄 수 있도록, 오류 메시지와 함께 등록 화면을 다시 보여 줍니다.

@Valid는 폼 객체의 검증 규칙을 적용하도록 하며, 바로 뒤의 BindingResult는 검증 오류와 입력값을 자료형에 맞게 바꾸는 과정의 오류를 받습니다. 3장의 기본 400 오류 응답과 달리 여기서는 컨트롤러가 오류를 확인하여 화면을 선택하고, DB 조회가 필요한 이메일 중복도 직접 검사해 같은 오류 목록에 넣습니다.

| 구분 | 3장의 REST API 예제 | 현재 MVC 폼 예제 |
|---|---|---|
| 입력을 받는 방법 | @RequestBody로 JSON을 객체로 변환합니다. | @ModelAttribute로 폼 값을 객체에 연결합니다. |
| 검증 적용 | @Valid를 사용합니다. | @Valid를 사용합니다. |
| 검증 실패 처리 | 기본 400 오류 응답을 반환합니다. | BindingResult를 확인하고 등록 화면을 다시 반환합니다. |
| 오류 확인 위치 | 응답 JSON의 errors 항목입니다. | 등록 화면의 각 입력칸 옆입니다. |

**실습 코드**

[MemberController.java — addValidationForm()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
// 기본 등록에 입력 검증을 추가한 버전이다.
@GetMapping("/members/new-validation")
public String addValidationForm(@ModelAttribute("memberForm") MemberForm form) {
    return "member-add-validation";
}
```

[MemberController.java — addValidation()](../spring-mvc/src/main/java/com/example/demo/controller/MemberController.java)

```java
@PostMapping("/members/validation")
public String addValidation(@Valid @ModelAttribute("memberForm") MemberForm form,
                            BindingResult bindingResult) {
    // @Valid가 폼의 검증 규칙을 확인한다. BindingResult는 바로 뒤에 두어 오류를 받는다.
    if (memberRepository.findByEmail(form.getEmail()).isPresent()) {
        // 이메일 중복처럼 DB 조회가 필요한 검증은 직접 추가한다.
        bindingResult.rejectValue("email", "duplicate", "이미 사용 중인 이메일입니다.");
    }
    if (bindingResult.hasErrors()) {
        // 같은 화면을 반환하면 입력값과 오류 메시지가 유지된다.
        return "member-add-validation";
    }

    // 검증을 통과한 폼의 값으로 Member 엔티티를 만들어 저장한다.
    Member member = Member.builder()
            .name(form.getName()).email(form.getEmail()).age(form.getAge()).build();
    memberRepository.save(member);
    return "redirect:/members";
}
```

[member-add-validation.html](../spring-mvc/src/main/resources/templates/member-add-validation.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>회원 등록(검증)</title>
</head>
<body>
<div th:replace="~{common :: header}"></div>
<h2>회원 등록(검증)</h2>
<p>정혁 / JungHyuk@campus.com / 10살로 등록해 보세요.</p>
<!-- novalidate로 브라우저의 검증을 끄고 서버의 한글 오류 메시지를 확인한다. -->
<form th:action="@{/members/validation}" th:object="${memberForm}" method="post" novalidate>
    <!-- th:field는 폼의 입력값을 연결하고 th:errors는 해당 항목의 오류를 표시한다. -->
    <p>
        <label for="name">이름</label>
        <input type="text" th:field="*{name}">
        <span th:errors="*{name}"></span>
    </p>
    <p>
        <label for="email">이메일</label>
        <input type="email" th:field="*{email}">
        <span th:errors="*{email}"></span>
    </p>
    <p>
        <label for="age">나이</label>
        <input type="number" th:field="*{age}">
        <span th:errors="*{age}"></span>
    </p>
    <button type="submit">등록하기</button>
</form>
<p><a th:href="@{/members}">목록으로</a></p>
<div th:replace="~{common :: footer}"></div>
</body>
</html>
```

**코드 설명**

BindingResult는 검증 대상 매개변수 바로 뒤에 두며, rejectValue()로 필드별 오류를 추가할 수 있습니다. 첫 값 email은 오류가 생긴 필드, duplicate는 오류를 구별하는 코드, 마지막 문자열은 표시할 문구입니다.

hasErrors()가 true이면 같은 등록 템플릿을 반환하여 입력 객체와 오류 정보를 유지하고, th:errors로 각 항목의 메시지를 표시합니다. 폼의 novalidate는 브라우저의 내장 형식 검증을 끄고 서버가 만든 메시지를 관찰하기 위한 HTML 속성입니다.

**타입 변환 오류와 검증 오류**

`BindingResult`는 `@Valid`의 검증 결과뿐 아니라 폼 값을 객체에 바인딩할 때 발생한 오류도 담습니다. 서버는 문자열로 받은 나이를 먼저 `Integer`로 변환하고, 변환된 값이 `@Min(0)` 등의 조건을 만족하는지 검사합니다.

| 서버에 전달된 값 | 처리 단계 | 결과 |
|---|---|---|
| `age=10` | 정수 변환 후 검증 | 정수 10으로 변환되고 나이 조건을 통과합니다. |
| `age=-1` | 정수 변환 후 검증 | 정수 변환은 성공하지만 `@Min(0)` 검증에 실패합니다. |
| `age=abc` | 타입 변환 | 정수로 변환하지 못해 `typeMismatch` 바인딩 오류가 기록됩니다. |

age=abc처럼 정수로 바꿀 수 없는 값이 서버에 전달되면, 검증 규칙을 검사하기 전에 자료형 변환부터 실패합니다. 이때의 메시지는 애너테이션의 message와 다를 수 있지만, hasErrors()로 오류를 확인하여 저장하지 않고 입력 화면에서 안내할 수 있습니다.

**실행과 예상 결과**

기본 CRUD의 삭제 실습을 마쳐 기존 회원 4명만 남은 상태에서 검증 화면을 엽니다. 빈 이름·잘못된 이메일·음수 나이를 제출하고, 기존 샘플 이메일 중복도 확인합니다. 마지막으로 정혁 / JungHyuk@campus.com / 10을 입력해 정상 등록합니다.

잘못된 입력을 제출하면 다음 메시지가 각 입력칸 옆에 표시됩니다. novalidate가 브라우저의 형식 검증을 끄므로 서버가 만든 메시지를 확인할 수 있습니다.

| 항목 | 제출할 값 | 예상 오류 메시지 |
|---|---|---|
| 이름 | 빈 문자열 | 이름을 입력하세요. |
| 이메일 | wrong | 이메일 형식으로 입력하세요. |
| 나이 | -1 | 나이는 0 이상으로 입력하세요. |

이때 DB에 회원은 추가되지 않으며, 입력한 이메일과 나이는 화면에 남아 수정할 수 있습니다. 같은 템플릿을 바로 반환하므로 오류를 담은 BindingResult와 입력 객체를 다시 사용할 수 있습니다. 오류가 있을 때 리다이렉트하지 않는 이유도 여기에 있습니다.

정상 정보로 제출하면 저장 후 목록으로 리다이렉트되고, 정혁을 포함한 회원 5명이 표시됩니다. 이후 게시글 조회는 회원 수를 바꾸지 않습니다. MVC 앱을 재시작하면 다시 회원 4명과 게시글 100개의 초기 상태가 됩니다.

## 4.5 게시글 조회와 페이징

> **본 교재 참고**  
> 5.4.3 「모델 객체와 서비스 계층 작성」, 5.4.5 「게시판 웹 화면 구현」

회원에서는 CRUD 흐름을 익혔습니다. 게시글에서는 같은 작업을 모두 반복하기보다 작성자 이름을 포함한 조회와 페이지 이동을 배웁니다. Controller → Service → Repository 구조는 REST 장에서 배운 것과 같지만 JSON 대신 HTML을 응답합니다.

### 4.5.1 화면에 전달할 게시글 DTO

게시글 화면에는 제목·본문·날짜·작성자 이름이 필요하지만, 작성자 객체의 모든 정보까지 전달할 필요는 없습니다. 화면에 표시할 값을 DTO로 모아 전달하면, 템플릿은 엔티티의 내부 관계를 따라가지 않고도 필요한 값을 읽을 수 있습니다.

**실습 코드**

[ArticleResponse.java](../spring-mvc/src/main/java/com/example/demo/dto/ArticleResponse.java)

```java
package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 화면에서 사용할 게시글 정보만 담는다. 작성자는 이름으로 제공한다.
@Getter
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String description;
    private String writerName;
    private LocalDateTime created;
    private LocalDateTime updated;
}
```

[ArticleService.java](../spring-mvc/src/main/java/com/example/demo/service/ArticleService.java)

```java
package com.example.demo.service;

import com.example.demo.dto.ArticleResponse;
import com.example.demo.entity.Article;
import com.example.demo.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public Page<ArticleResponse> findAll(Pageable pageable) {
        Page<Article> page = articleRepository.findAll(pageable);
        // 각 게시글을 DTO로 바꾸고, 전체 개수와 페이지 정보는 유지한다.
        return page.map(this::toResponse);
    }

    public ArticleResponse findById(Long id) {
        // 조회 결과가 없으면 공통 예외 처리에서 404 화면을 표시한다.
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
        return toResponse(article);
    }

    private ArticleResponse toResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .writerName(article.getMember().getName())
                .created(article.getCreated())
                .updated(article.getUpdated())
                .build();
    }
}
```

**코드 설명**

ArticleResponse는 화면에서 사용할 제목·본문·날짜·작성자 이름을 담으며, 서비스의 toResponse()가 엔티티에서 이 값들을 옮깁니다. findAll()의 Page.map()은 목록의 각 엔티티를 DTO로 바꾸면서 전체 개수와 페이지 정보를 유지합니다.

findById()에서 대상을 찾지 못하면 NoSuchElementException이 발생합니다. 뒤에서 작성할 공통 오류 처리기는 이 예외를 받아 조회 대상이 없다는 안내를 표시합니다.

**실행과 예상 결과**

DTO 필드와 Service의 builder 코드를 연결해 읽습니다.

writerName에는 Article의 Member에서 꺼낸 이름이 들어갑니다. 화면은 엔티티 관계의 내부 구조 대신 DTO 속성을 사용합니다.

### 4.5.2 게시글 목록·상세 화면

목록은 Page의 content를 반복하고, 제목 링크는 실제 게시글 ID로 상세 화면을 엽니다.

**실습 코드**

[ArticleController.java — list()](../spring-mvc/src/main/java/com/example/demo/controller/ArticleController.java)

```java
@GetMapping("/articles")
public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
                   Pageable pageable, Model model) {
    // /articles?page=0처럼 조회하며, 페이지 번호는 0부터 시작한다.
    model.addAttribute("page", articleService.findAll(pageable));
    return "article-list";
}
```

[ArticleController.java — detail()](../spring-mvc/src/main/java/com/example/demo/controller/ArticleController.java)

```java
@GetMapping("/articles/{id}")
public String detail(@PathVariable("id") Long id, Model model) {
    model.addAttribute("article", articleService.findById(id));
    return "article-detail";
}
```

[article-list.html](../spring-mvc/src/main/resources/templates/article-list.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>게시글 목록</title>
</head>
<body>
<div th:replace="~{common :: header}"></div>
<h2>게시글 목록</h2>
<!-- 전체 게시글 개수와 현재 페이지를 확인한다. -->
<p th:text="|전체 ${page.totalElements}개 · 현재 ${page.number + 1} / ${page.totalPages}페이지|">전체 100개</p>
<table>
    <tr>
        <th>ID</th>
        <th>제목</th>
        <th>작성자</th>
        <th>작성일</th>
    </tr>

    <!-- page.content에는 현재 페이지의 게시글만 들어 있다. -->
    <tr th:each="article : ${page.content}">
        <td th:text="${article.id}">1</td>
        <td><a th:href="@{/articles/{id}(id=${article.id})}" th:text="${article.title}">제주도 푸른 바다</a></td>
        <td th:text="${article.writerName}">윤서준</td>
        <td th:text="${#temporals.format(article.created, 'yyyy-MM-dd HH:mm')}">2026-11-22 09:30</td>
    </tr>

</table>
<p th:if="${page.empty}">게시글이 없습니다.</p>
<!-- 공통 페이징에 페이지 정보와 게시글 목록 주소를 전달한다. -->
<div th:replace="~{common :: pagination(${page}, '/articles')}"></div>
<div th:replace="~{common :: footer}"></div>
</body>
</html>
```

[article-detail.html](../spring-mvc/src/main/resources/templates/article-detail.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>게시글 상세</title>
</head>
<body>
<div th:replace="~{common :: header}"></div>
<h2>게시글 상세</h2>
<h3 th:text="${article.title}">제주도 푸른 바다</h3>
<p th:text="|작성자: ${article.writerName}|">작성자: 윤서준</p>
<p>작성일: <span th:text="${#temporals.format(article.created, 'yyyy-MM-dd HH:mm')}">2026-11-22 09:30</span></p>
<p>수정일: <span th:text="${#temporals.format(article.updated, 'yyyy-MM-dd HH:mm')}">2026-11-22 09:30</span></p>
<!-- th:text로 본문을 일반 텍스트로 출력한다. -->
<p th:text="${article.description}">협재 해변에서 맑은 바다를 보며 여유를 즐겼다.</p>
<p><a th:href="@{/articles}">목록으로</a></p>
<div th:replace="~{common :: footer}"></div>
</body>
</html>
```

**코드 설명**

@PageableDefault는 한 페이지 10개·ID 내림차순을 기본 조회 조건으로 지정합니다. 컨트롤러는 Model에 page 또는 article을 넣고 화면 이름을 반환하며, 템플릿은 같은 이름으로 데이터를 읽습니다.

날짜는 앞에서 사용한 #temporals.format()으로 읽기 쉬운 문자열로 바꿉니다. 본문은 th:text를 사용하여 일반 텍스트로 출력합니다.

**실행과 예상 결과**

/articles에서 제목을 누르고 상세에서 목록으로 돌아옵니다.

샘플 기준으로 전체 게시글 수 100과 현재 페이지의 10개가 표시되며, ID가 큰 글부터 정렬됩니다. 제목 링크를 열면 그 글의 작성자 이름과 본문을 확인할 수 있습니다.

화면의 페이지 번호는 1부터 표시하므로, 하단의 2를 누르면 주소의 page가 1로 바뀌고 다음 10개가 나타납니다. 서버가 사용하는 페이지 번호와 화면에 표시하는 번호를 함께 확인합니다.

### 4.5.3 페이징 fragment 연결

> **본 교재 참고**  
> 5.2.4 「타임리프의 레이아웃 관리」, 5.4.5 「게시판 웹 화면 구현」

게시글을 여러 페이지로 나누어 보여 주려면 사용자가 다른 페이지로 이동할 링크도 필요합니다. 페이지 번호와 이전·다음 링크를 공통 화면 조각으로 만들면 다른 목록에서도 같은 구조를 사용할 수 있습니다.

페이징 링크는 URL의 page 값을 바꾸어 목록을 다시 요청합니다. 공통 조각에는 조회 결과인 Page와 목록 주소를 전달하여, 해당 목록의 페이지 수에 맞는 링크를 만듭니다.

**실습 코드**

[common.html](../spring-mvc/src/main/resources/templates/common.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>공통 화면</title></head>
<body>
<!-- 이름을 붙인 화면 조각을 각 화면에서 th:replace로 재사용한다. -->
<div th:fragment="header">
    <h1>Spring MVC 실습</h1>
    <div>
        <a th:href="@{/members}">회원 목록</a>
        <a th:href="@{/members/new}">회원 등록</a>
        <a th:href="@{/members/new-validation}">회원 등록(검증)</a>
        <a th:href="@{/articles}">게시글 목록</a>
    </div>
    <hr>
</div>
<!-- page는 페이지 정보, baseUrl은 목록을 조회할 주소이다. -->
<div th:fragment="pagination(page, baseUrl)" th:if="${page.totalPages > 0}">
    <!-- 첫 페이지에는 이전 링크, 마지막 페이지에는 다음 링크를 표시하지 않는다. -->
    <a th:if="${!page.first}" th:href="@{${baseUrl}(page=${page.number - 1}, size=${page.size})}">이전</a>
    <!-- 서버의 페이지 번호는 0부터 시작하지만 화면에는 1을 더해 표시한다. -->
    <span th:each="number : ${#numbers.sequence(0, page.totalPages - 1)}">
        <strong th:if="${number == page.number}" th:text="${number + 1}">1</strong>
        <a th:unless="${number == page.number}"
           th:href="@{${baseUrl}(page=${number}, size=${page.size})}" th:text="${number + 1}">2</a>
    </span>
    <a th:if="${!page.last}" th:href="@{${baseUrl}(page=${page.number + 1}, size=${page.size})}">다음</a>
</div>
<div th:fragment="footer">
    <hr>
    <p>H2 메모리 DB를 사용합니다. 앱을 다시 실행하면 샘플 데이터로 시작합니다.</p>
</div>
</body>
</html>
```

**코드 설명**

`pagination(page, baseUrl)`은 페이지 정보와 목록 주소를 받는 화면 조각이며, 목록 화면의 `common :: pagination(${page}, '/articles')`에서 사용합니다. #numbers.sequence(0, totalPages-1)은 시작 번호부터 마지막 번호까지의 숫자 목록을 만드는 Thymeleaf 기능입니다.

서버의 페이지 번호는 0부터 시작하므로 화면에는 1을 더해 표시하고, 현재 페이지는 strong 태그로 굵게 표시합니다. 첫 페이지에서는 이전 링크를, 마지막 페이지에서는 다음 링크를 만들지 않으며, 페이지가 없으면 번호 목록 생성도 건너뜁니다.

**실행과 예상 결과**

페이지 1·2·10을 클릭하며 주소의 page=0·1·9와 굵게 표시된 번호를 확인합니다.

100개·크기 10이면 10페이지입니다. 첫 페이지에는 이전이 없고 마지막 페이지에는 다음이 없습니다. 링크에 size도 전달하므로 현재 페이지 크기를 유지합니다.

## 4.6 공통 오류 처리

여러 컨트롤러에서 같은 예외를 각각 처리하면 오류 처리 코드와 안내 문구가 여러 곳에 흩어집니다. 공통 오류 처리기를 두면 예외 종류에 따라 응답 상태와 화면을 일관되게 정할 수 있습니다.

@ControllerAdvice는 여러 컨트롤러의 예외를 처리할 클래스를 지정하고, @ExceptionHandler는 각 메서드가 받을 예외 종류를 정합니다. 화면에 표시한 숫자만으로 HTTP 상태가 바뀌지는 않으므로 @ResponseStatus로 실제 응답 상태도 지정합니다.

### 4.6.1 400·404·409·500 예외 처리

숫자 변환 실패, 조회 대상 없음, DB 제약 위반, 예상하지 못한 오류를 구분해 같은 오류 화면에 전달합니다.

**실습 코드**

[GlobalExceptionHandler.java](../spring-mvc/src/main/java/com/example/demo/controller/GlobalExceptionHandler.java)

```java
package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.NoSuchElementException;

// 여러 Controller에서 발생한 예외를 한곳에서 처리한다.
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Model model) {
        // /members/abc처럼 숫자인 ID에 문자를 보내면 400으로 응답한다.
        model.addAttribute("status", 400);
        model.addAttribute("message", "회원 ID 등 숫자로 입력해야 하는 값을 확인하세요.");
        return "error";
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex, Model model) {
        // @ResponseStatus는 실제 HTTP 응답을 404로 설정한다.
        // Model의 status와 message는 오류 화면에 표시할 값이다.
        model.addAttribute("status", 404);
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(DataIntegrityViolationException ex, Model model) {
        // 중복 이메일 등 DB 제약 위반은 409로 응답한다. 상세 원인은 로그에 남긴다.
        log.error("데이터 제약 위반으로 요청을 처리하지 못했습니다.", ex);
        model.addAttribute("status", 409);
        model.addAttribute("message", "데이터 제약으로 요청을 처리할 수 없습니다. 중복 이메일 등을 확인하세요.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        // 예상하지 못한 오류는 자세한 내용을 로그에 남기고 공통 안내를 표시한다.
        log.error("요청 처리 중 예상하지 못한 오류가 발생했습니다.", ex);
        model.addAttribute("status", 500);
        model.addAttribute("message", "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        return "error";
    }
}
```

[error.html](../spring-mvc/src/main/resources/templates/error.html)

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>오류 안내</title>
</head>
<body>
<div th:replace="~{common :: header}"></div>
<h2>요청을 처리할 수 없습니다.</h2>
<!-- 공통 예외 처리에서 전달한 상태 코드와 안내 메시지를 표시한다. -->
<p>상태 코드: <span th:text="${status}">500</span></p>
<p th:text="${message}">처리 중 오류가 발생했습니다.</p>
<p><a th:href="@{/members}">회원 목록으로</a></p>
<div th:replace="~{common :: footer}"></div>
</body>
</html>
```

**코드 설명**

숫자로 받을 ID에 문자가 들어오는 등 요청값을 필요한 자료형으로 바꾸지 못하면 MethodArgumentTypeMismatchException이 발생하며, 여기서는 400으로 응답합니다. 조회 대상이 없을 때의 NoSuchElementException은 404로, DB 제약 위반인 DataIntegrityViolationException은 409로 처리합니다.

그 밖에 이 처리기로 전달되는 Exception은 500으로 처리하고, log.error("문구", ex)로 안내 문구와 예외의 상세 내용을 로그에 기록합니다. 사용자 화면에는 정해진 메시지만 표시하며, 회원 등록의 BindingResult 오류는 이 처리기를 거치지 않고 원래 입력 화면에서 안내합니다.

**실행과 예상 결과**

`/members/abc`, `/articles/abc`에 접속해 숫자 변환 오류를 확인하고, 없는 숫자 ID의 회원·게시글도 조회합니다. 기본 회원 등록에서 기존 이메일로 등록해 DB 예외도 확인합니다. 브라우저 Network에서 상태 코드를 확인합니다.

응답 상태와 화면에 표시된 내용은 다음 기준으로 비교합니다.

| 상황 | HTTP 상태 | 표시할 내용 |
|---|---|---|
| /members/abc처럼 숫자 ID에 문자 전달 | 400 | 숫자로 입력할 값을 확인하도록 안내하는 오류 화면 |
| 존재하지 않는 숫자 ID 조회 | 404 | 회원 또는 게시글을 찾을 수 없다는 오류 화면 |
| 기본 등록에서 중복 이메일 저장 | 409 | DB 제약 위반을 안내하는 오류 화면 |
| 검증 등록에서 잘못된 입력 또는 이메일 중복 | 200 | 입력값과 필드별 오류를 유지한 등록 화면 |
| 예상하지 못한 서버 오류 | 500 | 공통 오류 화면 |

검증 버전은 BindingResult로 오류를 확인하고 입력 화면을 다시 보여 주므로, 잘못된 입력이라도 화면을 정상 반환한 응답 상태는 200입니다. 예상하지 못한 서버 오류는 공통 처리기에서 500 응답과 안내 화면으로 처리합니다.

## 4.7 학습 확인

- @Controller의 문자열 반환과 @RestController의 객체 반환을 비교합니다.
- 화면 이름 반환과 redirect 반환이 브라우저에서 어떻게 다른지 설명합니다.
- 기본 등록과 검증 등록에서 같은 이메일 중복이 어디서 처리되는지 설명합니다.
- page=0이 화면의 1페이지인 이유를 말해 봅니다.
- 화면에 404 숫자를 출력하는 것과 HTTP 404 상태를 설정하는 차이를 설명합니다.
