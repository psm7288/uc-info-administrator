package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uc.dev.uc_info.model.base.BaseTimeEntity;

import java.time.LocalDate;

/**
 * 학사 일정(Schedule) 엔티티.
 *
 * <p>관리자가 등록하여 학생 앱 일정 화면에 노출되는 학사 일정이다.
 * 대상 학과({@link #department})가 null 이면 전체 대상, {@link #targetGrade}
 * 가 null 이면 전체 학년 대상이다. {@link #visible} 로 노출 여부를 토글한다.
 * 생성 시각은 {@link BaseTimeEntity} 에서 상속.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Admin} : 등록 관리자 (N:1, 소유 측, NOT NULL)</li>
 *   <li>{@link Department} : 대상 학과 (N:1, 소유 측, nullable=전체)</li>
 * </ul>
 */
@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor
public class Schedule extends BaseTimeEntity {

    /** PK. 학사일정 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    /** 등록 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    /** 대상 학과. null 이면 전체 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    /** 일정 제목 */
    @Column(name = "title", nullable = false)
    private String title;

    /** 카테고리: ACADEMIC/EXAM/REGISTRATION/VACATION/EVENT/ETC */
    @Column(name = "category", nullable = false, length = 20)
    private String category;

    /** 대상 학년. null 이면 전체 */
    @Column(name = "target_grade", length = 10)
    private String targetGrade;

    /** 일정 시작일 */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** 일정 종료일 */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** 노출 여부 */
    @Column(name = "visible", nullable = false)
    private Boolean visible = true;
}