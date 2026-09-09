package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uc.dev.uc_info.model.base.BaseTimeEntity;

/**
 * 수강선택(Enrollment) 엔티티.
 *
 * <p>학생이 개설과목({@link CourseOffering}) 중 하나를 자기 시간표에
 * 담은 기록이다. 학생 개인 시간표는 이 테이블에 담긴 항목들을 모아서
 * 구성된다(별도 Timetable 테이블 없음).</p>
 *
 * <p>(user_id, course_offering_id) 조합에 유니크 제약을 걸어 같은 과목을
 * 중복으로 담지 못하게 한다.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link User} : 담은 학생 (N:1, 소유 측, NOT NULL)</li>
 *   <li>{@link CourseOffering} : 담은 과목 (N:1, 소유 측, NOT NULL)</li>
 * </ul>
 */
@Entity
@Table(name = "enrollment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_offering_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Enrollment extends BaseTimeEntity {

    /** PK. 수강선택 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    /** 담은 학생 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 담은 개설과목 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;

    /** 시간표 표시용 색상 인덱스(0~5). 담을 때 서버가 순환 배정 */
    @Column(name = "color", nullable = false)
    private Integer color;
}