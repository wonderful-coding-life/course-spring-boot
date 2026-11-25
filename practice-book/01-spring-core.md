# 1장 스프링 코어

> **본 교재 참고**  
> 1장 「스프링 부트 이해하기」의 1.1 「스프링 부트란 무엇인가?」, 1.2 「스프링 부트의 핵심 개념」; 2장 「스프링 부트 시작하기」의 2.2 「프로젝트를 시작하기 전에 알아 두어야 할 사전 지식」

자바 프로그램은 객체들이 협력하며 동작합니다. 카페는 주문을 받고 커피 머신으로 커피를 만듭니다. 이때 카페가 사용할 머신을 누가 만들고 연결할지에 따라 코드의 구조가 달라집니다. 스프링은 이러한 객체 생성과 연결을 도와주는 프레임워크입니다.

이 장에서는 커피 예제로 스프링 컨테이너, 빈, 의존성 주입을 익힙니다. 이어서 반복되는 자바 코드를 줄이는 롬복과 실행 결과를 관찰하는 로그를 배웁니다.

**학습 목표**

- 스프링이 관리하는 객체와 직접 만든 객체를 구분한다.
- 인터페이스와 생성자 주입으로 객체를 연결한다.
- 롬복으로 객체를 만들고, 로그로 실행 결과를 확인한다.

**준비**: Java 21과 IntelliJ IDEA에서 `spring-core`를 엽니다. DB는 필요하지 않습니다. 이 장의 소스는 `com.example.demo` 패키지에 있습니다.

## 1.1 스프링 부트 프로젝트 이해하기

> **본 교재 참고**  
> 1.1 「스프링 부트란 무엇인가?」, 2.1.6 「스프링 이니셜라이저」

**프로젝트 생성과 의존성 선택**

Spring Initializr에서 다음과 같이 설정하고 Dependencies에 **Lombok**을 추가합니다. Lombok은 생성자·getter 같은 반복 코드를 줄여 줍니다.

![Spring Core 프로젝트 생성 설정과 Lombok 의존성 선택](images/01-spring-core/spring-initializr.png)

프로젝트 생성의 공통 설정과 압축 해제 방법은 교재 앞부분의 「실습 환경과 진행 방법」을 참고합니다. 생성한 프로젝트 폴더는 `spring-core`로 준비합니다.

스프링은 객체를 만들고 연결하는 기능을 비롯해 애플리케이션 개발에 필요한 기반을 제공합니다. 스프링 부트는 함께 사용할 라이브러리를 묶어 제공하고, 프로젝트에 추가한 라이브러리와 설정을 바탕으로 실행 환경을 자동으로 구성합니다. 개발자는 필요한 기능을 선택한 뒤 애플리케이션의 동작을 작성하는 데 집중할 수 있습니다.

`src/main/java`는 실행 코드, `src/main/resources`는 설정과 자원, `src/test/java`는 테스트 코드의 위치입니다. `build.gradle`은 사용할 라이브러리와 자바 버전 등을 정의합니다.

[build.gradle](../spring-core/build.gradle)

