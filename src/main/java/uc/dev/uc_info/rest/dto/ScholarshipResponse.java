package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 장학금 목록/상세 응답(GET /api/scholarships, GET /api/scholarships/{id})
 * 공용 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class ScholarshipResponse {

    /** 장학금 PK */
    private Long id;

    /** 장학금명 */
    private String title;

    /** 유형 코드: REGIONAL / GRADE / INTERNAL / EXTERNAL */
    private String type;

    /** 신청 마감일 */
    private LocalDate deadline;
}