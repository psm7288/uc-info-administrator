package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Shuttle;

/**
 * 셔틀버스 노선(Shuttle) 영속성 접근 인터페이스.
 *
 * <p>{@code JpaRepository<Shuttle, Long>}을 상속하면 save/findById/findAll/
 * delete/count 등 기본 CRUD는 자동 제공된다. 아래 2개 메서드를 인터페이스
 * 안에 직접 선언하면 된다. 둘 다 Spring Data JPA의 메서드 이름 규칙만으로
 * 자동 구현되므로 {@code @Query}는 필요 없다.</p>
 *
 * <ul>
 *   <li>{@code List<Shuttle> findAllByOrderByCreatedAtDesc()}
 *       — 전체 노선을 등록일시(createdAt) 내림차순(최신순)으로 조회한다.
 *       노선 관리 화면(SUPER_ADMIN 전용, 학과 분기 없음) 목록 조회용이다.</li>
 *   <li>{@code long countByStatus(String status)}
 *       — 특정 상태(status)의 노선 개수를 센다. 화면 상단 "운영 노선 수"
 *       통계에서 {@code countByStatus("ACTIVE")} 형태로 호출된다.</li>
 * </ul>
 *
 * <p>Notice/Schedule의 Repository와 달리 department 관련 OR/NULL 조건이
 * 전혀 없다 — 셔틀은 학과 개념이 없는 도메인이라 {@code @Query}가 필요한
 * 메서드가 하나도 없다.</p>
 */
public interface ShuttleRepository extends JpaRepository<Shuttle, Long> {
}