```groovy
plugins {
	id 'java'
	id 'org.springframework.boot' version '4.1.2'
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
	implementation 'org.springframework.boot:spring-boot-starter'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testCompileOnly 'org.projectlombok:lombok'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
	testAnnotationProcessor 'org.projectlombok:lombok'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

Gradle의 의존성 설정은 라이브러리를 컴파일과 실행 중 어느 단계에서 사용할지 구분합니다. 다음 표에서는 현재 프로젝트에 등장하는 설정과 이후 DB 실습에서 사용할 `runtimeOnly`를 비교합니다.

| 설정 | 사용하는 시점과 역할 | 예 |
|---|---|---|
| `implementation` | 실행 코드의 컴파일과 실행에 사용합니다. | 스프링 부트 스타터 |
| `compileOnly` | 실행 코드의 컴파일에만 사용하고 실행 의존성에는 포함하지 않습니다. | 롬복 애너테이션 |
| `annotationProcessor` | 컴파일할 때 애너테이션을 처리하여 코드를 생성합니다. | 롬복 코드 생성기 |
| `runtimeOnly` | 컴파일에는 필요하지 않고 실행할 때 사용합니다. | JDBC 드라이버 |
| `testImplementation` | 테스트 코드의 컴파일과 실행에 사용합니다. | 테스트 스타터 |

롬복을 `compileOnly`와 `annotationProcessor`에 각각 선언하는 것은 서로 다른 역할을 연결하기 위해서입니다. 전자는 소스에서 롬복 애너테이션을 사용할 수 있게 하고, 후자는 이를 읽어 생성자와 getter 등의 코드를 만듭니다. 생성된 코드로 실행하므로 이 예제에서는 실행 시 롬복 라이브러리가 필요하지 않습니다.

`testCompileOnly`, `testAnnotationProcessor`, `testRuntimeOnly`는 같은 구분을 테스트 코드에 적용한 설정입니다. IntelliJ에서 Gradle 프로젝트를 불러와 의존성 동기화를 마친 뒤 실습합니다.

## 1.2 객체 생성과 의존성 주입

> **본 교재 참고**  
> 1.2.1 「의존성 주입」, 1.2.2 「제어의 역전」

카페가 커피를 만들려면 커피 머신이 필요합니다. 이처럼 한 객체가 다른 객체의 기능을 사용하는 관계를 의존 관계라고 합니다. 카페 안에서 머신을 직접 만들면 어떤 머신을 사용할지도 카페 코드가 결정하게 되지만, 외부에서 머신을 전달받으면 카페는 전달받은 머신의 기능만 사용하면 됩니다. 필요한 객체를 외부에서 전달하는 방식을 **의존성 주입(Dependency Injection, DI)**이라고 합니다.

스프링에서는 컨테이너가 객체를 만들고 필요한 곳에 의존성을 주입합니다. 컨테이너가 관리하는 객체를 빈(bean)이라고 하며, 이 예제에서는 커피 머신과 카페가 빈으로 등록됩니다. 자바의 생성자 호출과 객체 참조는 그대로 사용하면서, 객체 생성과 의존성 주입을 스프링에 맡기는 것입니다.

### 1.2.1 커피 머신의 공통 기능과 구현체

인터페이스에는 무엇을 할 수 있는지 선언합니다. 구현 클래스에는 실제 동작을 작성합니다. 카페는 구체적인 머신 종류보다 커피를 만드는 공통 기능에 의존하도록 구성합니다.

**실습 코드**

[CoffeeMachine.java](../spring-core/src/main/java/com/example/demo/CoffeeMachine.java)

```java
package com.example.demo;

