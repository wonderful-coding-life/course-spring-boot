package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

// 로그인에 사용할 회원 정보이다. 비밀번호는 BCrypt 해시로 저장한다.
@Entity
@Table(name = "member")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private String password;
    // 한 회원에게 하나의 권한을 지정한다. 예: ROLE_USER, ROLE_ADMIN
    private String authority;
}
