package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 오늘의 식단 응답(GET /api/meal/today) DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class MealResponse {

    /** 표시용 날짜. "2026년 4월 29일 화요일" 형식 */
    private String date;

    /** 조식. 데이터 없으면 null */
    private MealSlotResponse breakfast;

    /** 중식. 데이터 없으면 null */
    private MealSlotResponse lunch;

    /** 석식. 데이터 없으면 null */
    private MealSlotResponse dinner;
}