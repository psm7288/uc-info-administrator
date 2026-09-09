package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uc.dev.uc_info.model.base.BaseTimeEntity;

import java.time.LocalDate;

/**
 * 학생 식당 식단(Meal) 엔티티.
 *
 * <p>하루 한 끼(조식/중식/석식)당 한 행이다 SUPER_ADMIN이
 * CSV 파일을 업로드하는 방식으로만 등록·수정된다.</p>
 *
 * <p>{@code items}는 메뉴 항목을 콤마로 구분한 문자열이다(예:
 * "쌀밥,김치찌개,계란후라이"). 학생 앱 REST 응답에서는 이 문자열을
 * 배열로 쪼개서 내보낸다.</p>
 *
 * <p>(meal_date, meal_type) 조합에 유니크 제약을 걸어, 같은 날 같은
 * 끼니가 중복 등록되지 않게 한다 — CSV 재업로드 시 이 조합이 이미
 * 있으면 덮어쓰기(update)로 처리한다.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Admin} : 업로드한 관리자 (N:1, 소유 측, NOT NULL)</li>
 * </ul>
 */
@Entity
@Table(name = "meal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"meal_date", "meal_type"}))
@Getter
@Setter
@NoArgsConstructor
public class Meal extends BaseTimeEntity {

    /** PK. 식단 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Long mealId;

    /** 업로드한 관리자(항상 SUPER_ADMIN) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    /** 날짜 */
    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    /** 끼니 구분: BREAKFAST / LUNCH / DINNER */
    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    /** 배식 시간(예: "08:00-09:30") */
    @Column(name = "time_range", nullable = false, length = 20)
    private String timeRange;

    /** 메뉴 항목. 콤마로 구분된 문자열(예: "쌀밥,김치찌개,계란후라이") */
    @Column(name = "items", nullable = false, columnDefinition = "TEXT")
    private String items;
}