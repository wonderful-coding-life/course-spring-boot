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

		CoffeeOrder order = CoffeeOrder.builder().coffeeName("드립커피").quantity(3).build();
		cafe.orderCoffee(order);

		SpringCafe cafe2 = context.getBean(SpringCafe.class);
		SpringCafe cafe3 = new SpringCafe(new DripCoffeeMachine());

		log.info("cafe == cafe2 : {}", cafe.equals(cafe2)); // true
		log.info("cafe == cafe3 : {}", cafe.equals(cafe3)); // false
	}

}
