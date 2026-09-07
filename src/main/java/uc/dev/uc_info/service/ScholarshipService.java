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
import uc.dev.uc_info.repository.ScholarshipRepository;

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
}