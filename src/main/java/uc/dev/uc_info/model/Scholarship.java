package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uc.dev.uc_info.model.base.BaseTimeEntity;

import java.time.LocalDate;

/**
 * 장학금(Scholarship) 엔티티. (모델만 — Repository/Service/Controller·화면연동 보류)
 *
 * <p>교내·외 장학금 정보를 통합 관리하기 위한 엔티티다. 현재는 엔티티(스키마)만
 * 정의하고, 조회/등록 등 애플리케이션 로직과 화면 연동은 후속 작업으로 보류한다.</p>
 *
 * <p>대상 학과({@link #department})가 null 이면 전체 대상, {@link #targetGrade}
 * 가 null 이면 전체 학년 대상이다. {@link #deadline} 은 화면에서 D-Day 배지
 * 계산에 사용한다. 생성 시각은 {@link BaseTimeEntity} 에서 상속.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Admin} : 등록 관리자 (N:1, 소유 측, NOT NULL)</li>
 *   <li>{@link Department} : 대상 학과 (N:1, 소유 측, nullable=전체)</li>
 *   <li>{@link Notice} : 연결 공지 (N:1, 소유 측, optional — '공지 연결' 기능)</li>
 * </ul>
 */
@Entity
@Table(name = "scholarship")
@Getter
@NoArgsConstructor
public class Scholarship extends BaseTimeEntity {

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

    /** 연결 공지. 없으면 null */
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