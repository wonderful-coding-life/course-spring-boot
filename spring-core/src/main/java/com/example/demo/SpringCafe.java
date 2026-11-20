package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringCafe {

    // private static final Logger log = LoggerFactory.getLogger(SpringCafe.class);

    private final CoffeeMachine coffeeMachine;

    public void makeCoffee() {
        log.debug("커피를 만들고 있습니다.");
        log.trace("사용하는 커피 머신: {}", coffeeMachine.getClass().getSimpleName());
        log.info("{}를 만들었습니다.", coffeeMachine.brew());
    }

    public void orderCoffee(CoffeeOrder order) {
        log.info("주문 정보: {}", order);
    }
}
