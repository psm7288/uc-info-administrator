package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공지 열람 기록. 학생(User)이 공지(Notice)를 읽었을 때 생성.
 * tracking 페이지의 열람률 / 미확인자 산출에 사용.
 */
@Entity
@Table(name = "notice_read_log",
        uniqueConstraints = @UniqueConstraint(columnNames = {"notice_id", "user_id"}))
@Getter
@NoArgsConstructor
public class NoticeReadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    @PrePersist
    private void prePersist() {
        this.readAt = LocalDateTime.now();
    }
}

