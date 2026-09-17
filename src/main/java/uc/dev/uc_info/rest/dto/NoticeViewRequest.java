package uc.dev.uc_info.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공지 열람 트래킹 요청(POST /api/notices/{id}/view) DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class NoticeViewRequest {

    /** 열람한 학생의 학번 */
    @NotBlank(message = "학번은 필수입니다.")
    private String studentId;
}