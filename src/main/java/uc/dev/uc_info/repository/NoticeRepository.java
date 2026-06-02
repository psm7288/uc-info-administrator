package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Notice;

/**
 * 공지(Notice) 영속성 접근 인터페이스.
 *
 * <p>JpaRepository 상속으로 save/findById/findAll/delete/count 등 기본 CRUD 자동 제공.
 * 아래 주석은 화면·서비스에서 필요한 조회 메서드 목록이다. 직접 메서드 시그니처를
 * 작성하면 된다(Spring Data 가 메서드 이름 규칙으로 쿼리 자동 생성, 복잡하면 @Query).</p>
 *
 * <p>상태(status): DRAFT(임시저장)/WAITING(검토중)/PUBLISHED(게시중)/CLOSED(게시종료)</p>
 *
 * <h3>필요한 조회 메서드 (직접 구현)</h3>
 * <ul>
 *   <li>findAllByOrderByCreatedAtDesc
 *       — 전체 공지 최신순. 관리자 목록(SUPER_ADMIN).</li>
 *   <li>findByStatusOrderByCreatedAtDesc(status)
 *       — 특정 상태 공지 최신순. 대시보드/재발송/트래킹(PUBLISHED), 승인대기(WAITING) 등.</li>
 *   <li>countByStatus(status)
 *       — 상태별 개수. 대시보드/목록 통계.</li>
 *   <li>findByDepartmentOrAll(deptId)  [@Query 필요]
 *       — DEPT_ADMIN 목록용. 본인 학과(department.deptId=deptId) + 전체 대상(department IS NULL).
 *         정렬 createdAt DESC.</li>
 *   <li>findPublishedForStudent(deptId)  [@Query 필요]
 *       — 학생 앱용. status='PUBLISHED' AND (department.deptId=deptId OR department IS NULL).
 *         정렬 pinned DESC, createdAt DESC.</li>
 * </ul>
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // TODO(담당): 위 주석의 조회 메서드들을 직접 선언.
    //   - 단순 조건은 메서드 이름은 위와 같은 규칙입니다.
    //   - department OR 전체(null) 처럼 OR/NULL 조건은 @Query(JPQL)입니다.
    //   - 참고 : JPQL는 엔티티명으로 조회를 합니다.
}