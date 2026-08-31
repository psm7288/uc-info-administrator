package uc.dev.uc_info.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.common.util.TextNormalizer;
import uc.dev.uc_info.common.validation.AdminScopeValidator;
import uc.dev.uc_info.common.validation.DepartmentResolver;
import uc.dev.uc_info.dto.ScheduleDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Department;
import uc.dev.uc_info.model.Schedule;
import uc.dev.uc_info.repository.ScheduleRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 학사 일정(Schedule) 비즈니스 로직 서비스. 권한 판단/학과 변환은 Notice와
 * 공유하는 {@link AdminScopeValidator}/{@link DepartmentResolver}에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final AdminScopeValidator adminScopeValidator;
    private final DepartmentResolver departmentResolver;

    /**
     * 관리자 권한에 따른 학사 일정 목록을 조회한다.
     *
     * @param admin 로그인한 관리자(권한·소속 학과 판단용)
     * @return 권한 범위에 맞는 일정 전체 목록(시작일 오름차순, 숨김 포함)
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과(admin.department)가 없음(데이터 이상)
     * @throws AccessDeniedException SUPER_ADMIN/DEPT_ADMIN 어느 쪽도 아닌 role
     */
    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesFor(Admin admin) {
        if (adminScopeValidator.isSuperAdmin(admin)) {
            return scheduleRepository.findAllByOrderByStartDateAsc();
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("학과 관리자의 소속 학과 정보가 없습니다.");
            }
            return scheduleRepository.findByDepartmentOrAll(admin.getDepartment().getDeptId());
        }
        throw new AccessDeniedException("학사 일정을 조회할 권한이 없습니다.");
    }

    /**
     * 일정 단건을 PK로 조회한다.
     *
     * @param id 일정 PK
     * @return 조회된 일정 엔티티
     * @throws IllegalArgumentException id가 null인 경우
     * @throws EntityNotFoundException  해당 id의 일정이 없는 경우
     */
    @Transactional(readOnly = true)
    public Schedule getSchedule(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("일정 ID가 필요합니다.");
        }

        return scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("학사 일정을 찾을 수 없습니다."));
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
     * 학생 앱에 노출 중(visible=true)인 일정 개수. 현재는 호출부가 없다.
     *
     * @return 노출 중인 일정 건수
     */
    @Transactional(readOnly = true)
    public long countVisible() {
        return scheduleRepository.countByVisibleTrue();
    }

    /**
     * 특정 학과 학생에게 노출할 일정 목록을 조회한다. Phase 2 학생 앱 REST
     * API용으로 미리 만들어 둔 메서드다.
     *
     * @param deptId 조회할 학생의 소속 학과 PK
     * @return 노출 대상 일정 목록(시작일순)
     * @throws IllegalArgumentException deptId가 null인 경우
     */
    @Transactional(readOnly = true)
    public List<Schedule> findVisibleForStudent(Long deptId) {
        if (deptId == null) {
            throw new IllegalArgumentException("학과 ID가 필요합니다.");
        }
        return scheduleRepository.findVisibleForStudent(deptId);
    }

    /**
     * 새 학사 일정을 등록한다.
     *
     * @param dto   등록 폼 DTO(컨트롤러에서 이미 @Valid 검증 통과)
     * @param admin 등록자(로그인 관리자)
     * @return 저장된 일정
     */
    @Transactional
    public Schedule createSchedule(ScheduleDTO dto, Admin admin) {
        validateDate(dto.getStartDate(), dto.getEndDate());

        Department department = departmentResolver.resolve(dto.getDeptId());
        adminScopeValidator.validateAssignable(admin, department);

        Schedule schedule = new Schedule();
        schedule.setAdmin(admin);
        schedule.setDepartment(department);
        schedule.setTitle(dto.getTitle().trim());
        schedule.setCategory(dto.getCategory());
        schedule.setTargetGrade(TextNormalizer.emptyToNull(dto.getTargetGrade()));
        schedule.setStartDate(dto.getStartDate());
        schedule.setEndDate(dto.getEndDate());
        schedule.setVisible(dto.getVisible() == null ? true : dto.getVisible());

        return scheduleRepository.save(schedule);
    }

    /**
     * 기존 학사 일정을 수정한다. 등록자(admin) 필드는 변경하지 않는다.
     *
     * @param id    수정할 일정 PK
     * @param dto   수정 폼 DTO(컨트롤러에서 이미 @Valid 검증 통과)
     * @param admin 수정 요청 관리자(권한 판단)
     * @return 수정된 일정
     */
    @Transactional
    public Schedule updateSchedule(Long id, ScheduleDTO dto, Admin admin) {
        validateDate(dto.getStartDate(), dto.getEndDate());

        Schedule schedule = getSchedule(id);
        adminScopeValidator.validateAccess(admin, schedule);

        Department department = departmentResolver.resolve(dto.getDeptId());
        adminScopeValidator.validateAssignable(admin, department);

        schedule.setDepartment(department);
        schedule.setTitle(dto.getTitle().trim());
        schedule.setCategory(dto.getCategory());
        schedule.setTargetGrade(TextNormalizer.emptyToNull(dto.getTargetGrade()));
        schedule.setStartDate(dto.getStartDate());
        schedule.setEndDate(dto.getEndDate());
        schedule.setVisible(dto.getVisible() == null ? true : dto.getVisible());

        return scheduleRepository.save(schedule);
    }

    /**
     * 학사 일정을 삭제한다. 다른 엔티티가 FK로 참조하지 않는 구조라(ERD
     * 기준) 소프트 삭제 없이 바로 삭제해도 무방하다.
     *
     * @param id    삭제할 일정 PK
     * @param admin 삭제 요청 관리자(권한 판단)
     */
    @Transactional
    public void deleteSchedule(Long id, Admin admin) {
        Schedule schedule = getSchedule(id);
        adminScopeValidator.validateAccess(admin, schedule);
        scheduleRepository.delete(schedule);
    }

    /**
     * 날짜 범위 검증. startDate는 DTO의 {@code @NotNull}로 이미 보장되지만,
     * 비교 전 NPE 방지용으로 한 번 더 확인한다.
     *
     * @param startDate 시작일
     * @param endDate   종료일(없으면 null)
     * @throws IllegalArgumentException startDate가 없거나, endDate가 startDate보다 빠른 경우
     */
    private void validateDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("일정 시작일은 필수입니다.");
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("일정 종료일은 시작일보다 빠를 수 없습니다.");
        }
    }
}