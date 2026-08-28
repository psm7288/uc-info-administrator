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

/**
 * 공지(Notice) 비즈니스 로직 서비스. Controller는 이 서비스만 호출한다.
 * 상태는 PUBLISHED/DRAFT/CLOSED 3개만 쓰고, CLOSED(게시종료)와 실제 DB
 * 삭제({@link #deleteNotice})는 서로 다른 별개 기능이다.
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * 권한별 공지 목록. SUPER_ADMIN은 전체, DEPT_ADMIN은 본인 학과+전체 대상.
     *
     * @param admin 로그인 관리자
     * @return 권한 범위의 공지 목록(최신순)
     */
    @Transactional(readOnly = true)
    public List<Notice> findNoticesFor(Admin admin) {
        validateAdmin(admin);

        if ("SUPER_ADMIN".equals(admin.getRole())) {
            return noticeRepository.findAllByOrderByCreatedAtDesc();
        }

        if ("DEPT_ADMIN".equals(admin.getRole())) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("DEPT_ADMIN 관리자에게 소속 학과가 없습니다.");
            }

            return noticeRepository.findByDepartmentOrAll(admin.getDepartment().getDeptId());
        }
        throw new AccessDeniedException("공지 조회 권한이 없는 관리자입니다.");
    }

    /**
     * id로 공지 단건 조회. 권한 체크 없이 존재 여부만 확인한다.
     *
     * @param id 공지 PK
     * @return 조회된 공지
     * @throws EntityNotFoundException 없는 id인 경우
     */
    @Transactional(readOnly = true)
    public Notice getNotice(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("공지 ID는 필수입니다.");
        }
        return noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지 정보를 찾을 수 없습니다."));
    }

    /**
     * 접근 권한까지 확인한 공지 단건 조회. — 수정 폼 진입 시 사용.
     *
     * @param id    공지 PK
     * @param admin 로그인 관리자(권한 판단)
     * @return 접근 가능이 확인된 공지
     */
    @Transactional(readOnly = true)
    public Notice getNoticeForAdmin(Long id, Admin admin) {
        validateAdmin(admin);

        Notice notice = getNotice(id);
        validateNoticeAccess(notice, admin);

        return notice;
    }

    /**
     * 상태별 공지 개수. — 목록/대시보드 통계용.
     *
     * @param status 조회할 상태(PUBLISHED/DRAFT/CLOSED)
     * @return 해당 상태 공지 건수
     */
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("공지 상태는 필수입니다.");
        }
        return noticeRepository.countByStatus(status);
    }

    /**
     * 전체 공지 개수. — 목록 상단 totalCount.
     *
     * @return 전체 건수
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return noticeRepository.count();
    }

    /**
     * 학과 전체 목록. — 작성/수정 폼의 대상 학과 select용.
     *
     * @return 전체 학과 목록
     */
    @Transactional(readOnly = true)
    public List<Department> findAllDepartments() {
        return departmentRepository.findAll();
    }

    /**
     * 새 공지를 작성한다. status가 "PUBLISHED"가 아니면 DRAFT로 저장한다.
     *
     * @param dto   작성 DTO(Controller에서 @Valid 검증 통과)
     * @param admin 작성자(로그인 관리자)
     * @return 저장된 공지
     */
    @Transactional
    public Notice createNotice(NoticeDTO dto,Admin admin) {
        validateAdmin(admin);
        validateDto(dto);
        validateDate(dto);

        Department department = resolveDepartment(dto.getDeptId());
        validateDepartmentAccess(department,admin);

        String status = "PUBLISHED".equals(dto.getStatus())
                ? "PUBLISHED"
                : "DRAFT";

        Notice notice = new Notice();

        notice.setAdmin(admin);
        notice.setDepartment(department);
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setCategory(dto.getCategory());
        notice.setPriority(dto.getPriority());
        notice.setTargetGrade(normalizeTargetGrade(dto.getTargetGrade()));
        notice.setStatus(status);
        notice.setStartDate(dto.getStartDate());
        notice.setEndDate(dto.getEndDate());

        return noticeRepository.save(notice);
    }

    /**
     * 공지사항 수정. status 하나로 PUBLISHED/DRAFT/CLOSED(게시종료) 전부
     * 처리한다 — 셋 중 하나가 아니면 DRAFT로 취급.
     *
     * @param id    수정할 공지 PK
     * @param dto   수정 DTO(Controller에서 @Valid 검증 통과)
     * @param admin 수정 요청 관리자(권한 판단)
     * @return 수정된 공지
     */
    @Transactional
    public Notice updateNotice(Long id, NoticeDTO dto, Admin admin) {
        validateAdmin(admin);
        validateDto(dto);
        validateDate(dto);

        Notice notice = getNotice(id);
        validateNoticeAccess(notice, admin);

        Department department = resolveDepartment(dto.getDeptId());
        validateDepartmentAccess(department, admin);

        notice.setDepartment(department);
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setCategory(dto.getCategory());
        notice.setPriority(dto.getPriority());
        notice.setStatus(normalizeStatus(dto.getStatus()));
        notice.setTargetGrade(normalizeTargetGrade(dto.getTargetGrade()));
        notice.setStartDate(dto.getStartDate());
        notice.setEndDate(dto.getEndDate());

        return noticeRepository.save(notice);
    }

    /**
     * 공지를 DB에서 실제로 삭제한다(되돌릴 수 없음). status를 CLOSED로
     * 바꾸는 것과는 완전히 별개 기능이다.
     *
     * @param id    삭제할 공지 PK
     * @param admin 삭제 요청 관리자(권한 판단)
     */
    @Transactional
    public void deleteNotice(Long id, Admin admin){
        validateAdmin(admin);

        Notice notice = getNotice(id);
        validateNoticeAccess(notice, admin);

        noticeRepository.delete(notice);
    }

    /**
     * 로그인 관리자와 role 존재 여부를 확인한다.
     *
     * @param admin 확인할 관리자
     * @throws AccessDeniedException admin 또는 role이 없는 경우
     */
    private void validateAdmin(Admin admin) {
        if (admin == null) {
            throw new AccessDeniedException("관리자 인증 정보가 없습니다.");
        }

        if (admin.getRole() == null) {
            throw new AccessDeniedException("관리자 권한 정보가 없습니다.");
        }
    }

    /**
     * DTO 기본 입력값 검증(title/content 공백 여부).
     *
     * @param dto 검증할 DTO
     * @throws IllegalArgumentException dto가 null이거나 title/content가 빈 경우
     */
    private void validateDto(NoticeDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("공지 입력 정보가 없습니다.");
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("공지 제목은 필수입니다.");
        }

        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new IllegalArgumentException("공지 내용은 필수입니다.");
        }
    }

    /**
     * 게시 종료일이 시작일보다 빠르면 안 된다(둘 다 있을 때만 비교).
     *
     * @param dto 검사할 DTO
     * @throws IllegalArgumentException endDate가 startDate보다 빠른 경우
     */
    private void validateDate(NoticeDTO dto) {
        if (dto.getStartDate() != null
                && dto.getEndDate() != null
                && dto.getEndDate()
                .isBefore(dto.getStartDate())) {

            throw new IllegalArgumentException("게시 종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    /**
     * deptId → Department 변환. null이면 전체 대상이라 그대로 null 반환.
     *
     * @param deptId 변환할 학과 PK
     * @return 조회된 Department, 또는 null
     * @throws EntityNotFoundException deptId에 해당하는 학과가 없는 경우
     */
    private Department resolveDepartment(Long deptId) {
        if (deptId == null) {
            return null;
        }
        return departmentRepository
                .findById(deptId)
                .orElseThrow(() ->  new EntityNotFoundException("학과 정보를 찾을 수 없습니다."));
    }

    /**
     * 관리자가 이 대상 학과를 지정할 권한이 있는지 검사한다. SUPER_ADMIN은
     * 항상 통과, DEPT_ADMIN은 본인 학과 또는 전체(null)만 지정 가능하다.
     *
     * @param department 지정하려는 대상 학과(전체면 null)
     * @param admin      권한을 확인할 관리자
     */
    private void validateDepartmentAccess(Department department, Admin admin) {
        if ("SUPER_ADMIN".equals(admin.getRole())) {
            return;
        }

        if (!"DEPT_ADMIN".equals(admin.getRole())) {
            throw new AccessDeniedException("공지 관리 권한이 없습니다.");
        }

        if (admin.getDepartment() == null) {
            throw new IllegalStateException("DEPT_ADMIN 관리자에게 소속 학과가 없습니다.");
        }

        if (department == null) {
            return;
        }
        Long adminDeptId = admin.getDepartment().getDeptId();
        Long targetDeptId = department.getDeptId();

        if (!adminDeptId.equals(targetDeptId)) {
            throw new AccessDeniedException("다른 학과를 대상으로 공지를 작성하거나 수정할 수 없습니다.");
        }
    }

    /**
     * 기존 공지에 대한 접근 권한을 검사한다. SUPER_ADMIN은 항상 통과,
     * DEPT_ADMIN은 본인 학과 또는 전체 대상 공지만 접근 가능하다.
     *
     * @param notice 접근하려는 기존 공지
     * @param admin  권한을 확인할 관리자
     */
    private void validateNoticeAccess(Notice notice, Admin admin) {
        if ("SUPER_ADMIN".equals(admin.getRole())) {
            return;
        }

        if (!"DEPT_ADMIN".equals(admin.getRole())) {
            throw new AccessDeniedException("공지 접근 권한이 없습니다.");
        }

        if (admin.getDepartment() == null) {
            throw new IllegalStateException("DEPT_ADMIN 관리자에게 소속 학과가 없습니다.");
        }
        Department noticeDepartment = notice.getDepartment();

        if (noticeDepartment == null) {
            return;
        }

        Long adminDeptId = admin.getDepartment().getDeptId();
        Long noticeDeptId = noticeDepartment.getDeptId();
        if (!adminDeptId.equals(noticeDeptId)) {
            throw new AccessDeniedException("다른 학과의 공지에는 접근할 수 없습니다.");
        }
    }

    /**
     * 빈 문자열을 null로 정규화(전체 학년 의미).
     *
     * @param targetGrade 원본 값
     * @return 비어있으면 null, 아니면 그대로
     */
    private String normalizeTargetGrade(String targetGrade) {
        if (targetGrade == null || targetGrade.isBlank()) {
            return null;
        }
        return targetGrade;
    }

    /**
     * status 값을 PUBLISHED/DRAFT/CLOSED 중 하나로 정규화한다.
     *
     * @param status 원본 값
     * @return PUBLISHED 또는 CLOSED면 그대로, 그 외(null 포함)는 DRAFT
     */
    private String normalizeStatus(String status) {
        if ("PUBLISHED".equals(status) || "CLOSED".equals(status)) {
            return status;
        }
        return "DRAFT";
    }
}