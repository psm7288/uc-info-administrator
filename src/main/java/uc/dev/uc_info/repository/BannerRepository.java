package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.Banner;

import java.util.List;

/**
 * 배너(Banner) 영속성 접근 인터페이스.
 *
 * <p>기본 CRUD는 JpaRepository가 제공하며, 관리자 목록 조회와 상태별
 * 통계에 필요한 조회 메서드를 추가한다. Banner 자체에는 department가
 * 없으므로 DEPT_ADMIN 권한 범위는 연결된 Notice의 department를 기준으로
 * 판단한다.</p>
 */
public interface BannerRepository extends JpaRepository<Banner, Long> {

    /**
     * 전체 배너를 최신 등록순으로 조회한다. SUPER_ADMIN 목록 조회용.
     *
     * <p>목록 화면이 연결된 공지의 학과명(linkedDeptName)을 평탄화해서
     * 보여줘야 하므로, admin뿐 아니라 notice/notice.department까지
     * LEFT JOIN FETCH한다(공지가 없는 배너도 포함해야 하므로 INNER 아닌
     * LEFT — {@link #findByDepartmentOrAll}의 INNER JOIN과는 의도가 다름).</p>
     *
     * @return 전체 배너 목록
     */
    @Query("""
        SELECT b
        FROM Banner b
        JOIN FETCH b.admin
        LEFT JOIN FETCH b.notice n
        LEFT JOIN FETCH n.department
        ORDER BY b.createdAt DESC
    """)
    List<Banner> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 상태의 배너 개수를 조회한다. 학과 필터 없이 시스템 전체
     * 기준이다 — SUPER_ADMIN 통계용. DEPT_ADMIN용은
     * {@link #countByStatusAndDepartmentOrAll}을 쓴다.
     *
     * @param status ACTIVE / SCHEDULED / INACTIVE
     * @return 해당 상태 배너 개수
     */
    long countByStatus(String status);

    /**
     * 학과 관리자에게 허용된 배너 목록을 조회한다.
     *
     * <p>연결된 공지의 대상 학과가 전체(null)이거나
     * 관리자의 소속 학과와 같은 배너만 반환한다.</p>
     *
     * <p>공지와 연결되지 않은 배너는 SUPER_ADMIN 전용이므로
     * DEPT_ADMIN 목록에서는 제외한다.</p>
     *
     * @param deptId 관리자 소속 학과 PK
     * @return 권한 범위에 맞는 배너 목록
     */
    @Query("""
        SELECT b
        FROM Banner b
        JOIN FETCH b.admin
        JOIN FETCH b.notice n
        LEFT JOIN FETCH n.department d
        WHERE d IS NULL
           OR d.deptId = :deptId
        ORDER BY b.createdAt DESC
    """)
    List<Banner> findByDepartmentOrAll(@Param("deptId") Long deptId);

    /**
     * DEPT_ADMIN 권한 범위(연결 공지가 본인 학과+전체 대상, 공지 없는
     * 배너는 제외) 배너 개수. {@link #findByDepartmentOrAll}과 동일한
     * 범위로 세야 화면 숫자와 목록이 일치한다.
     *
     * @param deptId 관리자 소속 학과 PK
     * @return 해당 범위의 배너 개수
     */
    @Query("""
        SELECT COUNT(b)
        FROM Banner b
        JOIN b.notice n
        WHERE n.department IS NULL
           OR n.department.deptId = :deptId
    """)
    long countByDepartmentOrAll(@Param("deptId") Long deptId);

    /**
     * DEPT_ADMIN 권한 범위이면서 특정 상태인 배너 개수.
     * {@link #countByStatus}의 DEPT_ADMIN용 버전이다.
     *
     * @param status 카운트할 배너 상태
     * @param deptId 관리자 소속 학과 PK
     * @return 해당 범위·상태의 배너 개수
     */
    @Query("""
        SELECT COUNT(b)
        FROM Banner b
        JOIN b.notice n
        WHERE b.status = :status
          AND (n.department IS NULL OR n.department.deptId = :deptId)
    """)
    long countByStatusAndDepartmentOrAll(@Param("status") String status, @Param("deptId") Long deptId);
}