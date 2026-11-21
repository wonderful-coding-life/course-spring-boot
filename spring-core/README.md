# spring-core

커피 머신과 주문 예제로 스프링의 객체 관리, 의존성 주입, 롬복, 로그를 익힙니다.
교재는 **『이것이 스프링 부트다 with 자바』**이며, 아래 장·절 제목으로 찾아보면 됩니다.

## 교재와 실습 코드 연결

| 학습 주제 | 주요 실습 코드 | 교재에서 찾아볼 곳 | 실습 내용 |
|---|---|---|---|
| 인터페이스와 의존성 주입 | [CoffeeMachine](src/main/java/com/example/demo/CoffeeMachine.java), [SpringCafe](src/main/java/com/example/demo/SpringCafe.java) | 1.2.1 「의존성 주입」, 1.2.2 「제어의 역전」 | `SpringCafe`의 생성자 주입을 확인합니다. |
| 빈 등록과 구현체 선택 | [DripCoffeeMachine](src/main/java/com/example/demo/DripCoffeeMachine.java), [EspressoMachine](src/main/java/com/example/demo/EspressoMachine.java) | 1.2.2 「제어의 역전」 | `@Component`, `@Primary`로 사용할 커피 머신을 정합니다. |
| 컨테이너에서 객체 가져오기 | [DemoApplication](src/main/java/com/example/demo/DemoApplication.java) | 1.2.2 「제어의 역전」 | `getBean()`으로 가져온 객체와 `new`로 만든 객체를 비교합니다. |
| 롬복과 생성자 주입 | [SpringCafe](src/main/java/com/example/demo/SpringCafe.java), [CoffeeOrder](src/main/java/com/example/demo/CoffeeOrder.java) | 2.2.1 「롬복」 | `@RequiredArgsConstructor`, getter·setter, builder 등을 주문 예제에 적용합니다. |
| 로그 출력과 레벨 | [SpringCafe](src/main/java/com/example/demo/SpringCafe.java) | 2.2.3 「로그 출력」 | `@Slf4j`로 INFO·DEBUG·TRACE 로그를 출력합니다. |
| 로그 설정과 파일 저장 | [application.properties](src/main/resources/application.properties) | 2.2.2 「애플리케이션 설정」, 2.2.3 「로그 출력」 | 로그 레벨, 파일 이름, 로그 파일 교체와 보관 설정을 확인합니다. |

## 실행 방법

[DemoApplication](src/main/java/com/example/demo/DemoApplication.java)을 실행하고 콘솔과 `log/app.log`를 확인합니다. DB는 필요하지 않습니다.

[전체 강의 안내](../README.md) · [다음: spring-jdbc](../spring-jdbc/README.md)
