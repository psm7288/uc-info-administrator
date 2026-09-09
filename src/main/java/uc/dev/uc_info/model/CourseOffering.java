package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uc.dev.uc_info.common.validation.DepartmentScoped;
import uc.dev.uc_info.model.base.BaseTimeEntity;

/**
 * 개설과목(CourseOffering) 엔티티.
 *
 * <p>학과가 이번 학기에 개설한 과목 정보다. 학생 개인 데이터가 아니라
 * "학과 단위로 존재하는 강의 목록"이며, 학생은 이 중에서 자신이 들을
 * 과목을 선택해({@link Enrollment}) 자기 시간표를 구성한다.</p>
 *
 * <p>DEPT_ADMIN이 CSV 파일로 본인 학과 개설과목을 일괄 등록한다(CSV 한 줄
 * = 이 엔티티 한 행). SUPER_ADMIN은 전체 학과 걸 올릴 수 있다.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Admin} : 업로드한 관리자 (N:1, 소유 측, NOT NULL)</li>
 *   <li>{@link Department} : 개설 학과 (N:1, 소유 측, NOT NULL)</li>
 * </ul>
 */
@Entity
@Table(name = "course_offering")
@Getter
@Setter
@NoArgsConstructor
public class CourseOffering extends BaseTimeEntity implements DepartmentScoped {

    /** PK. 개설과목 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_offering_id")
    private Long courseOfferingId;

    /** 업로드한 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    /** 개설 학과 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    /** 과목명 */
    @Column(name = "subject", nullable = false)
    private String subject;

    /** 대상 학년. null이면 전체 학년 대상 */
    @Column(name = "target_grade", length = 10)
    private String targetGrade;

    /** 요일: 월/화/수/목/금 */
    @Column(name = "day", nullable = false, length = 10)
    private String day;

    /** 시작 교시(시각) */
    @Column(name = "start_hour", nullable = false)
    private Integer startHour;

    /** 종료 교시(시각) */
    @Column(name = "end_hour", nullable = false)
    private Integer endHour;

    /** 강의실 */
    @Column(name = "room")
    private String room;

    /** 담당 교수 */
    @Column(name = "professor")
    private String professor;
}