public interface CoffeeMachine {
    String brew();
}
```

[DripCoffeeMachine.java](../spring-core/src/main/java/com/example/demo/DripCoffeeMachine.java)

```java
package com.example.demo;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
// CoffeeMachine 빈이 여러 개면 이 빈을 우선 주입한다.
@Primary
public class DripCoffeeMachine implements CoffeeMachine {
    @Override
    public String brew() {
        return "드립 커피";
    }
}
```

[EspressoMachine.java](../spring-core/src/main/java/com/example/demo/EspressoMachine.java)

```java
package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class EspressoMachine implements CoffeeMachine {
    @Override
    public String brew() {
        return "에스프레소";
    }
}
```

**코드 설명**

- `CoffeeMachine.brew()`는 커피 이름을 문자열로 반환하는 공통 기능입니다.
- 두 구현 클래스의 `@Component`는 스프링이 객체를 찾아 빈으로 등록하게 합니다.
- 같은 인터페이스를 구현한 빈이 둘이므로 `@Primary`가 붙은 드립 머신을 우선 선택합니다. 구현 클래스가 둘이라는 이유만으로 순서에 따라 선택하지는 않습니다.

**실행과 예상 결과**

각 클래스가 같은 패키지에 있는지 확인합니다. 실제 호출은 다음 소절에서 애플리케이션을 실행해 확인합니다.

드립 머신은 `드립 커피`, 에스프레소 머신은 `에스프레소`를 반환하도록 작성되어 있습니다.

**직접 바꿔 보기**

실습을 마친 뒤 `@Primary`를 에스프레소 머신으로 옮기고 앱을 재시작해 선택 결과를 비교합니다. 두 클래스에 동시에 붙이지 않습니다.

**@Primary와 @Qualifier 비교하기**

`@Primary`가 여러 후보 중 기본으로 선택할 빈을 정한다면, `@Qualifier`는 특정 주입 위치에서 사용할 후보를 한정합니다. 드립 머신을 기본으로 유지하면서 특정 카페에는 에스프레소 머신을 전달하고 싶을 때 사용할 수 있습니다.

| 애너테이션 | 적용 위치 | 역할 |
|---|---|---|
| `@Primary` | 빈으로 등록할 클래스 등 | 단일 빈을 주입할 때 기본 우선순위를 지정합니다. |
| `@Qualifier` | 주입받을 필드나 생성자 매개변수 등 | 주입할 빈의 후보를 이름이나 구분값으로 한정합니다. |

다음과 같이 필드에 `@Autowired`와 `@Qualifier`를 함께 지정하면, 해당 카페에서 사용할 머신을 선택할 수 있습니다.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class EspressoCafe {
    // CoffeeMachine 후보 중 espressoMachine 빈을 이 필드에 주입한다.
    @Autowired
    @Qualifier("espressoMachine")
    private CoffeeMachine coffeeMachine;
}
```

기본 이름 생성 규칙에서 `EspressoMachine`의 빈 이름은 `espressoMachine`입니다. `@Autowired`는 필드에 의존성을 주입하도록 표시하고, `@Qualifier`는 해당 이름으로 후보를 한정하므로 드립 머신에 `@Primary`가 있어도 에스프레소 머신이 주입됩니다. 이 예제의 필드는 생성자로 초기화하지 않고 객체 생성 후 스프링이 값을 설정하므로 `final`을 붙이지 않습니다.

### 1.2.2 생성자로 커피 머신 주입받기

클래스 안에서 필요한 객체를 직접 만들면 구현체를 바꾸기 위해 그 클래스를 수정해야 할 수 있지만, 생성자로 필요한 객체를 받으면 의존성을 주입하는 일을 외부에 맡길 수 있습니다. 스프링에서는 이 생성자를 이용해 빈을 주입합니다.

**실습 코드**

[SpringCafe.java](../spring-core/src/main/java/com/example/demo/SpringCafe.java)

```java
package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 이 클래스를 스프링이 관리하는 빈으로 등록한다.
@Component
// Lombok이 생성자를 만들고, 스프링이 CoffeeMachine을 주입한다.
@RequiredArgsConstructor
// 로그 출력을 위한 log 필드를 자동으로 만든다.
@Slf4j
public class SpringCafe {

    private final CoffeeMachine coffeeMachine;

    public void makeCoffee() {
        log.debug("커피를 만들고 있습니다.");
        log.trace("사용하는 커피 머신: {}", coffeeMachine.getClass().getSimpleName());
        log.info("{}를 만들었습니다.", coffeeMachine.brew());
    }

}
```

**코드 설명**

`private final CoffeeMachine coffeeMachine`은 카페가 사용할 머신을 보관하는 필드입니다. `@RequiredArgsConstructor`를 붙이면 Lombok이 이 final 필드를 초기화하는 생성자를 만듭니다. 롬복이 만드는 생성자를 자바 코드로 표현하면 다음과 같습니다.

```java
public SpringCafe(CoffeeMachine coffeeMachine) {
    this.coffeeMachine = coffeeMachine;
}
```

