package uc.dev.uc_info.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.dto.ScheduleDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Department;
import uc.dev.uc_info.model.Schedule;
import uc.dev.uc_info.repository.DepartmentRepository;
import uc.dev.uc_info.repository.ScheduleRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 학사 일정(Schedule) 비즈니스 로직 서비스.
 *
 * <p>컨트롤러는 이 서비스만 호출한다(Repository 직접 호출 금지). 트랜잭션은
 * 이 레이어에서 건다 — 조회는 readOnly, 변경(등록/수정/삭제)은 일반 트랜잭션.</p>
 *
 * <p>권한(권한 매트릭스): 공지와 동일한 규칙을 따른다. SUPER_ADMIN 은 모든 일정을,
 * DEPT_ADMIN 은 본인 학과 + 전체 대상(department=null) 일정만 다룬다. 일정은
 * 공지처럼 상태머신(DRAFT/WAITING/PUBLISHED)이 없고, {@code visible} 토글만 있다.
 * 관리자 목록 화면({@link #findSchedulesFor})은 숨김(visible=false) 일정도 그대로
 * 포함한다 — visible 은 "학생 앱에 보일지"만 결정할 뿐, 관리자는 숨긴 일정도
 * 확인·수정할 수 있어야 하기 때문이다. 학생 앱 노출 여부는 {@link #findVisibleForStudent}
 * 가 별도로 담당한다.</p>
 *
 * <p><b>검증 책임 분리</b>: title/category 공백, startDate 필수 같은 입력 형식 검증은
 * {@link ScheduleDTO} 의 Bean Validation 애노테이션({@code @NotBlank} 등)과 컨트롤러의
 * {@code BindingResult} 가 처리한다. 로그인 여부는 SecurityConfig 가 {@code /schedules/**}
 * 를 인증 필수로 막고 있어 이 서비스에 도달한 시점엔 admin 이 항상 존재한다. 그래서 이
 * 서비스는 그 두 가지를 다시 검사하지 않고, <b>DB 조회가 필요하거나 로그인 사용자의
 * 권한에 따라 달라지는 검증</b>(존재하는 학과인지, 날짜 역전 여부, 본인 학과/전체 대상인지)
 * 만 담당한다.</p>
 *
 * <h3>이 서비스가 던지는 예외 (전역 @ControllerAdvice 에서 응답으로 변환)</h3>
 * <ul>
 *   <li><b>EntityNotFoundException</b>: id 로 조회했는데 없을 때(getSchedule/update/delete),
 *       dto.deptId 가 존재하지 않는 학과일 때({@link #resolveDepartment}).</li>
 *   <li><b>IllegalArgumentException</b>: id/deptId 가 null 로 넘어온 경우,
 *       endDate 가 startDate 보다 빠른 경우({@link #validateDate}).</li>
 *   <li><b>IllegalStateException</b>: DEPT_ADMIN 인데 소속 학과 정보가 없는 데이터 이상 상황.</li>
 *   <li><b>AccessDeniedException</b>: 학사 일정 관리 권한 자체가 없는 role,
 *       DEPT_ADMIN 이 다른 학과 일정을 등록/조회/수정/삭제하려 할 때.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * 관리자 권한에 따른 학사 일정 목록을 조회한다.
     *
     * <p>처리 흐름</p>
     * <ol>
     *   <li>SUPER_ADMIN → 전체 일정, 시작일 오름차순
     *       ({@link ScheduleRepository#findAllByOrderByStartDateAsc()}).</li>
     *   <li>DEPT_ADMIN → 본인 학과 + 전체 대상(department=null) 일정
     *       ({@link ScheduleRepository#findByDepartmentOrAll(Long)}).</li>
     * </ol>
     * <p>숨김(visible=false) 일정도 포함해서 반환한다 — 관리자는 노출 여부와 무관하게
     * 전부 볼 수 있어야 토글/수정/삭제가 가능하다. 학생에게 보일 목록은
     * {@link #findVisibleForStudent} 가 따로 처리한다.</p>
     * <p>admin 은 SecurityConfig 가 이미 인증을 보장하므로 null 체크를 별도로 하지 않는다.</p>
     *
     * <p>예외</p>
     * <ul>
     *   <li>DEPT_ADMIN 인데 소속 학과(admin.department)가 없음 → <b>IllegalStateException</b>(데이터 이상)</li>
     *   <li>SUPER_ADMIN/DEPT_ADMIN 어느 쪽도 아닌 role → <b>AccessDeniedException</b></li>
     * </ul>
     *
     * @param admin 로그인한 관리자(권한·소속 학과 판단용)
     * @return 권한 범위에 맞는 일정 전체 목록(시작일 오름차순, 숨김 포함)
     */
    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesFor(Admin admin) {
        if (isSuperAdmin(admin)) {
            return scheduleRepository.findAllByOrderByStartDateAsc();
        }

        if (isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("학과 관리자의 소속 학과 정보가 없습니다.");
            }

            Long deptId = admin.getDepartment().getDeptId();
            return scheduleRepository.findByDepartmentOrAll(deptId);
        }

        throw new AccessDeniedException("학사 일정을 조회할 권한이 없습니다.");
    }

    /**
     * 일정 단건을 PK로 조회한다.
     *
     * @param id 일정 PK
     * @return 조회된 일정 엔티티
     * @throws IllegalArgumentException id 가 null 인 경우
     * @throws EntityNotFoundException  해당 id 의 일정이 없는 경우
     */
    @Transactional(readOnly = true)
    public Schedule getSchedule(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("일정 ID가 필요합니다.");
        }

        return scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("학사 일정을 찾을 수 없습니다. id=" + id));
    }

    /**
     * 전체 일정 개수(숨김 포함).
     *
     * @return 전체 일정 건수
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return scheduleRepository.count();
    }

    /**
     * 학생 앱에 노출 중(visible=true)인 일정 개수. 통계용으로 만들어 둔 메서드이며,
     * 현재는 어느 화면에서도 호출하지 않는다(추후 대시보드 등에서 사용 가능).
     *
     * @return 노출 중인 일정 건수
     */
    @Transactional(readOnly = true)
    public long countVisible() {
        return scheduleRepository.countByVisibleTrue();
    }

    /**
     * 특정 학과 학생에게 노출할 일정 목록을 조회한다. — Phase 2 학생 앱 REST API 용으로
     * 미리 만들어 둔 메서드다. 현재 이 서비스를 호출하는 컨트롤러는 아직 없다.
     *
     * <p>조건: visible=true 이면서 본인 학과 또는 전체 대상(department=null) 일정,
     * 시작일 오름차순({@link ScheduleRepository#findVisibleForStudent(Long)}).</p>
     *
     * @param deptId 조회할 학생의 소속 학과 PK
     * @return 노출 대상 일정 목록(시작일순)
     * @throws IllegalArgumentException deptId 가 null 인 경우
     */
    @Transactional(readOnly = true)
    public List<Schedule> findVisibleForStudent(Long deptId) {
        if (deptId == null) {
            throw new IllegalArgumentException("학과 ID가 필요합니다.");
        }

        return scheduleRepository.findVisibleForStudent(deptId);
    }

    /**
     * 새 학사 일정을 등록한다. — 등록 모달 제출(POST /schedules).
     *
     * <p>처리 흐름</p>
     * <ol>
     *   <li>title/category 공백, startDate 필수 여부는 {@link ScheduleDTO} 의
     *       Bean Validation + 컨트롤러의 BindingResult 가 이미 걸러냈다고 보고 재검사하지 않는다.</li>
     *   <li>기간 검증: endDate가 있으면 startDate보다 빠르면 안 됨({@link #validateDate}) —
     *       필드 하나짜리 애노테이션으로 표현할 수 없는 두 필드 간 비교라 여기서 확인한다.</li>
     *   <li>dto.deptId → Department 변환. null이면 전체 대상({@link #resolveDepartment}).</li>
     *   <li>대상 학과에 대한 등록 권한 확인({@link #validateDepartmentPermission}) —
     *       DEPT_ADMIN 은 본인 학과 또는 전체(null)만 지정 가능.</li>
     *   <li>새 Schedule 생성 후 admin(등록자)/department/title/category/targetGrade/
     *       startDate/endDate/visible 을 채우고 save.</li>
     * </ol>
     * <p>targetGrade 는 빈 문자열이면 null(전체 학년)로 정규화한다({@link #emptyToNull}).
     * visible 은 폼에서 체크박스가 전송되지 않으면 null 로 들어올 수 있어, null 이면
     * 기본값 true(노출)로 처리한다.</p>
     *
     * <p>예외</p>
     * <ul>
     *   <li>endDate &lt; startDate → <b>IllegalArgumentException</b></li>
     *   <li>dto.deptId 가 존재하지 않는 학과 → <b>EntityNotFoundException</b></li>
     *   <li>DEPT_ADMIN 이 본인 학과/전체 외의 학과를 지정 → <b>AccessDeniedException</b></li>
     * </ul>
     *
     * @param dto   등록 폼 DTO(컨트롤러에서 이미 @Valid 검증 통과)
     * @param admin 등록자(로그인 관리자)
     * @return 저장된 일정
     */
    @Transactional
    public Schedule createSchedule(ScheduleDTO dto,Admin admin){
        validateDate(dto.getStartDate(),dto.getEndDate());

        Department department = resolveDepartment(dto.getDeptId());
        validateDepartmentPermission(admin,department);

        Schedule schedule = new Schedule();

        schedule.setAdmin(admin);
        schedule.setDepartment(department);
        schedule.setTitle(dto.getTitle().trim());
        schedule.setCategory(dto.getCategory());
        schedule.setTargetGrade(emptyToNull(dto.getTargetGrade()));
        schedule.setStartDate(dto.getStartDate());
        schedule.setEndDate(dto.getEndDate());
        /*
         * 체크박스가 전송되지 않으면 null이 될 수 있으므로
         * null이면 기본적으로 true 처리
         */
        schedule.setVisible(
                dto.getVisible() == null
                        ? true
                        : dto.getVisible()
        );

        return scheduleRepository.save(schedule);
    }

    /**
     * 기존 학사 일정을 수정한다.
     *
     * <p>처리 흐름</p>
     * <ol>
     *   <li>기간 검증({@link #createSchedule}과 동일하게 {@link #validateDate} 만 수행).</li>
     *   <li>id 로 기존 일정 조회({@link #getSchedule}).</li>
     *   <li>기존 일정에 대한 접근 권한 확인({@link #validateSchedulePermission}) —
     *       DEPT_ADMIN 은 본인 학과 또는 전체 대상 일정만 수정 가능.</li>
     *   <li>dto.deptId → Department 변환 후, 변경하려는 대상 학과에 대한 권한도
     *       별도로 확인({@link #validateDepartmentPermission}) — 예를 들어 DEPT_ADMIN 이
     *       본인 학과 일정을 다른 학과로 옮기려는 시도를 막는다.</li>
     *   <li>department/title/category/targetGrade/startDate/endDate/visible 을 갱신 후 save.</li>
     * </ol>
     * <p>등록자(admin) 필드는 수정 시 변경하지 않는다 — 최초 등록자 정보를 그대로 유지한다.</p>
     *
     * <p>예외</p>
     * <ul>
     *   <li>id 없는 일정 → <b>EntityNotFoundException</b></li>
     *   <li>endDate &lt; startDate → <b>IllegalArgumentException</b></li>
     *   <li>DEPT_ADMIN 이 다른 학과 소속 일정에 접근하거나, 다른 학과로 변경 시도 → <b>AccessDeniedException</b></li>
     * </ul>
     *
     * @param id    수정할 일정 PK
     * @param dto   수정 폼 DTO(컨트롤러에서 이미 @Valid 검증 통과)
     * @param admin 수정 요청 관리자(권한 판단)
     * @return 수정된 일정
     */
    @Transactional
    public Schedule updateSchedule(Long id,ScheduleDTO dto,Admin admin) {
        validateDate(dto.getStartDate(),dto.getEndDate());

        Schedule schedule = getSchedule(id);
        validateSchedulePermission(admin,schedule);

        Department department = resolveDepartment(dto.getDeptId());

        /*
         * 변경하려는 대상 학과 권한 확인
         */
        validateDepartmentPermission(admin,department);

        schedule.setDepartment(department);
        schedule.setTitle(dto.getTitle().trim());
        schedule.setCategory(dto.getCategory());
        schedule.setTargetGrade(emptyToNull(dto.getTargetGrade()));
        schedule.setStartDate(dto.getStartDate());
        schedule.setEndDate(dto.getEndDate());
        schedule.setVisible(
                dto.getVisible() == null
                        ? true
                        : dto.getVisible()
        );

        return scheduleRepository.save(schedule);
    }

    /**
     * 학사 일정을 삭제한다. — 삭제 버튼(POST /schedules/{id}/delete).
     *
     * <p>처리: id로 조회({@link #getSchedule}) → 접근 권한 확인
     * ({@link #validateSchedulePermission}) → 물리 삭제.</p>
     * <p>Schedule 은 다른 엔티티가 FK 로 참조하지 않는 구조라(ERD 기준) 소프트 삭제 없이
     * 바로 삭제해도 무방하다.</p>
     *
     * <p>예외</p>
     * <ul>
     *   <li>id 없는 일정 → <b>EntityNotFoundException</b></li>
     *   <li>DEPT_ADMIN 이 다른 학과 소속 일정을 삭제하려 함 → <b>AccessDeniedException</b></li>
     * </ul>
     *
     * @param id    삭제할 일정 PK
     * @param admin 삭제 요청 관리자(권한 판단)
     */
    @Transactional
    public void deleteSchedule(Long id, Admin admin) {
        Schedule schedule = getSchedule(id);
        validateSchedulePermission(admin,schedule);

        scheduleRepository.delete(schedule);
    }

    /**
     * 날짜 범위 검증. startDate 는 필수(호출 시점엔 DTO 의 {@code @NotNull} 로
     * 이미 보장됨)이며, endDate 가 있으면 startDate 보다 빠를 수 없다(단일 일정처럼
     * endDate 가 없는 경우는 통과). startDate 가 혹시라도 null 로 들어오는 경우를
     * 대비해 비교 전에 한 번 더 확인한다(NPE 방지용 방어 코드).
     *
     * @param startDate 시작일
     * @param endDate   종료일(없으면 null)
     * @throws IllegalArgumentException startDate 가 없거나, endDate 가 startDate 보다 빠른 경우
     */
    private void validateDate(LocalDate startDate,LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("일정 시작일은 필수입니다.");
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("일정 종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    /**
     * DTO 의 deptId → Department 엔티티로 변환한다. deptId가 null이면
     * 전체 학과 대상이라는 의미이므로 null 을 그대로 반환한다.
     *
     * @param deptId 변환할 학과 PK(전체 대상이면 null)
     * @return 조회된 Department, 또는 deptId 가 null 이면 null
     * @throws EntityNotFoundException deptId 에 해당하는 학과가 없는 경우
     */
    private Department resolveDepartment(Long deptId){
        if (deptId == null) {
            return null;
        }

        return departmentRepository
                .findById(deptId)
                .orElseThrow(() ->
                        new EntityNotFoundException("존재하지 않는 학과입니다."));
    }

    /**
     * 관리자가 이 대상 학과를 지정(등록/변경)할 권한이 있는지 검사한다.
     * SUPER_ADMIN 은 항상 통과. DEPT_ADMIN 은 department 가 null(전체 대상)
     * 이거나 본인 소속 학과와 같아야 통과한다.
     *
     * @param admin      권한을 확인할 관리자
     * @param department 지정하려는 대상 학과(전체 대상이면 null)
     * @throws IllegalStateException  DEPT_ADMIN 인데 소속 학과 정보가 없는 경우(데이터 이상)
     * @throws AccessDeniedException  DEPT_ADMIN 이 본인 학과가 아닌 다른 학과를 지정하려는 경우,
     *                                또는 admin 이 SUPER_ADMIN/DEPT_ADMIN 어느 쪽도 아닌 경우
     */
    private void validateDepartmentPermission(Admin admin,Department department) {
        if (isSuperAdmin(admin)) {
            return;
        }

        if (isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("학과 관리자의 소속 학과가 없습니다.");
            }

            if (department == null) {
                return;
            }
            Long adminDeptId = admin.getDepartment().getDeptId();
            Long targetDeptId = department.getDeptId();

            if (!adminDeptId.equals(targetDeptId)) {
                throw new AccessDeniedException("다른 학과의 일정을 등록할 수 없습니다.");
            }
            return;
        }
        throw new AccessDeniedException("학사 일정 관리 권한이 없습니다.");
    }

    /**
     * 이미 존재하는 일정에 대한 접근(수정/삭제) 권한을 검사한다.
     * SUPER_ADMIN 은 항상 통과. DEPT_ADMIN 은 일정의 대상 학과가 null(전체 대상)
     * 이거나 본인 소속 학과와 같아야 통과한다.
     *
     * <p>{@link #validateDepartmentPermission} 과의 차이: 이건 "이미 등록된 일정"의
     * 현재 소속을 검사하고, 저건 "새로 지정하려는 학과"를 검사한다. 수정 시에는
     * 두 검사를 모두 거친다.</p>
     *
     * @param admin    권한을 확인할 관리자
     * @param schedule 접근하려는 기존 일정
     * @throws AccessDeniedException DEPT_ADMIN 이 아닌 다른 권한이거나, 다른 학과 소속 일정에 접근하는 경우
     * @throws IllegalStateException DEPT_ADMIN 인데 소속 학과 정보가 없는 경우(데이터 이상)
     */
    private void validateSchedulePermission(Admin admin,Schedule schedule) {
        if (isSuperAdmin(admin)) {
            return;
        }

        if (!isDeptAdmin(admin)) {
            throw new AccessDeniedException("학사 일정에 접근할 권한이 없습니다.");
        }

        if (admin.getDepartment() == null) {
            throw new IllegalStateException("관리자의 학과 정보가 없습니다.");
        }

        if (schedule.getDepartment() == null) {
            return;
        }

        Long adminDeptId = admin.getDepartment().getDeptId();
        Long scheduleDeptId = schedule.getDepartment().getDeptId();

        if (!adminDeptId.equals(scheduleDeptId)) {
            throw new AccessDeniedException("다른 학과의 일정에 접근할 수 없습니다.");
        }
    }

    /**
     * admin.role 이 "SUPER_ADMIN" 인지 확인한다.
     *
     * @param admin 확인할 관리자
     * @return SUPER_ADMIN 이면 true
     */
    private boolean isSuperAdmin(Admin admin) {
        return "SUPER_ADMIN".equals(
                String.valueOf(admin.getRole())
        );
    }

    /**
     * admin.role 이 "DEPT_ADMIN" 인지 확인한다.
     *
     * @param admin 확인할 관리자
     * @return DEPT_ADMIN 이면 true
     */
    private boolean isDeptAdmin(Admin admin){
        return "DEPT_ADMIN".equals(
                String.valueOf(admin.getRole())
        );
    }

    /**
     * 빈 문자열/공백만 있는 문자열을 null로 바꾼다. targetGrade 처럼
     * "선택 안 함"이 빈 문자열로 넘어오는 필드를 "전체 대상(null)"으로
     * 정규화할 때 쓴다.
     *
     * @param value 원본 문자열
     * @return value 가 null 이거나 공백뿐이면 null, 아니면 value 그대로
     */
    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}