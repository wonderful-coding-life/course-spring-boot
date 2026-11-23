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
