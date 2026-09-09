package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 개설과목 목록 응답(GET /api/courses) DTO. 학생이 이 중에서 담을 과목을
 * 고른다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CourseOfferingResponse {

    /** 개설과목 PK. 담을 때(POST /api/enrollments) 이 값을 courseOfferingId로 보낸다 */
    private Long id;

    /** 과목명 */
    private String subject;

    /** 요일: 월/화/수/목/금 */
    private String day;

    /** 시작 교시(시각) */
    private Integer startHour;

    /** 종료 교시(시각) */
    private Integer endHour;

    /** 강의실 */
    private String room;

    /** 담당 교수 */
    private String professor;
}