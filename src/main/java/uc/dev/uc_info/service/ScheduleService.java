//package uc.dev.uc_info.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import uc.dev.uc_info.dto.ScheduleDTO;
//import uc.dev.uc_info.model.Admin;
//import uc.dev.uc_info.model.Schedule;
//import uc.dev.uc_info.repository.ScheduleRepository;
//
//import java.util.List;
//
///**
// * 학사 일정(Schedule) 비즈니스 로직 서비스.
// *
// * <p>컨트롤러는 이 서비스만 호출한다(Repository 직접 호출 금지). 트랜잭션은
// * 이 레이어에서 — 조회 readOnly, 변경 일반.</p>
// *
// * <p>권한(권한 매트릭스): 공지와 동일하다. DEPT_ADMIN 은 본인 학과 + 전체 대상
// * 일정만, SUPER_ADMIN 은 모든 일정을 다룬다. 일정은 상태머신이 없고 visible
// * (노출 여부) 토글만 있어 승인(approve) 단계가 없다.</p>
// *
// * <h3>구현 담당자가 던져야 할 예외 (전역 @ControllerAdvice 에서 받는다)</h3>
// * <ul>
// *   <li><b>EntityNotFoundException</b>: id 로 조회했는데 없을 때 (getSchedule/update/delete).</li>
// *   <li><b>IllegalArgumentException</b>: 입력값이 잘못됐을 때
// *       (title 공백, startDate 없음, endDate &lt; startDate, deptId 가 존재하지 않는 학과 등).</li>
// *   <li><b>권한 예외</b>: DEPT_ADMIN 이 남의 학과 대상에 접근/작성 시
// *       (AccessDeniedException 또는 IllegalStateException).</li>
// * </ul>
// * <p>예외를 던지기만 하면 되고, 화면/응답 변환은 전역 처리(본인 담당)가 맡는다.</p>
// */
//@Service
//@RequiredArgsConstructor
//public class ScheduleService {
//
//    private final ScheduleRepository scheduleRepository;
//    // TODO(담당): 대상 학과 변환(dto.deptId → Department)을 위해 DepartmentRepository 주입.
//
//    // ============================================================
//    // 조회
//    // ============================================================
//
//    /**
//     * 학사 일정 목록을 조회한다. — 일정 관리 화면(/schedules).
//     *
//     * <p>권한 분기</p>
//     * <ul>
//     *   <li>SUPER_ADMIN → 전체 일정(findAllByOrderByStartDateAsc)</li>
//     *   <li>DEPT_ADMIN  → 본인 학과 + 전체 대상
//     *       (findByDepartmentOrAll(admin.getDepartment().getDeptId()))</li>
//     * </ul>
//     * <p>예외: 없음(목록은 비어도 정상). DEPT_ADMIN 인데 department 가 null 이면
//     *   데이터 이상 → IllegalStateException 으로 방어 가능.</p>
//     *
//     * @param admin 로그인한 관리자(권한·소속 학과 판단용)
//     * @return 권한 범위에 맞는 일정 목록(시작일순)
//     */
//    @Transactional(readOnly = true)
//    public List<Schedule> findSchedulesFor(Admin admin) {
//        throw new UnsupportedOperationException(
//                "구현 예정: admin.role 분기 → SUPER 전체 / DEPT 본인학과+전체");
//    }
//
//    /**
//     * 단건 일정을 조회한다. — 수정 모달 진입 시.
//     *
//     * <p>예외: 없는 id → <b>EntityNotFoundException</b>.</p>
//     *
//     * @param id 일정 PK
//     * @return 일정 엔티티
//     */
//    @Transactional(readOnly = true)
//    public Schedule getSchedule(Long id) {
//        throw new UnsupportedOperationException("구현 예정: findById + orElseThrow(EntityNotFoundException)");
//    }
//
//    /**
//     * 전체 일정 개수. — 화면 상단 'totalCount'.
//     *
//     * @return 전체 건수 (count 위임). 예외 없음.
//     */
//    @Transactional(readOnly = true)
//    public long countAll() {
//        throw new UnsupportedOperationException("구현 예정: count 위임");
//    }
//
//    // ============================================================
//    // 변경 (작성/수정/삭제)
//    // ============================================================
//
//    /**
//     * 새 학사 일정을 등록한다. — 등록 모달 제출(POST /schedules).
//     *
//     * <p>처리 흐름</p>
//     * <ol>
//     *   <li>새 Schedule 만들고 dto 값 채우기(title, category, targetGrade,
//     *       startDate, endDate, visible).</li>
//     *   <li>등록자 연결: schedule.setAdmin(admin). (NOT NULL)</li>
//     *   <li>대상 학과: dto.deptId 가 있으면 Department 찾아 set, 없으면 null(전체).</li>
//     *   <li>visible 미지정(null)이면 기본 true 로 둘지 결정(정책).</li>
//     *   <li>save.</li>
//     * </ol>
//     * <p>여기서 던질 예외(담당자 구현):</p>
//     * <ul>
//     *   <li>DEPT_ADMIN 이 dto.deptId 를 본인 학과/전체(null) 외로 지정 → <b>권한 예외</b>.</li>
//     *   <li>dto.deptId 가 존재하지 않는 학과 → <b>EntityNotFoundException</b> 또는 IllegalArgumentException.</li>
//     *   <li>endDate &lt; startDate (둘 다 있을 때) → <b>IllegalArgumentException</b>.</li>
//     * </ul>
//     *
//     * @param dto   등록·검증 DTO
//     * @param admin 등록자(로그인 관리자)
//     * @return 저장된 일정
//     */
//    @Transactional
//    public Schedule createSchedule(ScheduleDTO dto, Admin admin) {
//        throw new UnsupportedOperationException(
//                "구현 예정: dto→Schedule 매핑, admin/department 연결, 권한·기간 검증, save");
//    }
//
//    /**
//     * 기존 학사 일정을 수정한다. — 수정 모달 제출(POST /schedules/{id}/edit).
//     *
//     * <p>처리 흐름: id 로 조회(없으면 <b>EntityNotFoundException</b>) → DEPT_ADMIN 이면
//     *   권한 확인(본인 학과/전체 대상인지) → 필드 갱신
//     *   (title/category/targetGrade/startDate/endDate/visible, 대상 학과 dto.deptId → Department or null).</p>
//     * <p>기간 역전(endDate &lt; startDate) → IllegalArgumentException.</p>
//     *
//     * @param id    일정 PK
//     * @param dto   수정 DTO
//     * @param admin 수정 요청 관리자(권한 판단)
//     * @return 수정된 일정
//     */
//    @Transactional
//    public Schedule updateSchedule(Long id, ScheduleDTO dto, Admin admin) {
//        throw new UnsupportedOperationException(
//                "구현 예정: 조회(EntityNotFound)→권한확인→필드 갱신");
//    }
//
//    /**
//     * 학사 일정을 삭제한다. — 삭제 버튼(POST /schedules/{id}/delete).
//     *
//     * <p>처리: id 로 조회(없으면 <b>EntityNotFoundException</b>) → DEPT_ADMIN 권한 확인 →
//     *   삭제. 일정은 다른 엔티티가 참조하지 않으므로 물리 삭제 가능
//     *   (감추기만 하려면 visible=false 로 두는 방법도 있음 — 정책 결정).</p>
//     *
//     * @param id    일정 PK
//     * @param admin 삭제 관리자(권한 판단)
//     */
//    @Transactional
//    public void deleteSchedule(Long id, Admin admin) {
//        throw new UnsupportedOperationException("구현 예정: 조회→권한확인→삭제(또는 visible=false)");
//    }
//}

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

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final DepartmentRepository departmentRepository;

    // ============================================================
    // 조회
    // ============================================================

    /**
     * 관리자 권한에 따른 일정 목록 조회
     *
     * SUPER_ADMIN : 전체 일정
     * DEPT_ADMIN  : 본인 학과 + 전체 대상 일정
     */
    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesFor(Admin admin) {

        validateAdmin(admin);

        if (isSuperAdmin(admin)) {
            return scheduleRepository.findAllByOrderByStartDateAsc();
        }

        if (isDeptAdmin(admin)) {

            if (admin.getDepartment() == null) {
                throw new IllegalStateException(
                        "학과 관리자의 소속 학과 정보가 없습니다."
                );
            }

            Long deptId = admin.getDepartment().getDeptId();

            return scheduleRepository.findByDepartmentOrAll(deptId);
        }

        throw new AccessDeniedException(
                "학사 일정을 조회할 권한이 없습니다."
        );
    }


    /**
     * 일정 단건 조회
     */
    @Transactional(readOnly = true)
    public Schedule getSchedule(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "일정 ID가 필요합니다."
            );
        }

        return scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "학사 일정을 찾을 수 없습니다. id=" + id
                        )
                );
    }


    /**
     * 전체 일정 개수
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return scheduleRepository.count();
    }


    /**
     * 노출 중인 일정 개수
     */
    @Transactional(readOnly = true)
    public long countVisible() {
        return scheduleRepository.countByVisibleTrue();
    }


    /**
     * 학생용 노출 일정 조회
     */
    @Transactional(readOnly = true)
    public List<Schedule> findVisibleForStudent(Long deptId) {

        if (deptId == null) {
            throw new IllegalArgumentException(
                    "학과 ID가 필요합니다."
            );
        }

        return scheduleRepository.findVisibleForStudent(deptId);
    }


    // ============================================================
    // 등록
    // ============================================================

    @Transactional
    public Schedule createSchedule(
            ScheduleDTO dto,
            Admin admin
    ) {

        validateAdmin(admin);
        validateDto(dto);
        validateDate(
                dto.getStartDate(),
                dto.getEndDate()
        );

        Department department =
                resolveDepartment(dto.getDeptId());

        validateDepartmentPermission(
                admin,
                department
        );

        Schedule schedule = new Schedule();

        schedule.setAdmin(admin);
        schedule.setDepartment(department);

        schedule.setTitle(
                dto.getTitle().trim()
        );

        schedule.setCategory(
                dto.getCategory()
        );

        schedule.setTargetGrade(
                emptyToNull(dto.getTargetGrade())
        );

        schedule.setStartDate(
                dto.getStartDate()
        );

        schedule.setEndDate(
                dto.getEndDate()
        );

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


    // ============================================================
    // 수정
    // ============================================================

    @Transactional
    public Schedule updateSchedule(
            Long id,
            ScheduleDTO dto,
            Admin admin
    ) {

        validateAdmin(admin);
        validateDto(dto);

        validateDate(
                dto.getStartDate(),
                dto.getEndDate()
        );

        Schedule schedule =
                getSchedule(id);

        /*
         * 현재 일정 접근 권한 확인
         */
        validateSchedulePermission(
                admin,
                schedule
        );

        Department department =
                resolveDepartment(dto.getDeptId());

        /*
         * 변경하려는 대상 학과 권한 확인
         */
        validateDepartmentPermission(
                admin,
                department
        );

        schedule.setDepartment(department);

        schedule.setTitle(
                dto.getTitle().trim()
        );

        schedule.setCategory(
                dto.getCategory()
        );

        schedule.setTargetGrade(
                emptyToNull(dto.getTargetGrade())
        );

        schedule.setStartDate(
                dto.getStartDate()
        );

        schedule.setEndDate(
                dto.getEndDate()
        );

        schedule.setVisible(
                dto.getVisible() == null
                        ? true
                        : dto.getVisible()
        );

        return scheduleRepository.save(schedule);
    }


    // ============================================================
    // 삭제
    // ============================================================

    @Transactional
    public void deleteSchedule(
            Long id,
            Admin admin
    ) {

        validateAdmin(admin);

        Schedule schedule =
                getSchedule(id);

        validateSchedulePermission(
                admin,
                schedule
        );

        /*
         * Schedule은 현재 다른 엔티티에서
         * 직접 참조하는 구조가 없으므로 물리 삭제
         */
        scheduleRepository.delete(schedule);
    }


    // ============================================================
    // 내부 검증
    // ============================================================

    private void validateAdmin(Admin admin) {

        if (admin == null) {
            throw new AccessDeniedException(
                    "로그인 정보가 없습니다."
            );
        }
    }


    /**
     * DTO 기본 입력값 검증
     */
    private void validateDto(
            ScheduleDTO dto
    ) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "일정 정보가 없습니다."
            );
        }

        if (dto.getTitle() == null ||
                dto.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "일정 제목은 필수입니다."
            );
        }

        if (dto.getCategory() == null ||
                dto.getCategory().isBlank()) {

            throw new IllegalArgumentException(
                    "일정 카테고리는 필수입니다."
            );
        }

        if (dto.getStartDate() == null) {

            throw new IllegalArgumentException(
                    "일정 시작일은 필수입니다."
            );
        }
    }


    /**
     * 날짜 범위 검증
     */
    private void validateDate(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate == null) {
            throw new IllegalArgumentException(
                    "일정 시작일은 필수입니다."
            );
        }

        if (endDate != null &&
                endDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    "일정 종료일은 시작일보다 빠를 수 없습니다."
            );
        }
    }


    /**
     * DTO deptId → Department 엔티티 변환
     *
     * deptId가 null이면 전체 학과 대상
     */
    private Department resolveDepartment(
            Long deptId
    ) {

        if (deptId == null) {
            return null;
        }

        return departmentRepository
                .findById(deptId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "존재하지 않는 학과입니다. deptId="
                                        + deptId
                        )
                );
    }


    /**
     * 관리자가 지정 가능한 학과인지 검사
     */
    private void validateDepartmentPermission(
            Admin admin,
            Department department
    ) {

        /*
         * SUPER_ADMIN은 모든 학과 가능
         */
        if (isSuperAdmin(admin)) {
            return;
        }

        if (isDeptAdmin(admin)) {

            if (admin.getDepartment() == null) {
                throw new IllegalStateException(
                        "학과 관리자의 소속 학과가 없습니다."
                );
            }

            /*
             * department == null
             * 전체 학과 대상 일정은 허용
             */
            if (department == null) {
                return;
            }

            Long adminDeptId =
                    admin.getDepartment().getDeptId();

            Long targetDeptId =
                    department.getDeptId();

            if (!adminDeptId.equals(targetDeptId)) {

                throw new AccessDeniedException(
                        "다른 학과의 일정을 등록할 수 없습니다."
                );
            }

            return;
        }

        throw new AccessDeniedException(
                "학사 일정 관리 권한이 없습니다."
        );
    }


    /**
     * 기존 일정 접근 권한 검사
     */
    private void validateSchedulePermission(
            Admin admin,
            Schedule schedule
    ) {

        /*
         * SUPER_ADMIN 모든 일정 접근 가능
         */
        if (isSuperAdmin(admin)) {
            return;
        }

        if (!isDeptAdmin(admin)) {

            throw new AccessDeniedException(
                    "학사 일정에 접근할 권한이 없습니다."
            );
        }

        if (admin.getDepartment() == null) {

            throw new IllegalStateException(
                    "관리자의 학과 정보가 없습니다."
            );
        }

        /*
         * 전체 학과 대상 일정
         */
        if (schedule.getDepartment() == null) {
            return;
        }

        Long adminDeptId =
                admin.getDepartment().getDeptId();

        Long scheduleDeptId =
                schedule.getDepartment().getDeptId();

        if (!adminDeptId.equals(scheduleDeptId)) {

            throw new AccessDeniedException(
                    "다른 학과의 일정에 접근할 수 없습니다."
            );
        }
    }


    private boolean isSuperAdmin(
            Admin admin
    ) {

        return "SUPER_ADMIN".equals(
                String.valueOf(admin.getRole())
        );
    }


    private boolean isDeptAdmin(
            Admin admin
    ) {

        return "DEPT_ADMIN".equals(
                String.valueOf(admin.getRole())
        );
    }


    /**
     * 빈 문자열을 null로 변경
     */
    private String emptyToNull(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            return null;
        }

        return value;
    }
}