package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class EspressoMachine implements CoffeeMachine {
    @Override
    public String brew() {
        return "에스프레소";
    }
}
