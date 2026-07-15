package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserSignupDto {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    // max = 16 추가
    @Size(min = 3, max = 16, message = "아이디는 3글자 이상 16글자 이하로 설정해 주세요.")
    // 공백 없는 영문/숫자만 허용
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "아이디는 띄어쓰기 없이 영어와 숫자만 사용할 수 있습니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 4, max = 16, message = "비밀번호는 4글자 이상 16글자 이하로 설정해 주세요.")
    // 공백 없는 영문/숫자만 허용
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "비밀번호는 띄어쓰기 없이 영어와 숫자만 사용할 수 있습니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String passwordConfirm;

    @NotBlank(message = "별명은 필수 입력 항목입니다.")
    @Size(min = 2, max = 10, message = "별명은 2글자 이상 10글자 이하로 설정해 주세요.")
    // 한글, 영어, 숫자 조합 가능하되 공백(띄어쓰기)은 원천 차단
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "별명은 띄어쓰기 없이 한글, 영어, 숫자만 사용할 수 있습니다.")
    private String nickname;
}