package uc.dev.uc_info.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 장학금 등록/수정 폼 요청 + 목록 화면 표시 겸용 DTO.
 *
 * <p>등록/수정 폼의 입력값을 받고,
 * 목록 화면에서도 장학금 정보를 표시할 때 사용한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ScholarshipDTO {

    /** 장학금 PK. 목록 표시 및 수정 식별용 */
    private Long scholarshipId;

    /** 장학금명 */
    @NotBlank(message = "장학금명은 필수입니다.")
    private String name;

    /** 장학금 유형: REGIONAL / GRADE / INTERNAL / EXTERNAL */
    @NotBlank(message = "장학금 유형은 필수입니다.")
    private String type;

    /** 대상 학과 PK. null이면 전체 대상 */
    private Long deptId;

    /** 대상 학년. null이면 전체 학년 */
    private String targetGrade;

    /** 거주 조건. 선택 입력 */
    private String residenceCondition;

    /** 신청 마감일 */
    private LocalDate deadline;

    /** 학생 앱 노출 여부 */
    private Boolean visible;

    /** 연결 공지 PK. null이면 연결 안 함 */
    private Long noticeId;
}