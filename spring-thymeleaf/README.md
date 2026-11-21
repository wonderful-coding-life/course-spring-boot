# spring-thymeleaf

회원 예제로 Thymeleaf 기본 문법을 실습합니다. DB 연결은 필요하지 않습니다.
교재는 **『이것이 스프링 부트다 with 자바』**의 5장 「스프링 부트로 게시판 만들기」를 참고합니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| Controller와 화면 연결 | [ThymeleafController](src/main/java/com/example/demo/controller/ThymeleafController.java), [실습 메뉴](src/main/resources/templates/index.html) | 5.2.1 「타임리프 프로젝트 생성」 | `@Controller`, `Model`, 템플릿 이름 반환으로 HTML을 응답합니다. |
| 화면에 전달할 회원 객체 | [Member](src/main/java/com/example/demo/model/Member.java), [ThymeleafController](src/main/java/com/example/demo/controller/ThymeleafController.java) | 5.2.3 「타임리프 표현식」의 「자바 객체 사용하기」 | 회원 객체와 목록을 Model에 담아 전달합니다. |

## 실행 방법

1. `DemoApplication`을 실행합니다.
2. 브라우저에서 <http://localhost:8080>을 엽니다.
3. 메뉴 순서대로 화면과 HTML 코드를 함께 확인합니다.

다른 웹 프로젝트가 8080 포트를 사용하고 있다면 먼저 종료합니다.

## 문법 실습 순서와 교재 연결

| 순서 | 화면 | 학습 내용 | 교재에서 찾아볼 곳 |
|---|---|---|---|
| 1 | [text.html](src/main/resources/templates/text.html) | `Model`, `th:text`, `${...}`, 객체 속성과 문장 출력 | 5.2.1 「타임리프 프로젝트 생성」, 5.2.3 「타임리프 표현식」 |
| 2 | [condition.html](src/main/resources/templates/condition.html) | `th:if`, `th:unless`, 나이에 따른 문구 표시 | 5.2.3 「타임리프 표현식」 |
| 3 | [list.html](src/main/resources/templates/list.html) | `th:each`, 반복 순번, 회원 4명 출력 | 5.2.3 「타임리프 표현식」 |
| 4 | [link.html](src/main/resources/templates/link.html) | `th:href`, `@{...}`, 쿼리 파라미터로 회원 선택 | 5.2.3 「타임리프 표현식」 |
| 5 | [form.html](src/main/resources/templates/form.html), [result.html](src/main/resources/templates/result.html) | `th:action`, `th:object`, `th:field`, `*{...}`, 폼 입력과 결과 출력 | 5.2.3 「타임리프 표현식」, 5.4.4 「사용자 로그인 구현」의 회원가입 폼, 5.4.5 「게시판 웹 화면 구현」의 게시글 입력 폼 |
| 6 | [utility.html](src/main/resources/templates/utility.html) | `#temporals.format()`, 날짜·시간 출력 | 5.2.3 「타임리프 표현식」 |
| 7 | [common.html](src/main/resources/templates/common.html) | `th:fragment`, `th:replace`, 공통 메뉴와 하단 재사용 | 5.2.4 「타임리프의 레이아웃 관리」 |

[ThymeleafController](src/main/java/com/example/demo/controller/ThymeleafController.java)에서 화면에 전달하는 데이터를 확인합니다.
[Member](src/main/java/com/example/demo/model/Member.java)는 화면 출력과 폼 입력에 사용하는 객체입니다.

회원 샘플은 윤서준·윤광철·공미영·김도윤이며 이메일 도메인은 `@campus.com`입니다.
폼에는 정혁 / `JungHyuk@campus.com` / 10살을 입력하고 제출해 봅니다. 입력값은 결과 화면에만 표시됩니다.

조건문 실습은 Controller에서 전달하는 회원의 나이를 변경해 두 문구를 확인합니다.
공통 화면 조각은 마지막 순서에서 각 HTML의 `th:replace`와 함께 살펴봅니다.

[전체 강의 안내](../README.md) · [이전: spring-rest](../spring-rest/README.md)
