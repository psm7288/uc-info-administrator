package uc.dev.uc_info.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학생 인증 요청(POST /api/auth/verify) DTO. 이름+학과+학번으로 본인을
 * 확인한다
 */
@Getter
@Setter
@NoArgsConstructor
public class VerifyRequest {

    /** 학생 이름 */
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    /** 학과명(예: "컴퓨터공학과"). Department PK가 아니라 이름 문자열로 받는다 */
    @NotBlank(message = "학과는 필수입니다.")
    private String department;

    /** 학번 */
    @NotBlank(message = "학번은 필수입니다.")
    private String studentId;
}