스프링은 SpringCafe의 생성자에 전달할 CoffeeMachine 빈을 찾습니다. 구현체가 둘이므로 @Primary가 붙은 DripCoffeeMachine을 선택해 전달합니다. 카페는 머신의 구체적인 종류를 결정하지 않고, 인터페이스에 선언된 brew()를 호출합니다. 이후 @Primary의 위치를 바꾸면 카페 코드를 고치지 않고도 사용할 머신을 바꿀 수 있습니다.

로그 문자열의 `{}` 자리에는 뒤에 전달한 값이 들어갑니다. `coffeeMachine.getClass().getSimpleName()`은 실제 커피 머신 객체의 클래스 이름을 패키지 이름 없이 반환하므로, 어떤 구현체가 선택되었는지 로그에서 확인할 수 있습니다.

**필드 주입과 비교하기**

생성자를 통해 빈을 전달받는 코드뿐 아니라 필드에 `@Autowired`를 붙여 주입받는 코드도 볼 수 있습니다. 생성자 주입 대신 필드 주입을 사용한다면, 주입받을 필드를 다음과 같이 선언합니다.

```java
@Autowired
private CoffeeMachine coffeeMachine;
```

스프링은 `@Autowired`가 붙은 필드의 타입에 맞는 빈을 찾아 주입하며, 현재처럼 CoffeeMachine 빈이 여러 개라면 `@Primary`가 붙은 드립 머신을 우선 선택합니다. 필드 주입에서는 위와 같이 final 없이 필드를 선언하고, 롬복으로 주입용 생성자를 만들 필요도 없습니다.

필드 주입은 선언이 간단하지만, 생성자 주입은 객체를 만들 때 필요한 의존성을 명확히 드러내고 스프링 없이 테스트할 때도 필요한 객체를 직접 전달하기 쉬우므로 스프링에서는 생성자 주입을 권장합니다. 생성자 주입은 생성자가 하나라면 `@Autowired`를 생략할 수 있으므로, 롬복이 생성자 하나를 만들어 주는 현재 코드에서도 애노테이션 없이 주입이 동작합니다.

**여러 빈을 리스트로 주입받기**

등록된 커피 머신의 기능을 모두 사용하려면 빈 하나를 선택하는 대신 목록으로 주입받을 수 있습니다. 기존 coffeeMachine 필드를 다음과 같이 `List<CoffeeMachine>` 타입으로 바꾸면, 같은 인터페이스를 구현한 빈을 한꺼번에 전달받습니다. List는 `java.util.List`를 사용합니다.

```java
private final List<CoffeeMachine> coffeeMachines;
```

기존처럼 클래스에 `@RequiredArgsConstructor`를 붙이면 롬복이 이 필드를 받는 생성자를 만들고, 스프링은 CoffeeMachine 타입의 빈을 찾아 리스트로 전달합니다. 따라서 드립 머신에 `@Primary`가 붙어 있어도 리스트에는 드립 머신과 에스프레소 머신이 모두 포함되며, `@Primary`는 빈 하나를 선택해 주입할 때 우선순위를 지정하는 역할을 합니다. 주입받은 리스트는 자바에서 배운 반복문으로 순회하며 각 머신의 brew()를 호출할 수 있습니다.

**로거 선언과 @Slf4j 비교하기**

로그를 출력하려면 일반적으로 `LoggerFactory.getLogger()`로 로거를 얻어 log 필드에 저장합니다. 따라서 로그를 사용하는 클래스마다 같은 형태의 선언을 반복해야 하는데, 클래스에 `@Slf4j`를 붙이면 롬복이 컴파일 과정에서 이 선언을 자동으로 만들어 반복 코드를 줄여 줍니다. 다음 두 코드는 로거를 직접 선언하는 방식과 롬복에 맡기는 방식을 각각 보여 줍니다.

직접 로거를 선언하는 경우에는 다음과 같이 작성합니다.

