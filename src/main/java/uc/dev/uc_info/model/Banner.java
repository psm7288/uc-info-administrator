package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학생 앱 메인에 노출되는 배너.
 * 공지(Notice)와 선택적으로 연결 가능.
 */
@Entity
@Table(name = "banner")
@Getter
@NoArgsConstructor
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Long bannerId;

    /** 등록 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    /** 연결 공지. 없으면 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @Column(name = "title", nullable = false)
    private String title;

    /** 배너 부제 / 날짜 텍스트 등 */
    @Column(name = "subtitle")
    private String subtitle;

    /**
     * 노출 상태: ACTIVE(노출중), SCHEDULED(예약), INACTIVE(비활성)
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}