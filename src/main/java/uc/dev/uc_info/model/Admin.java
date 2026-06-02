package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자(부서/전체 계정) 엔티티.
 *
 * <p>웹 관리자 대시보드에 로그인하는 자체 계정이다. 회원가입 없이 운영자가
 * 직접 발급한다. 권한({@link #role})에 따라 범위가 다르다.</p>
 *
 * <ul>
 *   <li>DEPT_ADMIN(학과 관리자): 본인 학과({@link #department}) 범위만 작성·조회</li>
 *   <li>SUPER_ADMIN(전체 관리자): 전 학과 + 학과 무관 항목(셔틀 등) 관리.
 *       특정 학과 비소속이므로 department 는 null</li>
 * </ul>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Department} : 소속 학과 (N:1, 소유 측, nullable — SUPER_ADMIN은 null)</li>
 *   <li>{@link Notice}/{@link Banner}/{@link Schedule}/{@link Shuttle}/{@link Scholarship}
 *       : 작성/등록 항목 (1:N, 비소유 측)</li>
 * </ul>
 */
@Entity
@Table(name = "admin")
@Getter
@NoArgsConstructor
public class Admin {

    /** PK. 관리자 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    /** 소속 학과. SUPER_ADMIN 은 null(특정 학과 비소속) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    /** 권한 구분: DEPT_ADMIN / SUPER_ADMIN */
    @Column(name = "role", length = 20)
    private String role;

    /** 로그인 ID. 유니크 */
    @Column(name = "username", nullable = false, unique = true, length = 20)
    private String username;

    /** BCrypt 해시. 평문 저장 금지 */
    @Column(name = "password", nullable = false)
    private String password;

    /** 관리자 표시 이름 */
    @Column(name = "admin_name", nullable = false, length = 20)
    private String adminName;

    /** 열람 추적 알림 사용 여부 */
    @Column(name = "alarm_tracking")
    private Boolean alarmTracking;

    /** 알림 발송 사용 여부 */
    @Column(name = "alarm_send")
    private Boolean alarmSend;
}