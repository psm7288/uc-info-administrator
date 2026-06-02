package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공지 열람 기록(NoticeReadLog) 엔티티.
 *
 * <p>학생(User)이 공지(Notice)를 열람했을 때 한 건 생성된다. tracking 화면의
 * 열람률 및 미확인자 산출의 기준 데이터다.</p>
 *
 * <p>(notice_id, user_id) 복합 유니크 제약으로 동일 학생이 같은 공지에 대해
 * 중복 기록되지 않는다.</p>
 *
 * <p>{@link #readAt} 은 "열람 시각"이며 레코드 생성 시점과 동일하므로
 * JPA Auditing 의 {@code @CreatedDate} 로 자동 주입한다(컬럼명만 read_at).
 * 의미가 일반적 createdAt 과 달라 BaseTimeEntity 를 상속하지 않고 자체 보유한다.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Notice} : 열람된 공지 (N:1, 소유 측, NOT NULL)</li>
 *   <li>{@link User} : 열람한 학생 (N:1, 소유 측, NOT NULL)</li>
 * </ul>
 */
@Entity
@Table(name = "notice_read_log",
        uniqueConstraints = @UniqueConstraint(columnNames = {"notice_id", "user_id"}))
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NoticeReadLog {

    /** PK. 열람 기록 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    /** 열람된 공지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    /** 열람한 학생 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 열람 시각(=생성 시각). @CreatedDate 자동 주입 */
    @CreatedDate
    @Column(name = "read_at", updatable = false)
    private LocalDateTime readAt;
}