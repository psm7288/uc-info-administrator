package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uc.dev.uc_info.model.base.BaseTimeEntity;

/**
 * 학과(Department) 엔티티.
 *
 * <p>학과 정보를 단일하게 보관하는 테이블이다. Admin 과 User 는 서로
 * 직접 FK 로 연결되지 않고, 둘 다 이 Department 를 참조하여 "같은 학과인지"를
 * 판단한다(admin.department == user.department).</p>
 *
 * <p>{@link #deptCode} 는 화면 select 의 value 등으로 쓰이는 비즈니스 키이며,
 * PK({@link #deptId})와 분리하여 학과 코드 체계가 바뀌어도 PK 가 안정적이도록 했다.
 * 생성 시각은 {@link BaseTimeEntity} 에서 상속(JPA Auditing 자동 주입).</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Admin} : 소속 관리자 (1:N, 비소유 측)</li>
 *   <li>{@link User} : 소속 학생 (1:N, 비소유 측)</li>
 *   <li>{@link Notice} : 대상 학과로 지정된 공지 (1:N, 비소유 측, optional)</li>
 *   <li>{@link Schedule} : 대상 학과로 지정된 일정 (1:N, 비소유 측, optional)</li>
 *   <li>{@link Scholarship} : 대상 학과로 지정된 장학금 (1:N, 비소유 측, optional)</li>
 * </ul>
 */
@Entity
@Table(name = "department")
@Getter
@NoArgsConstructor
public class Department extends BaseTimeEntity {

    /** PK. 학과 내부 식별자(대리키) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long deptId;

    /** 학과 코드. 비즈니스 키(화면 select value 등). 유니크 */
    @Column(name = "dept_code", nullable = false, unique = true)
    private Integer deptCode;

    /** 학과명 */
    @Column(name = "dept_name", nullable = false, length = 100)
    private String deptName;
}