```java
public class SpringCafe {
    private static final Logger log =
            LoggerFactory.getLogger(SpringCafe.class);
}
```

위 코드에서는 org.slf4j 패키지의 Logger와 LoggerFactory를 import해야 하지만, 롬복을 사용하면 다음과 같이 `@Slf4j`를 붙이는 것으로 같은 역할의 필드가 만들어집니다.

```java
@Slf4j
public class SpringCafe {
}
```

따라서 실제 실습 코드에서는 log 필드를 직접 선언하지 않고도 `log.info()`와 같은 메서드를 사용할 수 있습니다. 로그 설정과 출력 수준은 1.4절에서 살펴봅니다.

**실행과 예상 결과**

다음 소절의 DemoApplication을 실행합니다. 현재 `@Primary`가 드립 머신에 있다면 카페가 드립 머신을 사용합니다.

핵심 로그는 `드립 커피를 만들었습니다.`입니다. 로그 앞의 시각·스레드·클래스 표시는 환경에 따라 달라집니다.

### 1.2.3 컨테이너에서 객체 가져오고 비교하기

앞에서는 카페가 사용할 커피 머신을 생성자로 전달받도록 작성했습니다. 개발자가 직접 객체를 만들고 의존성을 전달하는 대신 스프링 컨테이너가 빈을 생성하고 필요한 의존성을 주입하도록 맡기는 것은 **제어의 역전(Inversion of Control, IoC)**의 한 예이며, 앞서 살펴본 생성자 주입은 이를 구현하는 방법입니다.

빈의 기본 범위는 싱글톤입니다. 하나의 컨테이너에서 같은 빈을 요청하면 이미 만들어 둔 인스턴스를 공유합니다. 이 예제의 SpringCafe도 하나의 빈으로 관리되므로 두 번 요청해 같은 객체를 받습니다. 반면 new로 생성한 카페는 별도의 자바 객체입니다.

**실습 코드**

[DemoApplication.java](../spring-core/src/main/java/com/example/demo/DemoApplication.java)

```java
package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class DemoApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);

		SpringCafe cafe = context.getBean(SpringCafe.class);
		cafe.makeCoffee();

		SpringCafe cafe2 = context.getBean(SpringCafe.class);
		SpringCafe cafe3 = new SpringCafe(new DripCoffeeMachine());

		// ==는 두 변수가 같은 객체를 가리키는지 비교한다.
		log.info("cafe == cafe2 : {}", cafe == cafe2); // 같은 빈이므로 true
		log.info("cafe == cafe3 : {}", cafe == cafe3); // 직접 만든 객체는 다르므로 false
	}

}
```

**코드 설명**

`SpringApplication.run(DemoApplication.class, args)`는 DemoApplication을 설정의 시작점으로 사용해 애플리케이션을 실행합니다. 여기서 `DemoApplication.class`는 해당 클래스의 정보를 담은 Class 객체이며, 스프링은 이 클래스에 붙은 `@SpringBootApplication`을 바탕으로 애플리케이션을 구성합니다.

`@SpringBootApplication`에는 자동 설정과 컴포넌트 탐색 등의 기능이 포함되어 있는데, 컴포넌트 탐색은 기본적으로 DemoApplication이 속한 패키지와 하위 패키지에서 `@Component` 등이 붙은 클래스를 찾습니다. 스프링은 이를 바탕으로 빈을 생성하고 필요한 의존성을 주입하므로, 이 예제에서는 두 커피 머신과 SpringCafe가 빈으로 등록되고 SpringCafe에는 드립 머신이 주입됩니다.

이렇게 빈을 생성하고 의존성 주입과 생명주기를 관리하는 스프링 컨테이너를 이 예제에서는 ApplicationContext라고 부릅니다. `SpringApplication.run()`은 시작한 컨테이너를 반환하므로 이를 context 변수에 저장한 뒤, `getBean(SpringCafe.class)`를 호출해 컨테이너가 관리하는 SpringCafe 빈을 가져올 수 있습니다. 코드에서는 반환받은 컨테이너를 ConfigurableApplicationContext 타입으로 저장하며, 가져온 카페에는 이미 커피 머신이 주입되어 있으므로 바로 `makeCoffee()`를 호출할 수 있습니다.

