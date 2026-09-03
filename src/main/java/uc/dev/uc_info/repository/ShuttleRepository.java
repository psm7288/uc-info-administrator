package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Shuttle;

import java.util.List;

/**
 * 셔틀버스 노선(Shuttle) 영속성 접근 인터페이스. 기본 CRUD는 JpaRepository가
 * 제공한다.
 */
public interface ShuttleRepository extends JpaRepository<Shuttle, Long> {

    /**
     * 전체 노선을 등록일시(createdAt) 기준 내림차순(최신순)으로 조회한다.
     * SUPER_ADMIN 전용 목록이라 권한 분기 없이 이 메서드 하나만 쓰인다.
     *
     * @return 최신순으로 정렬된 전체 Shuttle 목록
     */
    List<Shuttle> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 상태(status)의 노선 개수를 조회한다. 화면 상단 "운영 노선 수"
     * 통계에서 {@code countByStatus("ACTIVE")} 형태로 호출된다.
     *
     * @param status 카운트할 노선 상태(ACTIVE/SUSPENDED/DELAYED)
     * @return 해당 상태의 Shuttle 개수
     */
    long countByStatus(String status);
}