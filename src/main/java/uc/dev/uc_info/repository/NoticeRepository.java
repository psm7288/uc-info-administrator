package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.Notice;

import java.util.List;

/**
 * 공지(Notice) Repository.
 *
 * JpaRepository 기본 CRUD:
 * - save()
 * - findById()
 * - findAll()
 * - delete()
 * - count()
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * 전체 공지를 최신순으로 조회.
     * SUPER_ADMIN 공지 관리 화면에서 사용.
     */
    List<Notice> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 상태의 공지를 최신순으로 조회.
     *
     * 예:
     * PUBLISHED
     * WAITING
     * DRAFT
     * CLOSED
     */
    List<Notice> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * 특정 상태의 공지 개수.
     */
    long countByStatus(String status);

    /**
     * DEPT_ADMIN용 공지 목록.
     *
     * 자신의 학과 공지
     * +
     * 전체 학과 대상 공지(department = null)
     */
    @Query("""
            SELECT n
            FROM Notice n
            WHERE n.department IS NULL
               OR n.department.deptId = :deptId
            ORDER BY n.createdAt DESC
            """)
    List<Notice> findByDepartmentOrAll(@Param("deptId") Long deptId);

    /**
     * 학생에게 노출할 게시중 공지.
     *
     * 게시 상태(PUBLISHED)이면서
     * 자신의 학과 공지 또는 전체 대상 공지만 조회.
     */
    @Query("""
            SELECT n
            FROM Notice n
            WHERE n.status = 'PUBLISHED'
              AND (
                    n.department IS NULL
                    OR n.department.deptId = :deptId
                  )
            ORDER BY n.pinned DESC, n.createdAt DESC
            """)
    List<Notice> findPublishedForStudent(@Param("deptId") Long deptId);
}