package com.example.demo;

import lombok.*;

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
