package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학생 시간표 항목 응답(GET /api/schedule/me) DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class ScheduleResponse {

    /** 수강선택 PK. 과목 뺄 때(DELETE /api/enrollments/{id}) 이 값을 쓴다 */
    private Long enrollmentId;

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

    /** 시간표 표시용 색상 인덱스(0~5) */
    private Integer color;
}