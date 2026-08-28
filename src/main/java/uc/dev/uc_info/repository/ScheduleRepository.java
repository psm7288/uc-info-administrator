package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.Schedule;

import java.util.List;

/**
 * 학사 일정(Schedule) 영속성 접근 인터페이스.
 *
 * <p>JpaRepository 상속으로 기본 CRUD(findById/save/delete/count 등)는 자동 제공된다.
 * 아래 4개는 화면·서비스 요구사항에 맞춰 직접 선언한 조회 메서드다.</p>
 *
 * <p>일정(Schedule)은 공지(Notice)와 달리 게시 상태머신(DRAFT/WAITING/PUBLISHED)이 없고,
 * {@code visible}(노출 여부) 토글만 가진다. {@code category} 는 ACADEMIC/EXAM/
 * REGISTRATION/VACATION/EVENT/ETC 중 하나이며 문자열 컬럼이다(DB enum 아님).</p>
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * 전체 일정을 시작일 오름차순으로 조회한다. (SUPER_ADMIN 의 일정 관리 목록용)<br>
     * 숨김(visible=false) 여부와 무관하게 전부 반환한다.<br>
     * 관리자 화면에서는 숨긴 일정도 확인·수정할 수 있어야 하기 때문이다.
     *
     * @return 시작일순 전체 일정 목록(숨김 포함)
     */
    List<Schedule> findAllByOrderByStartDateAsc();

    /**
     * 학생 앱에 노출 중(visible=true)인 일정 개수. (지금 사용 x)
     *
     * @return 노출 중인 일정 건수
     */
    long countByVisibleTrue();

    /**
     * 학과 관리자(DEPT_ADMIN)용 일정 목록 조회.
     *
     * <p>본인 학과(department.deptId = deptId) 또는 전체 대상(department IS NULL)
     * 일정을 시작일 오름차순으로 반환한다.<br>
     * 숨김(visible=false) 여부와 무관하게 전부 반환한다 <br>
     * {@link #findAllByOrderByStartDateAsc()} 와 동일하게
     * "관리자는 숨긴 일정도 봐야 한다"는 원칙을 따른다.</p>
     *
     * @param deptId 조회 기준 학과 PK(로그인 admin 의 소속 학과)
     * @return 본인 학과 + 전체 대상 일정 목록(시작일순, 숨김 포함)
     */
    @Query("""
        SELECT s
        FROM Schedule s
        WHERE s.department IS NULL
           OR s.department.deptId = :deptId
        ORDER BY s.startDate ASC
    """)
    List<Schedule> findByDepartmentOrAll(@Param("deptId") Long deptId);

    /**
     * 학생 앱 노출용 일정 목록 조회.
     *
     * <p>visible=true 이면서 본인 학과 또는 전체 대상(department IS NULL) 일정을
     * 시작일 오름차순으로 반환한다.<br>
     * Phase 2 에서 학생 앱(Flutter) REST API 연동 시 사용될 예정이며,
     * 현재는 호출부(컨트롤러)가 아직 없다.</p>
     *
     * @param deptId 조회할 학생의 소속 학과 PK
     * @return 노출 대상 일정 목록(시작일순)
     */
    @Query("""
        SELECT s
        FROM Schedule s
        WHERE s.visible = true
          AND (
                s.department IS NULL
                OR s.department.deptId = :deptId
              )
        ORDER BY s.startDate ASC
    """)
    List<Schedule> findVisibleForStudent(@Param("deptId") Long deptId);
}