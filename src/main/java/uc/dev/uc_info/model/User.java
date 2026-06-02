package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * 학생(User) 엔티티.
 *
 * <p>모바일 학생 앱 사용자다. 이 웹 관리자 대시보드에 직접 로그인하지 않으며,
 * 관리자가 공지/일정 대상자를 산정하거나 열람률을 집계할 때 참조된다.
 * 일부 필드(fcm_token, score, scholarship_info 등)는 관리자 화면이 아닌
 * 학생 앱 연동을 위해 보존한다.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Department} : 소속 학과 (N:1, 소유 측, NOT NULL)</li>
 *   <li>{@link NoticeReadLog} : 공지 열람 기록 (1:N, 비소유 측)</li>
 * </ul>
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    /** PK. 학생 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** 소속 학과 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    /** 학생 이름 */
    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    /** 학년(1~). 공지/일정 target_grade 매칭용 */
    @Column(name = "grade")
    private Integer grade;

    /** FCM 푸시 토큰. 푸시 발송 대상 식별(학생 앱 연동) */
    @Column(name = "fcm_token")
    private String fcmToken;

    /** 장학 통합 정보(JSON). 학생 앱 연동 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scholarship_info", columnDefinition = "TEXT")
    private String scholarshipInfo;

    /** 연락처 */
    @Column(name = "tel", nullable = false, length = 20)
    private String tel;

    /** 이메일 */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /** 학적 상태(재학/휴학/졸업) */
    @Column(name = "academic_status", nullable = false, length = 20)
    private String academicStatus;

    /** 성별. char(1) */
    @Column(name = "gender", nullable = false, columnDefinition = "char(1)")
    private String gender;

    /** 지도교수명 */
    @Column(name = "advisor_name", length = 30)
    private String advisorName;

    /** 평점. 정밀도(3,2) — 예: 4.50 */
    @Column(name = "score", nullable = false, precision = 3, scale = 2)
    private BigDecimal score;

    /** 앱 접근 허용 여부 (DB 컬럼 Access → 소문자 매핑) */
    @Column(name = "access", nullable = false)
    private Boolean access;
}