package com.example.demo.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 회원 등록의 검증 버전에서 사용할 입력값과 검증 규칙을 모은다.
@Data
public class MemberForm {
    @NotBlank(message = "이름을 입력하세요.")
    @Size(max = 128, message = "이름은 128자 이하로 입력하세요.")
    private String name;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식으로 입력하세요.")
    @Size(max = 256, message = "이메일은 256자 이하로 입력하세요.")
    private String email;

    @NotNull(message = "나이를 입력하세요.")
    @Min(value = 0, message = "나이는 0 이상으로 입력하세요.")
    private Integer age;
}
