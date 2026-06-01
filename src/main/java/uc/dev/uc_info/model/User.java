package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "dept_id", nullable = false)
    private Integer deptId;

    @Column(name = "dept_name", nullable = false)
    private String deptName;

    @Column(name = "fcm_token")
    private String fcmToken;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scholarship_info")
    private String scholarshipInfo;

    @Column(name = "tel", nullable = false)
    private String tel;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "academic_status", nullable = false)
    private String academicStatus;

    @Column(name = "gender", nullable = false, columnDefinition = "char(1)")
    private String gender;

    @Column(name = "advisor_name")
    private String advisorName;

    @Column(name = "score", nullable = false, precision = 3, scale = 2)
    private BigDecimal score;

    @Column(name = "access", nullable = false)
    private Boolean access;
}