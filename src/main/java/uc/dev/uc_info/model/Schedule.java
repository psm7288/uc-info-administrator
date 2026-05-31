package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학사 일정. 학생 앱 일정 화면에 노출.
 */
@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    /** 등록 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "title", nullable = false)
    private String title;

    /**
     * 카테고리: ACADEMIC(학사), EXAM(시험), REGISTRATION(수강신청),
     *           VACATION(방학), EVENT(행사), ETC(기타)
     */
    @Column(name = "category", nullable = false, length = 20)
    private String category;

    /** 대상 학과 코드. null 이면 전체 */
    @Column(name = "target_dept_id")
    private Integer targetDeptId;

    /** 대상 학년. null 이면 전체 */
    @Column(name = "target_grade", length = 10)
    private String targetGrade;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** 노출 여부 */
    @Column(name = "visible", nullable = false)
    private Boolean visible = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}