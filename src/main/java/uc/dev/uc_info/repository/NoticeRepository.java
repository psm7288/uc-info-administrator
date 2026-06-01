package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.Notice;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /** 게시중 공지 최신순 */
    List<Notice> findByStatusOrderByCreatedAtDesc(String status);

    /** 특정 학과 대상 또는 전체 대상 공지 */
    @Query("SELECT n FROM Notice n WHERE n.status = 'PUBLISHED' " +
            "AND (n.targetDeptId = :deptId OR n.targetDeptId IS NULL) " +
            "ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findPublishedByDept(@Param("deptId") Integer deptId);

    /** 전체 공지 최신순(관리자 목록용) */
    List<Notice> findAllByOrderByCreatedAtDesc();

    /** 상태별 카운트 */
    long countByStatus(String status);
}