cafe2도 같은 컨테이너에서 가져오지만 cafe3은 new로 별도로 생성하며, `==` 연산자로 두 변수가 같은 객체를 가리키는지 비교합니다. 같은 컨테이너에서 받은 cafe와 cafe2는 같은 빈이므로 true이고, 직접 만든 cafe3은 별도의 객체이므로 cafe와의 비교 결과가 false입니다.

**실행과 예상 결과**

DemoApplication의 main 왼쪽 실행 버튼을 누릅니다. 프로젝트 폴더에서 `./gradlew.bat bootRun`으로 실행할 수도 있습니다.

예상 출력에서 비교 결과를 확인합니다. 로그의 시각과 클래스 이름은 생략했습니다.

```text
드립 커피를 만들었습니다.
cafe == cafe2 : true
cafe == cafe3 : false
```

cafe와 cafe2는 컨테이너에서 같은 빈을 받은 변수입니다. cafe3은 new로 생성한 별도 객체이므로 cafe와의 == 비교 결과가 false입니다. 두 객체가 같은 종류의 커피를 만들 수 있다는 사실과, 같은 객체라는 사실을 구분합니다.

## 1.3 롬복으로 반복 코드 줄이기

> **본 교재 참고**  
> 2.2.1 「롬복」

회원·주문처럼 값을 담는 객체에는 getter, setter, 생성자 등의 반복 코드가 생깁니다. 롬복은 애노테이션을 읽어 필요한 코드를 컴파일 과정에서 생성합니다. 자바 문법 자체가 사라지는 것은 아니므로, 어떤 메서드가 만들어지는지 알고 사용해야 합니다.

### 1.3.1 주문 객체 만들기와 출력하기

`CoffeeOrder`는 커피 이름과 주문 수량을 담는 객체입니다. 이 예제에서는 롬복으로 빌더와 `toString()`을 생성하여 주문 객체를 만들고 출력합니다.

빌더는 필드 이름을 지정하며 객체를 만드는 방식입니다. 매개변수가 여러 개인 생성자보다 어떤 값을 넣는지 읽기 쉽습니다.

**실습 코드**

다음은 주문 정보를 담는 클래스에 롬복 애노테이션을 적용한 예제입니다. 롬복이 생성하는 코드에 집중할 수 있도록 필요한 부분만 발췌했습니다.

```java
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoffeeOrder {
    private String coffeeName;
    private int quantity;
}
```

다음과 같이 빌더로 주문 객체를 만들고 출력할 수 있습니다.

```java
CoffeeOrder order = CoffeeOrder.builder()
        .coffeeName("드립커피")
        .quantity(3)
        .build();
System.out.println("주문 정보: " + order);
```

**코드 설명**

| 애노테이션 | 생성되는 코드 |
|---|---|
| `@Getter`, `@Setter` | 필드를 읽고 바꾸는 메서드 |
| `@NoArgsConstructor` | 매개변수가 없는 생성자 |
| `@AllArgsConstructor` | 모든 필드를 받는 생성자 |
| `@Builder` | 필드를 지정해 객체를 만드는 빌더 |
| `@ToString` | 필드 내용을 포함한 문자열 반환 메서드 |

이 예제는 `@Builder`와 `@ToString`의 동작을 보여 주며, 나머지 애노테이션은 getter·setter와 생성자를 생성합니다.

`CoffeeOrder.builder()`로 빌더를 얻고, `coffeeName()`과 `quantity()`로 값을 지정합니다. 마지막 `build()`가 지정한 값을 담은 주문 객체를 반환합니다.

**예상 결과**

