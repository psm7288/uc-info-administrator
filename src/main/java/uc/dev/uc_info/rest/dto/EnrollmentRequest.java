package uc.dev.uc_info.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 과목 담기 요청(POST /api/enrollments) DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class EnrollmentRequest {

    /** 담을 개설과목 PK */
    @NotNull(message = "개설과목 ID는 필수입니다.")
    private Long courseOfferingId;
}