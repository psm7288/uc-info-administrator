package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uc.dev.uc_info.model.base.BaseTimeEntity;

import java.time.LocalDate;

/**
 * 배너(Banner) 엔티티.
 *
 * <p>학생 앱 메인 화면에 노출되는 카드형 배너다. 공지(Notice)와 선택적으로
 * 연결할 수 있어, 배너 클릭 시 연결된 공지로 이동시키는 용도로 쓸 수 있다.
 * 생성 시각은 {@link BaseTimeEntity} 에서 상속.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Admin} : 등록 관리자 (N:1, 소유 측, NOT NULL)</li>
 *   <li>{@link Notice} : 연결 공지 (N:1, 소유 측, optional — 없으면 null)</li>
 * </ul>
 */
@Entity
@Table(name = "banner")
@Getter
@Setter
@NoArgsConstructor
public class Banner extends BaseTimeEntity {

    /** PK. 배너 식별자 */
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

    /** 배너 제목 */
    @Column(name = "title", nullable = false)
    private String title;

    /** 배너 부제 / 날짜 텍스트 등 */
    @Column(name = "subtitle")
    private String subtitle;

    /** 노출 상태: ACTIVE/SCHEDULED/INACTIVE */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** 노출 시작일 */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** 노출 종료일 */
    @Column(name = "end_date")
    private LocalDate endDate;
}