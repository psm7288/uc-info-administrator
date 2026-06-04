package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Schedule;

/**
 * 학사 일정(Schedule) 영속성 접근 인터페이스.
 *
 * <p>JpaRepository 상속으로 기본 CRUD 자동 제공. 아래 주석은 화면·서비스에서
 * 필요한 조회 메서드 목록이다. 직접 선언하면 된다(이름 규칙으로 자동 생성, 복잡하면 @Query).</p>
 *
 * <p>일정은 공지처럼 상태머신이 없고, visible(노출 여부) 토글만 있다.
 * category: ACADEMIC/EXAM/REGISTRATION/VACATION/EVENT/ETC</p>
 *
 * <h3>필요한 조회 메서드 (직접 구현)</h3>
 * <ul>
 *   <li>findAllByOrderByStartDateAsc
 *       — 전체 일정 시작일순. 관리자 목록(SUPER_ADMIN).</li>
 *   <li>countByVisibleTrue (또는 count)
 *       — 노출 일정 개수 / 전체 개수. 화면 상단 통계(totalCount).</li>
 *   <li>findByDepartmentOrAll(deptId)  [@Query 필요]
 *       — DEPT_ADMIN 목록용. 본인 학과(department.deptId=deptId) + 전체 대상(department IS NULL).
 *         정렬 startDate ASC.</li>
 *   <li>findVisibleForStudent(deptId)  [@Query 필요]
 *       — 학생 앱용. visible=true AND (department.deptId=deptId OR department IS NULL).
 *         정렬 startDate ASC.</li>
 * </ul>
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // TODO(담당): 위 주석의 조회 메서드들을 직접 선언.
    //   - 단순 조건은 이름 규칙으로(findAllByOrderByStartDateAsc, countByVisibleTrue 등).
    //   - department OR 전체(null) 같은 OR/NULL 조건은 @Query(JPQL)로:
    //     SELECT s FROM Schedule s
    //     WHERE s.department IS NULL OR s.department.deptId = :deptId
    //     ORDER BY s.startDate ASC
    //   - 참고: JPQL은 엔티티명으로 조회합니다.
}