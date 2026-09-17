package uc.dev.uc_info.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.common.util.TextNormalizer;
import uc.dev.uc_info.common.validation.AdminScopeValidator;
import uc.dev.uc_info.common.validation.DepartmentResolver;
import uc.dev.uc_info.dto.ScholarshipDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Department;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.model.Scholarship;
import uc.dev.uc_info.model.User;
import uc.dev.uc_info.repository.ScholarshipRepository;
import uc.dev.uc_info.repository.UserRepository;

import java.util.List;

/**
 * 장학금(Scholarship) 비즈니스 로직 서비스.
 *
 * <p>장학금 자체의 department를 기준으로 관리자 권한을 판단하며,
 * 학과 변환과 권한 검증은 공통 컴포넌트를 재사용한다.</p>
 */
@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final AdminScopeValidator adminScopeValidator;
    private final DepartmentResolver departmentResolver;
    private final NoticeService noticeService;
    private final UserRepository userRepository;

    /**
     * 관리자 권한 범위에 맞는 장학금 목록을 조회한다.
     *
     * @param admin 로그인 관리자
     * @return 권한 범위 내 장학금 목록
     */
    @Transactional(readOnly = true)
    public List<Scholarship> findScholarshipsFor(Admin admin) {
        if (adminScopeValidator.isSuperAdmin(admin)) {
            return scholarshipRepository.findAllByOrderByCreatedAtDesc();
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("DEPT_ADMIN 관리자에게 소속 학과가 없습니다.");
            }

            return scholarshipRepository.findByDepartmentOrAll(
                    admin.getDepartment().getDeptId()
            );
        }

        throw new AccessDeniedException("장학금 조회 권한이 없는 관리자입니다.");
    }

    /**
     * 장학금을 PK로 조회한다.
     *
     * @param id 장학금 PK
     * @return 조회된 장학금
     */
    @Transactional(readOnly = true)
    public Scholarship getScholarship(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("장학금 ID는 필수입니다.");
        }

        return scholarshipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("장학금 정보를 찾을 수 없습니다."));
    }

    /**
     * 관리자 권한 범위 내 전체 장학금 개수를 조회한다.
     *
     * @param admin 로그인 관리자
     * @return 권한 범위 내 장학금 개수
     */
    @Transactional(readOnly = true)
    public long countAll(Admin admin) {
        if (adminScopeValidator.isSuperAdmin(admin)) {
            return scholarshipRepository.count();
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("DEPT_ADMIN 관리자에게 소속 학과가 없습니다.");
            }

            return scholarshipRepository.countByDepartmentOrAll(
                    admin.getDepartment().getDeptId()
            );
        }

        throw new AccessDeniedException("장학금 통계를 조회할 권한이 없습니다.");
    }

    /**
     * 새 장학금을 등록한다.
     *
     * @param dto 장학금 등록 DTO
     * @param admin 등록 관리자
     * @return 저장된 장학금
     */
    @Transactional
    public Scholarship createScholarship(ScholarshipDTO dto, Admin admin) {
        Department department = departmentResolver.resolve(dto.getDeptId());
        adminScopeValidator.validateAssignable(admin, department);

        Notice notice = resolveNotice(dto.getNoticeId());

        Scholarship scholarship = new Scholarship();
        scholarship.setAdmin(admin);
        scholarship.setDepartment(department);
        scholarship.setNotice(notice);
        scholarship.setName(dto.getName());
        scholarship.setType(dto.getType());
        scholarship.setTargetGrade(TextNormalizer.emptyToNull(dto.getTargetGrade()));
        scholarship.setResidenceCondition(dto.getResidenceCondition());
        scholarship.setDeadline(dto.getDeadline());
        scholarship.setVisible(dto.getVisible() == null || dto.getVisible());

        return scholarshipRepository.save(scholarship);
    }

    /**
     * 기존 장학금을 수정한다.
     *
     * @param id 장학금 PK
     * @param dto 수정 DTO
     * @param admin 수정 관리자
     * @return 수정된 장학금
     */
    @Transactional
    public Scholarship updateScholarship(Long id, ScholarshipDTO dto, Admin admin) {
        Scholarship scholarship = getScholarship(id);
        adminScopeValidator.validateAccess(admin, scholarship);

        Department department = departmentResolver.resolve(dto.getDeptId());
        adminScopeValidator.validateAssignable(admin, department);

        Notice notice = resolveNotice(dto.getNoticeId());

        scholarship.setDepartment(department);
        scholarship.setNotice(notice);
        scholarship.setName(dto.getName());
        scholarship.setType(dto.getType());
        scholarship.setTargetGrade(TextNormalizer.emptyToNull(dto.getTargetGrade()));
        scholarship.setResidenceCondition(dto.getResidenceCondition());
        scholarship.setDeadline(dto.getDeadline());
        scholarship.setVisible(dto.getVisible() == null || dto.getVisible());

        return scholarshipRepository.save(scholarship);
    }

    /**
     * 장학금을 DB에서 삭제한다.
     *
     * @param id 장학금 PK
     * @param admin 삭제 관리자
     */
    @Transactional
    public void deleteScholarship(Long id, Admin admin) {
        Scholarship scholarship = getScholarship(id);
        adminScopeValidator.validateAccess(admin, scholarship);

        scholarshipRepository.delete(scholarship);
    }

    /**
     * 학생 앱에 노출할 장학금 목록을 조회한다. studentId로 학생을 찾아
     * 소속 학과를 알아낸 뒤, visible=true + 본인 학과/전체 대상 장학금만
     * 가져온다
     *
     * @param studentId 조회 요청 학생의 학번(토큰에서 추출)
     * @param type      좁힐 유형(REGIONAL/GRADE/INTERNAL/EXTERNAL, null/빈
     *                  문자열이면 전체 유형 유지)
     * @return 조건에 맞는 노출 대상 장학금 목록(마감일순)
     * @throws EntityNotFoundException studentId에 해당하는 학생이 없는 경우
     */
    @Transactional(readOnly = true)
    public List<Scholarship> findVisibleForStudent(String studentId, String type) {
        User user = getStudentOrThrow(studentId);
        Long deptId = user.getDepartment() != null ? user.getDepartment().getDeptId() : null;

        List<Scholarship> scholarships = scholarshipRepository.findVisibleForStudent(deptId);

        if (type == null || type.isBlank()) {
            return scholarships;
        }

        return scholarships.stream()
                .filter(s -> type.equals(s.getType()))
                .toList();
    }

    /**
     * 학생 앱의 장학금 상세 조회. 노출 안 함(visible=false) 상태이거나
     * 학생의 학과와 무관한(다른 학과 전용) 장학금이면 "없는 장학금"으로
     * 취급한다
     *
     * @param id        조회할 장학금 PK
     * @param studentId 조회 요청 학생의 학번(토큰에서 추출)
     * @return 조회된 장학금
     * @throws EntityNotFoundException 없는 id이거나, 비노출이거나, 다른 학과 전용이거나, 학생이 없는 경우
     */
    @Transactional(readOnly = true)
    public Scholarship getVisibleScholarshipForStudent(Long id, String studentId) {
        Scholarship scholarship = getScholarship(id);
        User user = getStudentOrThrow(studentId);
        Long deptId = user.getDepartment() != null ? user.getDepartment().getDeptId() : null;

        boolean isVisible = Boolean.TRUE.equals(scholarship.getVisible());
        boolean isVisibleToDept = scholarship.getDepartment() == null
                || scholarship.getDepartment().getDeptId().equals(deptId);

        if (!isVisible || !isVisibleToDept) {
            throw new EntityNotFoundException("장학금 정보를 찾을 수 없습니다.");
        }

        return scholarship;
    }

    /**
     * noticeId를 Notice 엔티티로 변환한다.
     *
     * @param noticeId 연결 공지 PK
     * @return 연결 공지 또는 null
     */
    private Notice resolveNotice(Long noticeId) {
        if (noticeId == null) {
            return null;
        }

        return noticeService.getNotice(noticeId);
    }

    /**
     * studentId로 학생을 조회한다.
     *
     * @param studentId 조회할 학번
     * @return 조회된 학생
     * @throws EntityNotFoundException 해당 학번의 학생이 없는 경우
     */
    private User getStudentOrThrow(String studentId) {
        return userRepository.findByStudentNumber(studentId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }
}