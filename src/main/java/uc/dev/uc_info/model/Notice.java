package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uc.dev.uc_info.model.base.BaseUpdatableEntity;

import java.time.LocalDate;

/**
 * 공지(Notice) 엔티티.
 *
 * <p>관리자가 작성하여 학생 앱에 노출되는 공식 공지다. 상태(status)에 따라
 * 임시저장 → 검토중 → 게시중 → 게시종료의 흐름을 가진다.</p>
 *
 * <p>대상 학과({@link #department})가 null 이면 전체 학과 대상이고,
 * {@link #targetGrade} 가 null 이면 전체 학년 대상이다.
 * 생성/수정 시각은 {@link BaseUpdatableEntity} 에서 상속(JPA Auditing 자동).</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Admin} : 작성 관리자 (N:1, 소유 측, NOT NULL)</li>
 *   <li>{@link Department} : 대상 학과 (N:1, 소유 측, nullable=전체)</li>
 *   <li>{@link NoticeReadLog} : 열람 기록 (1:N, 비소유 측)</li>
 *   <li>{@link Banner} : 연결 배너 (1:N, 비소유 측, optional)</li>
 * </ul>
 */
@Entity
@Table(name = "notice")
@Getter
@Setter
@NoArgsConstructor
public class Notice extends BaseUpdatableEntity {

    /** PK. 공지 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    /** 작성 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    /** 대상 학과. null 이면 전체 학과 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    /** 공지 제목 */
    @Column(name = "title", nullable = false)
    private String title;

    /** 공지 본문(HTML) */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** 카테고리: DEPARTMENT/ACADEMIC/SCHOLARSHIP/EVENT/EMPLOYMENT */
    @Column(name = "category", nullable = false, length = 20)
    private String category;

    /** 중요도: NORMAL/IMPORTANT/URGENT */
    @Column(name = "priority", nullable = false, length = 20)
    private String priority;

    /** 대상 학년. null 이면 전체 학년 */
    @Column(name = "target_grade", length = 10)
    private String targetGrade;

    /** 게시 상태: DRAFT/PUBLISHED/CLOSED */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** 게시 시작일 */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** 게시 종료일 */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** 상단 고정 여부 */
    @Column(name = "pinned", nullable = false)
    private Boolean pinned = false;

    /** 푸시 발송 여부 */
    @Column(name = "push_sent", nullable = false)
    private Boolean pushSent = false;

    /** 첨부파일 URL */
    @Column(name = "attachment_url")
    private String attachmentUrl;
}