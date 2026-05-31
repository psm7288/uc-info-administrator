package uc.dev.uc_info.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 관리자(부서 계정). 자체 로그인 계정이며 회원가입 없이 운영자가 직접 발급.
 * 담당 학생은 FK 가 아니라 부서코드(department_code == user.dept_id)로 묶는다.
 */
@Entity
@Table(name = "admin")
@Getter
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    /** 로그인 ID */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /** BCrypt 해시. 평문 저장 금지 */
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "admin_name", nullable = false)
    private String adminName;

    /** 담당 부서코드. user.dept_id 와 매칭하여 담당 학생 조회 */
    @Column(name = "department_code")
    private Integer departmentCode;

    @Column(name = "department_name")
    private String departmentName;

    /** 열람 추적 알림 사용 여부 */
    @Column(name = "alarm_tracking")
    private Boolean alarmTracking;

    /** 알림 발송 사용 여부 */
    @Column(name = "alarm_send")
    private Boolean alarmSend;

    /** 공지/배너 관련 설정값 */
    @Column(name = "notice_banner", columnDefinition = "TEXT")
    private String noticeBanner;

    /** 학과별 전용 계정 식별자 */
    @Column(name = "dept_exclusive_account", length = 50)
    private String deptExclusiveAccount;

    /** 부서 정보(JSONB). 핵심 로직 위임 예정 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "department_info", columnDefinition = "TEXT")
    private String departmentInfo;
}