package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DB에 저장하지 않고 화면 출력과 폼 입력에 사용하는 회원 객체이다.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long id;
    private String name;
    private String email;
    private Integer age;
}
