package uc.dev.uc_info.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.common.util.HtmlSanitizer;
import uc.dev.uc_info.common.util.TextNormalizer;
import uc.dev.uc_info.common.validation.AdminScopeValidator;
import uc.dev.uc_info.common.validation.DepartmentResolver;
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
 * 삭제({@link #deleteNotice})는 서로 다른 별개 기능이다. 권한 판단/학과
 * 변환은 Schedule/Banner와 공유하는 {@link AdminScopeValidator}/ {@link DepartmentResolver}에
 * 위임한다. content는 저장 전 {@link HtmlSanitizer}로 정제해 저장형 XSS를 막는다.
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final DepartmentRepository departmentRepository;
    private final AdminScopeValidator adminScopeValidator;
    private final DepartmentResolver departmentResolver;

    /**
     * 권한별 공지 목록. SUPER_ADMIN은 전체, DEPT_ADMIN은 본인 학과+전체 대상.
     *
     * @param admin 로그인 관리자
     * @return 권한 범위의 공지 목록(최신순, admin fetch join 포함)
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우
     * @throws AccessDeniedException 조회 권한이 없는 role인 경우
     */
    @Transactional(readOnly = true)
    public List<Notice> findNoticesFor(Admin admin) {

        if (adminScopeValidator.isSuperAdmin(admin)) {
            return noticeRepository.findAllByOrderByCreatedAtDesc();
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
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
        Notice notice = getNotice(id);
        adminScopeValidator.validateAccess(admin, notice);
        return notice;
    }

    /**
     * 권한 범위 내 상태별 공지 개수. — 목록 상단 통계용. 화면에 보이는
     * 목록(findNoticesFor)과 항상 같은 범위로 세야 숫자가 안 맞는 일이
     * 없다.
     *
     * @param status 조회할 상태(PUBLISHED/DRAFT/CLOSED)
     * @param admin  로그인 관리자(권한 범위 판단용)
     * @return 권한 범위 내 해당 상태 공지 건수
     * @throws IllegalArgumentException status가 비어있는 경우
     * @throws IllegalStateException    DEPT_ADMIN인데 소속 학과가 없는 경우
     * @throws AccessDeniedException    조회 권한이 없는 role인 경우
     */
    @Transactional(readOnly = true)
    public long countByStatus(String status, Admin admin) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("공지 상태는 필수입니다.");
        }

        if (adminScopeValidator.isSuperAdmin(admin)) {
            return noticeRepository.countByStatus(status);
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("DEPT_ADMIN 관리자에게 소속 학과가 없습니다.");
            }
            return noticeRepository.countByStatusAndDepartmentOrAll(status, admin.getDepartment().getDeptId());
        }
        throw new AccessDeniedException("공지 통계를 조회할 권한이 없습니다.");
    }

    /**
     * 권한 범위 내 전체 공지 개수. — 목록 상단 totalCount. findNoticesFor와
     * 항상 같은 범위로 센다.
     *
     * @param admin 로그인 관리자(권한 범위 판단용)
     * @return 권한 범위 내 전체 건수
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우
     * @throws AccessDeniedException 조회 권한이 없는 role인 경우
     */
    @Transactional(readOnly = true)
    public long countAll(Admin admin) {
        if (adminScopeValidator.isSuperAdmin(admin)) {
            return noticeRepository.count();
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("DEPT_ADMIN 관리자에게 소속 학과가 없습니다.");
            }
            return noticeRepository.countByDepartmentOrAll(admin.getDepartment().getDeptId());
        }
        throw new AccessDeniedException("공지 통계를 조회할 권한이 없습니다.");
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
     * content는 {@link HtmlSanitizer}로 정제 후 저장한다.
     *
     * @param dto   작성 DTO(Controller에서 @Valid 검증 통과)
     * @param admin 작성자(로그인 관리자)
     * @return 저장된 공지
     */
    @Transactional
    public Notice createNotice(NoticeDTO dto, Admin admin) {
        validateDate(dto);

        Department department = departmentResolver.resolve(dto.getDeptId());
        adminScopeValidator.validateAssignable(admin, department);

        String status = "PUBLISHED".equals(dto.getStatus()) ? "PUBLISHED" : "DRAFT";

        Notice notice = new Notice();

        notice.setAdmin(admin);
        notice.setDepartment(department);
        notice.setTitle(dto.getTitle());
        notice.setContent(HtmlSanitizer.sanitize(dto.getContent()));
        notice.setCategory(dto.getCategory());
        notice.setPriority(dto.getPriority());
        notice.setTargetGrade(TextNormalizer.emptyToNull(dto.getTargetGrade()));
        notice.setStatus(status);
        notice.setStartDate(dto.getStartDate());
        notice.setEndDate(dto.getEndDate());

        return noticeRepository.save(notice);
    }

    /**
     * 공지사항 수정. status 하나로 PUBLISHED/DRAFT/CLOSED(게시종료) 전부
     * 처리한다 — 셋 중 하나가 아니면 DRAFT로 취급. content는 저장 전
     * {@link HtmlSanitizer}로 다시 정제한다.
     *
     * @param id    수정할 공지 PK
     * @param dto   수정 DTO(Controller에서 @Valid 검증 통과)
     * @param admin 수정 요청 관리자(권한 판단)
     * @return 수정된 공지
     */
    @Transactional
    public Notice updateNotice(Long id, NoticeDTO dto, Admin admin) {
        validateDate(dto);

        Notice notice = getNotice(id);
        adminScopeValidator.validateAccess(admin, notice);

        Department department = departmentResolver.resolve(dto.getDeptId());
        adminScopeValidator.validateAssignable(admin, department);

        notice.setStatus(normalizeStatus(dto.getStatus()));
        notice.setDepartment(department);
        notice.setTitle(dto.getTitle());
        notice.setContent(HtmlSanitizer.sanitize(dto.getContent()));
        notice.setCategory(dto.getCategory());
        notice.setPriority(dto.getPriority());
        notice.setTargetGrade(TextNormalizer.emptyToNull(dto.getTargetGrade()));
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
    public void deleteNotice(Long id, Admin admin) {
        Notice notice = getNotice(id);
        adminScopeValidator.validateAccess(admin, notice);
        noticeRepository.delete(notice);
    }

    /**
     * 배너 등록/수정 시 연결 가능한 공지 목록을 조회한다. 학과 권한 범위는
     * {@link #findNoticesFor(Admin)}와 동일하고, 추가로 <b>PUBLISHED
     * 상태만</b> 남긴다 — 아직 게시 안 된(DRAFT) 공지를 배너로 미리 연결해
     * 두면 클릭했을 때 실제로는 노출도 안 된 내용으로 연결되는 문제가
     * 있어서다.
     *
     * @param admin 로그인 관리자(권한 판단용)
     * @return 권한 범위 내 게시중(PUBLISHED) 공지 목록(최신순)
     */
    @Transactional(readOnly = true)
    public List<Notice> findAllForLink(Admin admin) {
        return findNoticesFor(admin).stream()
                .filter(n -> "PUBLISHED".equals(n.getStatus()))
                .toList();
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
                && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("게시 종료일은 시작일보다 빠를 수 없습니다.");
        }
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