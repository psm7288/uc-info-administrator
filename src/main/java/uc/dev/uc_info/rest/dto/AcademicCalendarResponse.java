package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 학사일정 목록 응답(GET /api/academic-calendar) DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class AcademicCalendarResponse {

    /** 일정 PK */
    private Long id;

    /** 일정 제목 */
    private String title;

    /** 시작일 */
    private LocalDate startDate;

    /** 종료일(단일 일정이면 null) */
    private LocalDate endDate;

    /** 카테고리 코드: ACADEMIC/EXAM/REGISTRATION/VACATION/EVENT/ETC */
    private String category;
}