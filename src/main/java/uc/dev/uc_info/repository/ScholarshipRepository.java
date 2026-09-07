package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.Scholarship;

import java.util.List;

/**
 * 장학금(Scholarship) 영속성 접근 인터페이스.
 *
 * <p>SUPER_ADMIN은 전체 장학금을 조회하고,
 * DEPT_ADMIN은 본인 학과 또는 전체 대상 장학금만 조회한다.</p>
 */
public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

    /**
     * 전체 장학금을 최신 등록순으로 조회한다.
     *
     * @return 전체 장학금 목록
     */
    List<Scholarship> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 학과 또는 전체 대상 장학금을 최신순으로 조회한다.
     *
     * @param deptId 관리자 소속 학과 PK
     * @return 권한 범위 내 장학금 목록
     */
    @Query("""
        SELECT s
        FROM Scholarship s
        WHERE s.department IS NULL
           OR s.department.deptId = :deptId
        ORDER BY s.createdAt DESC
    """)
    List<Scholarship> findByDepartmentOrAll(@Param("deptId") Long deptId);

    /**
     * 특정 학과 또는 전체 대상 장학금 개수를 조회한다.
     *
     * @param deptId 관리자 소속 학과 PK
     * @return 권한 범위 내 장학금 개수
     */
    @Query("""
        SELECT COUNT(s)
        FROM Scholarship s
        WHERE s.department IS NULL
           OR s.department.deptId = :deptId
    """)
    long countByDepartmentOrAll(@Param("deptId") Long deptId);
}