`"주문 정보: " + order`처럼 문자열에 객체를 연결하면 객체의 `toString()`이 호출됩니다. `@ToString`은 필드 이름과 값을 포함하는 `toString()`을 생성하므로 다음과 같이 출력됩니다.

```text
주문 정보: CoffeeOrder(coffeeName=드립커피, quantity=3)
```

## 1.4 설정 파일과 로그

> **본 교재 참고**  
> 2.2.2 「애플리케이션 설정」, 2.2.3 「로그 출력」

로그는 프로그램이 어느 지점까지 실행됐는지, 어떤 값을 처리했는지 기록하는 수단입니다. 실행 결과를 콘솔에 보여 주면서 파일에도 남길 수 있습니다. 로그 레벨을 이용하면 필요한 상세도만 선택할 수 있습니다.

### 1.4.1 로그 레벨과 파일 설정

대표적인 레벨을 상세한 순서로 나열하면 TRACE, DEBUG, INFO, WARN, ERROR입니다. INFO로 설정하면 INFO·WARN·ERROR가 보이며 DEBUG·TRACE는 보이지 않습니다. 특정 패키지에 더 자세한 레벨을 적용할 수 있습니다.

**실습 코드**

[application.properties](../spring-core/src/main/resources/application.properties)

```properties
spring.application.name=demo

# logging level
logging.level.root=info
logging.level.com.example.demo=trace

# logging file
logging.file.name=./log/app.log
logging.logback.rollingpolicy.file-name-pattern=./log/app.backup.%d{yyyy-MM-dd}.%i.gz
logging.logback.rollingpolicy.max-history=7
logging.logback.rollingpolicy.max-file-size=10KB
logging.logback.rollingpolicy.total-size-cap=1GB
```

[SpringCafe.java — makeCoffee()](../spring-core/src/main/java/com/example/demo/SpringCafe.java)

```java
public void makeCoffee() {
    log.debug("커피를 만들고 있습니다.");
    log.trace("사용하는 커피 머신: {}", coffeeMachine.getClass().getSimpleName());
    log.info("{}를 만들었습니다.", coffeeMachine.brew());
}
```

**코드 설명**

`logging.level.root=info`는 전체 기본 레벨입니다. `logging.level.com.example.demo=trace`가 실습 패키지에 적용되어 현재는 TRACE와 DEBUG도 보입니다. 따라서 root만 바꿔도 패키지 설정이 남아 있으면 기대한 대로 줄어들지 않을 수 있습니다.

`logging.file.name`은 현재 작업 디렉터리를 기준으로 로그 파일의 위치를 지정합니다. `max-file-size`는 파일을 교체할 크기 기준이고, `max-history`는 날짜별 보관 기간, `total-size-cap`은 보관할 로그 파일의 총 크기 제한입니다. 파일 교체는 로그가 기록되는 과정에서 처리됩니다.

**실행과 예상 결과**

앱을 실행해 콘솔과 `log/app.log`를 확인합니다. IntelliJ 실행 설정의 작업 디렉터리가 프로젝트 폴더인지 확인합니다.

현재 설정에서는 사용하는 머신의 클래스 이름을 포함한 TRACE 로그도 보입니다. 패키지 레벨을 `info`로 바꾸고 재시작하면 `makeCoffee()`의 INFO만 보입니다.

**직접 바꿔 보기**

실습 후 패키지 레벨을 원래 `trace`로 복구합니다.

## 1.5 학습 확인

- 빈으로 등록한 객체는 누가 생성하는가?
- CoffeeMachine 구현체가 둘일 때 @Primary는 어떤 역할을 하는가?
- final 필드와 @RequiredArgsConstructor가 생성자 주입으로 이어지는 이유는 무엇인가?
- cafe와 cafe2의 `==` 비교는 true이고, cafe와 cafe3은 false인 이유는 무엇인가?
- INFO 설정에서 TRACE가 보일 때 어느 설정부터 확인할 것인가?
