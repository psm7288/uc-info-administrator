package uc.dev.uc_info.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uc.dev.uc_info.model.base.BaseUpdatableEntity;

/**
 * 셔틀버스 노선(Shuttle) 엔티티.
 *
 * <p>학생 앱 편의 정보 화면에 노출되는 셔틀버스 노선이다. 학과 무관한 학교
 * 공통 정보이므로 SUPER_ADMIN(전체 관리자)만 등록·수정·삭제한다.
 * 생성/수정 시각은 {@link BaseUpdatableEntity} 에서 상속.</p>
 *
 * <h3>연관관계</h3>
 * <ul>
 *   <li>{@link Admin} : 등록 관리자(SUPER_ADMIN) (N:1, 소유 측, NOT NULL)</li>
 * </ul>
 */
@Entity
@Table(name = "shuttle")
@Getter
@NoArgsConstructor
public class Shuttle extends BaseUpdatableEntity {

    /** PK. 셔틀 노선 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shuttle_id")
    private Long shuttleId;

    /** 등록 관리자(SUPER_ADMIN) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    /** 노선명 */
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

    /** 운행 상태: ACTIVE/SUSPENDED/DELAYED */
    @Column(name = "status", nullable = false, length = 20)
    private String status;
}