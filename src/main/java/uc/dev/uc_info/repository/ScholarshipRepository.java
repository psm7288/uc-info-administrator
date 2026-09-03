package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Scholarship;

/**
 * 장학금(Scholarship) 영속성 접근 인터페이스. Notice/Schedule과 완전히
 * 같은 패턴이다(자체 department 필드로 직접 필터링).
 *
 * <h3>필요한 조회 메서드</h3>
 * <ul>
 *   <li>{@code List<Scholarship> findAllByOrderByCreatedAtDesc()}
 *       — 전체 장학금 최신순. SUPER_ADMIN 목록용. 이름 규칙으로 자동
 *       구현되며, admin은 목록에서 안 쓰이니 JOIN FETCH 불필요.</li>
 *   <li>{@code List<Scholarship> findByDepartmentOrAll(Long deptId)}
 *       — DEPT_ADMIN용. department가 null이거나 deptId와 같은 것만.
 *       Schedule의 동일 메서드와 정확히 같은 JPQL 패턴
 *       ({@code WHERE s.department IS NULL OR s.department.deptId = :deptId})
 *       — {@code @Query} 필요.</li>
 *   <li>{@code long countByDepartmentOrAll(Long deptId)}
 *       — 위와 같은 범위의 개수. {@code countAll}과 짝(목록-통계 숫자 일치용).
 *       {@code @Query} 필요.</li>
 * </ul>
 */
public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {
}