package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

// 상품 CRUD는 앞에서 배운 MVC 방식으로 처리한다.
@Entity
@Table(name = "product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer price;
}
