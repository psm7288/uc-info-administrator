package uc.dev.uc_info.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.dto.NoticeDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Department;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.repository.DepartmentRepository;
import uc.dev.uc_info.repository.NoticeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final DepartmentRepository departmentRepository;

    // ============================================================
    // 조회
    // ============================================================

    @Transactional(readOnly = true)
    public List<Notice> findNoticesFor(Admin admin) {
        validateAdmin(admin);

        if ("SUPER_ADMIN".equals(admin.getRole())) {
            return noticeRepository.findAllByOrderByCreatedAtDesc();
        }

        if ("DEPT_ADMIN".equals(admin.getRole())) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException(
                        "DEPT_ADMIN 관리자에게 소속 학과가 없습니다."
                );
            }

            return noticeRepository.findByDepartmentOrAll(
                    admin.getDepartment().getDeptId()
            );
        }

        throw new AccessDeniedException(
                "공지 조회 권한이 없는 관리자입니다."
        );
    }

    @Transactional(readOnly = true)
    public Notice getNotice(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "공지 ID는 필수입니다."
            );
        }

        return noticeRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "공지 정보를 찾을 수 없습니다. id=" + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public Notice getNoticeForAdmin(
            Long id,
            Admin admin
    ) {
        validateAdmin(admin);

        Notice notice = getNotice(id);

        validateNoticeAccess(
                notice,
                admin
        );

        return notice;
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "공지 상태는 필수입니다."
            );
        }

        return noticeRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return noticeRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Department> findAllDepartments() {
        return departmentRepository.findAll();
    }

    // ============================================================
    // 작성
    // ============================================================

    @Transactional
    public Notice createNotice(
            NoticeDTO dto,
            Admin admin
    ) {
        validateAdmin(admin);
        validateDto(dto);
        validateDate(dto);

        Department department =
                resolveDepartment(dto.getDeptId());

        validateDepartmentAccess(
                department,
                admin
        );

        String status =
                "PUBLISHED".equals(dto.getStatus())
                        ? "PUBLISHED"
                        : "DRAFT";

        Notice notice = new Notice();

        notice.create(
                admin,
                department,
                dto.getTitle(),
                dto.getContent(),
                dto.getCategory(),
                dto.getPriority(),
                normalizeTargetGrade(
                        dto.getTargetGrade()
                ),
                status,
                dto.getStartDate(),
                dto.getEndDate()
        );

        return noticeRepository.save(notice);
    }

    // ============================================================
    // 수정
    // ============================================================

    @Transactional
    public Notice updateNotice(
            Long id,
            NoticeDTO dto,
            Admin admin
    ) {
        validateAdmin(admin);
        validateDto(dto);
        validateDate(dto);

        Notice notice = getNotice(id);

        validateNoticeAccess(
                notice,
                admin
        );

        Department department =
                resolveDepartment(dto.getDeptId());

        validateDepartmentAccess(
                department,
                admin
        );

        notice.update(
                department,
                dto.getTitle(),
                dto.getContent(),
                dto.getCategory(),
                dto.getPriority(),
                normalizeTargetGrade(
                        dto.getTargetGrade()
                ),
                dto.getStartDate(),
                dto.getEndDate()
        );

        String saveType =
                dto.getSaveType();

        if ("PUBLISHED".equals(saveType)) {
            notice.changeStatus("PUBLISHED");
        } else {
            notice.changeStatus("DRAFT");
        }

        return noticeRepository.save(notice);
    }

    // ============================================================
    // 승인
    // ============================================================

    @Transactional
    public void approveNotice(
            Long id,
            Admin admin
    ) {
        validateAdmin(admin);

        Notice notice = getNotice(id);

        validateNoticeAccess(
                notice,
                admin
        );

        if (!"WAITING".equals(notice.getStatus())) {
            throw new IllegalStateException(
                    "WAITING 상태의 공지만 승인할 수 있습니다."
            );
        }

        notice.changeStatus("PUBLISHED");
    }

    // ============================================================
    // 삭제
    // ============================================================

    @Transactional
    public void deleteNotice(
            Long id,
            Admin admin
    ) {
        validateAdmin(admin);

        Notice notice = getNotice(id);

        validateNoticeAccess(
                notice,
                admin
        );

        notice.changeStatus("CLOSED");
    }

    // ============================================================
    // 내부 검증
    // ============================================================

    private void validateAdmin(Admin admin) {
        if (admin == null) {
            throw new AccessDeniedException(
                    "관리자 인증 정보가 없습니다."
            );
        }

        if (admin.getRole() == null) {
            throw new AccessDeniedException(
                    "관리자 권한 정보가 없습니다."
            );
        }
    }

    private void validateDto(NoticeDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "공지 입력 정보가 없습니다."
            );
        }

        if (dto.getTitle() == null
                || dto.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "공지 제목은 필수입니다."
            );
        }

        if (dto.getContent() == null
                || dto.getContent().isBlank()) {

            throw new IllegalArgumentException(
                    "공지 내용은 필수입니다."
            );
        }
    }

    private void validateDate(NoticeDTO dto) {
        if (dto.getStartDate() != null
                && dto.getEndDate() != null
                && dto.getEndDate()
                .isBefore(dto.getStartDate())) {

            throw new IllegalArgumentException(
                    "게시 종료일은 시작일보다 빠를 수 없습니다."
            );
        }
    }

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
                                "학과 정보를 찾을 수 없습니다. deptId="
                                        + deptId
                        )
                );
    }

    private void validateDepartmentAccess(
            Department department,
            Admin admin
    ) {
        if ("SUPER_ADMIN".equals(admin.getRole())) {
            return;
        }

        if (!"DEPT_ADMIN".equals(admin.getRole())) {
            throw new AccessDeniedException(
                    "공지 관리 권한이 없습니다."
            );
        }

        if (admin.getDepartment() == null) {
            throw new IllegalStateException(
                    "DEPT_ADMIN 관리자에게 소속 학과가 없습니다."
            );
        }

        // department == null이면 전체 대상이므로 허용
        if (department == null) {
            return;
        }

        Long adminDeptId =
                admin.getDepartment().getDeptId();

        Long targetDeptId =
                department.getDeptId();

        if (!adminDeptId.equals(targetDeptId)) {
            throw new AccessDeniedException(
                    "다른 학과를 대상으로 공지를 작성하거나 수정할 수 없습니다."
            );
        }
    }

    private void validateNoticeAccess(
            Notice notice,
            Admin admin
    ) {
        if ("SUPER_ADMIN".equals(admin.getRole())) {
            return;
        }

        if (!"DEPT_ADMIN".equals(admin.getRole())) {
            throw new AccessDeniedException(
                    "공지 접근 권한이 없습니다."
            );
        }

        if (admin.getDepartment() == null) {
            throw new IllegalStateException(
                    "DEPT_ADMIN 관리자에게 소속 학과가 없습니다."
            );
        }

        Department noticeDepartment =
                notice.getDepartment();

        // 전체 대상 공지
        if (noticeDepartment == null) {
            return;
        }

        Long adminDeptId =
                admin.getDepartment().getDeptId();

        Long noticeDeptId =
                noticeDepartment.getDeptId();

        if (!adminDeptId.equals(noticeDeptId)) {
            throw new AccessDeniedException(
                    "다른 학과의 공지에는 접근할 수 없습니다."
            );
        }
    }

    private String normalizeTargetGrade(
            String targetGrade
    ) {
        if (targetGrade == null
                || targetGrade.isBlank()) {
            return null;
        }

        return targetGrade;
    }
}