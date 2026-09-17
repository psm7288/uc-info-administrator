package uc.dev.uc_info.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uc.dev.uc_info.model.Meal;
import uc.dev.uc_info.rest.dto.MealResponse;
import uc.dev.uc_info.rest.dto.MealSlotResponse;
import uc.dev.uc_info.service.MealService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 식단 학생 앱 REST 컨트롤러.
 */
@RestController
@RequestMapping("/api/meal")
@RequiredArgsConstructor
public class MealRestController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN);

    private final MealService mealService;

    /**
     * 오늘 날짜의 식단(조식/중식/석식)을 조회한다. 데이터 없는 끼니는
     * null로 나간다.
     *
     * @return 200 + 오늘 식단
     */
    @GetMapping("/today")
    public ResponseEntity<MealResponse> today() {
        LocalDate today = LocalDate.now();
        List<Meal> meals = mealService.getMealsByDate(today);

        MealResponse response = new MealResponse();
        response.setDate(today.format(DATE_FORMAT));
        response.setBreakfast(toSlot(meals, "BREAKFAST"));
        response.setLunch(toSlot(meals, "LUNCH"));
        response.setDinner(toSlot(meals, "DINNER"));

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 끼니 데이터를 목록에서 찾아 응답 슬롯으로 변환한다.
     *
     * @param meals    오늘의 전체 식단 목록(최대 3건)
     * @param mealType 찾을 끼니(BREAKFAST/LUNCH/DINNER)
     * @return 변환된 슬롯, 해당 끼니 데이터가 없으면 null
     */
    private MealSlotResponse toSlot(List<Meal> meals, String mealType) {
        return meals.stream()
                .filter(m -> mealType.equals(m.getMealType()))
                .findFirst()
                .map(m -> {
                    MealSlotResponse slot = new MealSlotResponse();
                    slot.setTime(m.getTimeRange());
                    slot.setItems(
                            Arrays.stream(m.getItems().split(","))
                                    .map(String::trim)
                                    .toList()
                    );
                    return slot;
                })
                .orElse(null);
    }
}