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

    /*
     * 주요 Validation 애노테이션
     * @NotNull: null을 허용하지 않는다. 빈 문자열과 공백은 허용한다.
     * @NotEmpty: null과 빈 문자열·빈 컬렉션을 허용하지 않는다. 공백 문자열은 허용한다.
     * @NotBlank: 문자열이 null이거나 비어 있거나 공백만 있으면 오류이다.
     * @Size(min = 2, max = 20): 문자열 길이 또는 컬렉션·배열의 크기를 제한한다.
     * @Email: 문자열이 이메일 형식인지 확인한다. 실제 존재하는 주소인지는 확인하지 않는다.
     * @Min(0), @Max(150): 숫자의 최솟값과 최댓값을 제한한다. 경계값도 허용한다.
     * @Positive: 0보다 큰 숫자만 허용한다.
     * @PositiveOrZero: 0 이상의 숫자를 허용한다.
     * @Pattern(regexp = "[0-9]+"): 숫자로만 된 문자열인지 확인한다. 정규식으로 규칙을 지정한다.
     * @Past, @Future: 날짜·시간이 현재보다 과거인지, 미래인지 확인한다.
     *
     * @Size, @Email, 숫자·날짜·정규식 검증은 null을 허용하므로 필수값에는
     * @NotNull 또는 문자열용 @NotBlank 등을 함께 사용한다.
     * message는 검증 실패 시 표시할 문구이다.
     * Controller의 입력 객체에 @Valid를 붙이면 선언한 검증 규칙을 확인한다.
     */
}
