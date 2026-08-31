package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Banner;

/**
 * 배너(Banner) 영속성 접근 인터페이스.
 *
 * <p>{@code JpaRepository<Banner, Long>}을 상속하면 기본 CRUD는 자동
 * 제공된다. 아래 3개를 인터페이스 안에 직접 선언해야 한다. 배너는 자체
 * department 필드가 없다 — 권한 범위는 연결된 {@code notice.department}를
 * 통해서만 정해진다(notice가 null이면 SUPER_ADMIN 전용 홍보 배너로
 * 취급한다).</p>
 *
 * <ul>
 *   <li>{@code List<Banner> findAllByOrderByCreatedAtDesc()}
 *       — 전체 배너 최신순. SUPER_ADMIN 목록 조회용. 이름 규칙만으로 자동
 *       구현되지만, {@code Banner.admin}이 지연로딩(LAZY)이라 목록 화면에서
 *       작성자 정보를 쓸 경우 N+1이 난다 — 그러면 이름 규칙 대신
 *       {@code @Query}로 바꿔서 {@code JOIN FETCH b.admin}을 추가해야 한다
 *       (Notice에서 이걸 빠뜨려서 실제로 N+1 버그가 났던 전례가 있으니
 *       목록 화면 설계 시 반드시 먼저 확인할 것).</li>
 *   <li>{@code long countByStatus(String status)}
 *       — 특정 상태(ACTIVE/SCHEDULED/INACTIVE) 배너 개수. 이름 규칙으로
 *       자동 구현된다. 화면 상단 activeCount/scheduledCount 둘 다 이
 *       메서드 하나로 처리한다(status 값만 다르게 호출).</li>
 *   <li>{@code List<Banner> findByDepartmentOrAll(Long deptId)}
 *       — DEPT_ADMIN용 목록 조회. {@code @Query}(JPQL)가 필요하다. Banner
 *       → notice → department로 두 단계를 거쳐야 한다: "연결된 공지의
 *       department가 null(전체 대상)이거나 파라미터 deptId와 같은 배너"만
 *       조회한다. 연결된 공지 자체가 없는 배너(notice가 null)는 이 목록에서
 *       자동으로 제외되는 게 맞는 동작이다(DEPT_ADMIN은 애초에 공지 없는
 *       배너를 볼 권한이 없다). 여기도 {@code JOIN FETCH b.admin}이
 *       필요하다.</li>
 * </ul>
 */
public interface BannerRepository extends JpaRepository<Banner, Long> {
}