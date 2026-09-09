package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Meal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 식단(Meal) 영속성 접근 인터페이스. 기본 CRUD는 JpaRepository가 제공한다.
 */
public interface MealRepository extends JpaRepository<Meal, Long> {

    /**
     * 특정 날짜의 식단(조식/중식/석식, 최대 3건)을 조회한다.
     *
     * @param mealDate 조회할 날짜
     * @return 해당 날짜의 식단 목록(0~3건)
     */
    List<Meal> findByMealDate(LocalDate mealDate);

    /**
     * 특정 날짜·끼니의 식단을 조회한다. CSV 업로드 시 이미 등록된 조합인지
     * 확인해서 덮어쓸지(update) 새로 만들지(insert) 판단하는 데 쓰인다.
     *
     * @param mealDate 조회할 날짜
     * @param mealType 조회할 끼니(BREAKFAST/LUNCH/DINNER)
     * @return 조회된 식단, 없으면 빈 Optional
     */
    Optional<Meal> findByMealDateAndMealType(LocalDate mealDate, String mealType);

    /**
     * 전체 식단을 최신 날짜순으로 조회한다. 관리자 화면의 "최근 업로드
     * 목록"용이다.
     *
     * @return 날짜 내림차순 전체 식단 목록
     */
    List<Meal> findAllByOrderByMealDateDesc();
}