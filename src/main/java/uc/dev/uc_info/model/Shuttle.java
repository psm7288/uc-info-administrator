package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 셔틀버스 노선. 학생 앱 편의 정보에 노출.
 */
@Entity
@Table(name = "shuttle")
@Getter
@NoArgsConstructor
public class Shuttle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shuttle_id")
    private Long shuttleId;

    /** 등록 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    /** 출발지 */
    @Column(name = "departure", nullable = false)
    private String departure;

    /** 도착지 */
    @Column(name = "destination", nullable = false)
    private String destination;

    /** 경유지 설명 */
    @Column(name = "waypoints")
    private String waypoints;

    /** 첫차 시간. 예) "08:00" */
    @Column(name = "first_departure", length = 10)
    private String firstDeparture;

    /** 막차 시간. 예) "18:30" */
    @Column(name = "last_departure", length = 10)
    private String lastDeparture;

    /**
     * 운행 상태: ACTIVE(정상운행), SUSPENDED(운행중단), DELAYED(지연)
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

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