package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

// 클래스명과 테이블명이 다르면 @Table(name), 필드명과 컬럼명이 다르면 @Column(name)으로 매핑한다.
@Entity
@Table(name = "member")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;
    // 이메일의 NOT NULL, UNIQUE 제약은 resources/schema.sql에서 정의한다.
    @Column(name = "email")
    private String email;
    @Column(name = "age")
    private Integer age;
}
