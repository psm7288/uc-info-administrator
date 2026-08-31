package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.Notice;

import java.util.List;

/**
 * 공지(Notice) 영속성 접근 인터페이스.
 *
 * <p>JpaRepository 상속으로 save/findById/findAll/delete/count 등 기본 CRUD 기능은 자동 제공된다.
 * 아래 5개의 메서드는 화면 및 서비스 요구사항에 맞춰 직접 선언한 조회 메서드다.</p>
 *
 * <p>공지 상태({@code status})는 DRAFT(임시저장), PUBLISHED(게시중), CLOSED(게시종료)
 * 상수를 가지며, 학과 정보({@code department})가 null인 경우 전체 대상 공지로 간주한다.</p>
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * 전체 공지를 작성일시(createdAt) 기준 내림차순(최신순)으로 조회합니다.
     * <p>최고 관리자(SUPER_ADMIN) 목록 조회용으로 사용됩니다.</p>
     *
     * @return 최신순으로 정렬된 전체 Notice 목록
     */
    @Query("""
            SELECT n FROM Notice n
            JOIN FETCH n.admin
            ORDER BY n.createdAt DESC
            """)
    List<Notice> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 상태(status)의 공지 목록을 작성일시(createdAt) 기준 내림차순(최신순)으로 조회합니다.
     * <p>대시보드/재발송/트래킹(PUBLISHED) 등의 목록 조회용으로 사용됩니다.</p>
     *
     * @param status 조회할 공지 상태 (DRAFT, PUBLISHED, CLOSED)
     * @return 해당 상태를 가진 최신순 Notice 목록
     */
    List<Notice> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * 특정 상태(status)의 공지 개수를 조회합니다.
     * <p>대시보드 및 목록 통계 집계용으로 사용됩니다.</p>

     * @param status 카운트할 공지 상태 (DRAFT, PUBLISHED, CLOSED)
     * @return 해당 상태의 Notice 개수
     */
    long countByStatus(String status);

    /**
     * 학과 관리자(DEPT_ADMIN)용 공지 목록을 조회합니다.
     * <p>본인 학과 공지(department.deptId = deptId)와 전체 대상 공지(department IS NULL)를
     * 작성일시(createdAt) 기준 내림차순으로 정렬하여 반환합니다.</p>
     *
     * @param deptId 조회할 학과 ID
     * @return 학과 및 전체 대상 공지 목록 (최신순)
     */
    @Query("""
            SELECT n
            FROM Notice n
            JOIN FETCH n.admin
            WHERE n.department IS NULL
                OR n.department.deptId = :deptId
            ORDER BY n.createdAt DESC
            """)
    List<Notice> findByDepartmentOrAll(@Param("deptId") Long deptId);

    /**
     * 학생 앱용 노출 공지 목록을 조회합니다.
     * <p>게시중(status = 'PUBLISHED') 상태이면서, 본인 학과 공지 또는 전체 대상 공지만 조회합니다.
     * 고정 여부(pinned) 내림차순, 작성일시(createdAt) 내림차순으로 정렬됩니다.</p>
     *
     * @param deptId 학생의 학과 ID
     * @return 학생에게 노출할 게시중 공지 목록 (상단고정 우선, 최신순)
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