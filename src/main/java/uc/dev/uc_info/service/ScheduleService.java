package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.dto.ScheduleDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Schedule;
import uc.dev.uc_info.repository.ScheduleRepository;

import java.util.List;

/**
 * 학사 일정(Schedule) 비즈니스 로직 서비스.
 *
 * <p>컨트롤러는 이 서비스만 호출한다(Repository 직접 호출 금지). 트랜잭션은
 * 이 레이어에서 — 조회 readOnly, 변경 일반.</p>
 *
 * <p>권한(권한 매트릭스): 공지와 동일하다. DEPT_ADMIN 은 본인 학과 + 전체 대상
 * 일정만, SUPER_ADMIN 은 모든 일정을 다룬다. 일정은 상태머신이 없고 visible
 * (노출 여부) 토글만 있어 승인(approve) 단계가 없다.</p>
 *
 * <h3>구현 담당자가 던져야 할 예외 (전역 @ControllerAdvice 에서 받는다)</h3>
 * <ul>
 *   <li><b>EntityNotFoundException</b>: id 로 조회했는데 없을 때 (getSchedule/update/delete).</li>
 *   <li><b>IllegalArgumentException</b>: 입력값이 잘못됐을 때
 *       (title 공백, startDate 없음, endDate &lt; startDate, deptId 가 존재하지 않는 학과 등).</li>
 *   <li><b>권한 예외</b>: DEPT_ADMIN 이 남의 학과 대상에 접근/작성 시
 *       (AccessDeniedException 또는 IllegalStateException).</li>
 * </ul>
 * <p>예외를 던지기만 하면 되고, 화면/응답 변환은 전역 처리(본인 담당)가 맡는다.</p>
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    // TODO(담당): 대상 학과 변환(dto.deptId → Department)을 위해 DepartmentRepository 주입.

    // ============================================================
    // 조회
    // ============================================================

    /**
     * 학사 일정 목록을 조회한다. — 일정 관리 화면(/schedules).
     *
     * <p>권한 분기</p>
     * <ul>
     *   <li>SUPER_ADMIN → 전체 일정(findAllByOrderByStartDateAsc)</li>
     *   <li>DEPT_ADMIN  → 본인 학과 + 전체 대상
     *       (findByDepartmentOrAll(admin.getDepartment().getDeptId()))</li>
     * </ul>
     * <p>예외: 없음(목록은 비어도 정상). DEPT_ADMIN 인데 department 가 null 이면
     *   데이터 이상 → IllegalStateException 으로 방어 가능.</p>
     *
     * @param admin 로그인한 관리자(권한·소속 학과 판단용)
     * @return 권한 범위에 맞는 일정 목록(시작일순)
     */
    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesFor(Admin admin) {
        throw new UnsupportedOperationException(
                "구현 예정: admin.role 분기 → SUPER 전체 / DEPT 본인학과+전체");
    }

    /**
     * 단건 일정을 조회한다. — 수정 모달 진입 시.
     *
     * <p>예외: 없는 id → <b>EntityNotFoundException</b>.</p>
     *
     * @param id 일정 PK
     * @return 일정 엔티티
     */
    @Transactional(readOnly = true)
    public Schedule getSchedule(Long id) {
        throw new UnsupportedOperationException("구현 예정: findById + orElseThrow(EntityNotFoundException)");
    }

    /**
     * 전체 일정 개수. — 화면 상단 'totalCount'.
     *
     * @return 전체 건수 (count 위임). 예외 없음.
     */
    @Transactional(readOnly = true)
    public long countAll() {
        throw new UnsupportedOperationException("구현 예정: count 위임");
    }

    // ============================================================
    // 변경 (작성/수정/삭제)
    // ============================================================

    /**
     * 새 학사 일정을 등록한다. — 등록 모달 제출(POST /schedules).
     *
     * <p>처리 흐름</p>
     * <ol>
     *   <li>새 Schedule 만들고 dto 값 채우기(title, category, targetGrade,
     *       startDate, endDate, visible).</li>
     *   <li>등록자 연결: schedule.setAdmin(admin). (NOT NULL)</li>
     *   <li>대상 학과: dto.deptId 가 있으면 Department 찾아 set, 없으면 null(전체).</li>
     *   <li>visible 미지정(null)이면 기본 true 로 둘지 결정(정책).</li>
     *   <li>save.</li>
     * </ol>
     * <p>여기서 던질 예외(담당자 구현):</p>
     * <ul>
     *   <li>DEPT_ADMIN 이 dto.deptId 를 본인 학과/전체(null) 외로 지정 → <b>권한 예외</b>.</li>
     *   <li>dto.deptId 가 존재하지 않는 학과 → <b>EntityNotFoundException</b> 또는 IllegalArgumentException.</li>
     *   <li>endDate &lt; startDate (둘 다 있을 때) → <b>IllegalArgumentException</b>.</li>
     * </ul>
     *
     * @param dto   등록·검증 DTO
     * @param admin 등록자(로그인 관리자)
     * @return 저장된 일정
     */
    @Transactional
    public Schedule createSchedule(ScheduleDTO dto, Admin admin) {
        throw new UnsupportedOperationException(
                "구현 예정: dto→Schedule 매핑, admin/department 연결, 권한·기간 검증, save");
    }

    /**
     * 기존 학사 일정을 수정한다. — 수정 모달 제출(POST /schedules/{id}/edit).
     *
     * <p>처리 흐름: id 로 조회(없으면 <b>EntityNotFoundException</b>) → DEPT_ADMIN 이면
     *   권한 확인(본인 학과/전체 대상인지) → 필드 갱신
     *   (title/category/targetGrade/startDate/endDate/visible, 대상 학과 dto.deptId → Department or null).</p>
     * <p>기간 역전(endDate &lt; startDate) → IllegalArgumentException.</p>
     *
     * @param id    일정 PK
     * @param dto   수정 DTO
     * @param admin 수정 요청 관리자(권한 판단)
     * @return 수정된 일정
     */
    @Transactional
    public Schedule updateSchedule(Long id, ScheduleDTO dto, Admin admin) {
        throw new UnsupportedOperationException(
                "구현 예정: 조회(EntityNotFound)→권한확인→필드 갱신");
    }

    /**
     * 학사 일정을 삭제한다. — 삭제 버튼(POST /schedules/{id}/delete).
     *
     * <p>처리: id 로 조회(없으면 <b>EntityNotFoundException</b>) → DEPT_ADMIN 권한 확인 →
     *   삭제. 일정은 다른 엔티티가 참조하지 않으므로 물리 삭제 가능
     *   (감추기만 하려면 visible=false 로 두는 방법도 있음 — 정책 결정).</p>
     *
     * @param id    일정 PK
     * @param admin 삭제 관리자(권한 판단)
     */
    @Transactional
    public void deleteSchedule(Long id, Admin admin) {
        throw new UnsupportedOperationException("구현 예정: 조회→권한확인→삭제(또는 visible=false)");
    }
}