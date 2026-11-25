package com.example.demo.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 회원가입의 입력값과 검증 규칙을 모으며, 확인용 비밀번호는 저장하지 않는다.
@Data
public class MemberForm {
    @NotBlank(message = "이름을 입력하세요.")
    @Size(max = 128, message = "이름은 128자 이하로 입력하세요.")
    private String name;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식으로 입력하세요.")
    @Size(max = 256, message = "이메일은 256자 이하로 입력하세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상으로 입력하세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String passwordConfirm;
}
