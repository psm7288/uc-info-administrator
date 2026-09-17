package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uc.dev.uc_info.common.validation.DepartmentScoped;
import uc.dev.uc_info.model.base.BaseTimeEntity;

import java.time.LocalDate;

/**
 * 장학금(Scholarship) 엔티티.
 *
 * <p>교내·외 장학금 정보를 통합 관리한다. 대상 학과({@link #department})가
 * null 이면 전체 대상, {@link #targetGrade} 가 null 이면 전체 학년 대상이다.
 * {@link #deadline} 은 화면에서 D-Day 배지 계산에 사용한다. 생성 시각은
 * {@link BaseTimeEntity} 에서 상속. Notice/Schedule과 동일하게
 * {@link DepartmentScoped} 를 구현해 {@code AdminScopeValidator} 를 그대로
 * 재사용한다.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Admin} : 등록 관리자 (N:1, 소유 측, NOT NULL)</li>
 *   <li>{@link Department} : 대상 학과 (N:1, 소유 측, nullable=전체)</li>
 *   <li>{@link Notice} : 연결 공지 (N:1, 소유 측, optional — 참고용 링크,
 *       권한 판단 기준은 아니다. Banner와 달리 Scholarship 자체에 department가
 *       있어서 권한은 이 필드가 아니라 {@link #department} 로 정해진다.)</li>
 * </ul>
 */
@Entity
@Table(name = "scholarship")
@Getter
@Setter
@NoArgsConstructor
public class Scholarship extends BaseTimeEntity implements DepartmentScoped {

    /** PK. 장학금 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scholarship_id")
    private Long scholarshipId;

    /** 등록 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    /** 대상 학과. null 이면 전체 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    /** 연결 공지. 없으면 null(참고용, 권한 판단 기준 아님) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    /** 장학금명 */
    @Column(name = "name", nullable = false)
    private String name;

    /** 유형: REGIONAL(지역)/GRADE(성적)/INTERNAL(교내)/EXTERNAL(외부) */
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    /** 대상 학년. null 이면 전체 */
    @Column(name = "target_grade", length = 10)
    private String targetGrade;

    /** 거주 조건 */
    @Column(name = "residence_condition")
    private String residenceCondition;

    /** 마감일. D-Day 계산용 */
    @Column(name = "deadline")
    private LocalDate deadline;

    /** 노출 여부 */
    @Column(name = "visible", nullable = false)
    private Boolean visible = true;
}