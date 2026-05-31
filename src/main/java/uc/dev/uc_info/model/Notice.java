package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    /** 작성 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 카테고리: DEPARTMENT(학과), ACADEMIC(학사), SCHOLARSHIP(장학),
     *           EVENT(행사), EMPLOYMENT(취업)
     */
    @Column(name = "category", nullable = false, length = 20)
    private String category;

    /**
     * 중요도: NORMAL, IMPORTANT, URGENT
     */
    @Column(name = "priority", nullable = false, length = 20)
    private String priority;

    /**
     * 대상 학과 코드. null 이면 전체 학과.
     * user.dept_id 와 매칭하여 대상 학생 필터링.
     */
    @Column(name = "target_dept_id")
    private Integer targetDeptId;

    /** 대상 학년. null 이면 전체 학년 */
    @Column(name = "target_grade", length = 10)
    private String targetGrade;

    /**
     * 게시 상태: DRAFT(임시저장), WAITING(검토중),
     *           PUBLISHED(게시중), CLOSED(게시종료)
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** 상단 고정 여부 */
    @Column(name = "pinned", nullable = false)
    private Boolean pinned = false;

    /** 푸시 발송 여부 */
    @Column(name = "push_sent", nullable = false)
    private Boolean pushSent = false;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}