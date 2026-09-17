package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.CourseOffering;

import java.util.List;

/**
 * 개설과목(CourseOffering) 영속성 접근 인터페이스.
 */
public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {

    /**
     * 전체 개설과목을 최신 등록순으로 조회한다. SUPER_ADMIN 목록용.
     *
     * @return 전체 개설과목 목록(department fetch join 포함)
     */
    @Query("""
        SELECT c
        FROM CourseOffering c
        JOIN FETCH c.department
        ORDER BY c.createdAt DESC
    """)
    List<CourseOffering> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 학과의 개설과목을 최신순으로 조회한다. DEPT_ADMIN 목록용이자,
     * 학생이 담을 수 있는 과목 목록(본인 학과) 조회에도 재사용한다
     *
     * @param deptId 조회할 학과 PK
     * @return 해당 학과의 개설과목 목록(최신순, department fetch join 포함)
     */
    @Query("""
        SELECT c
        FROM CourseOffering c
        JOIN FETCH c.department
        WHERE c.department.deptId = :deptId
        ORDER BY c.createdAt DESC
    """)
    List<CourseOffering> findByDepartment_DeptIdOrderByCreatedAtDesc(@Param("deptId") Long deptId);

    /**
     * 특정 학과의 개설과목 개수를 조회한다.
     *
     * @param deptId 조회할 학과 PK
     * @return 해당 학과의 개설과목 개수
     */
    long countByDepartment_DeptId(Long deptId);
}