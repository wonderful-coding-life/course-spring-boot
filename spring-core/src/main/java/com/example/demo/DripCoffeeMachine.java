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
