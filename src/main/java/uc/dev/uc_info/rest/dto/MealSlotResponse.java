package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 끼니 하나(조식/중식/석식)의 응답.
 */
@Getter
@Setter
@NoArgsConstructor
public class MealSlotResponse {

    /** 배식 시간(예: "08:00-09:30") */
    private String time;

    /** 메뉴 항목 목록. DB엔 콤마문자열로 저장돼 있는 걸 배열로 쪼갠 값 */
    private List<String> items;
}