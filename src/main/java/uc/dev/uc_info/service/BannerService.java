package uc.dev.uc_info.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.common.util.TextNormalizer;
import uc.dev.uc_info.common.validation.AdminScopeValidator;
import uc.dev.uc_info.dto.BannerDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Banner;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.repository.BannerRepository;

import java.util.List;

/**
 * 배너(Banner) 비즈니스 로직 서비스. Banner는 자체 학과 필드가 없어 연결된
 * Notice의 department를 기준으로 권한을 판단한다 — DEPT_ADMIN은 공지 연결이
 * 필수이고, SUPER_ADMIN은 선택이다. SUPER/DEPT/그 외 판단 자체는 전부
 * {@link AdminScopeValidator}에 위임하고, 여기서는 "공지 연결 필수 여부" 같은
 * Banner 고유 규칙만 앞단에서 처리한다. 통계(countAll/countByStatus)도
 * 목록과 마찬가지로 admin 권한 범위로 스코프한다.
 */
@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final NoticeService noticeService;
    private final AdminScopeValidator adminScopeValidator;

    /**
     * 권한별 배너 목록. SUPER_ADMIN은 전체, DEPT_ADMIN은 연결된 공지가
     * 본인 학과 또는 전체 대상인 배너만.
     *
     * @param admin 로그인 관리자
     * @return 권한 범위의 배너 목록(최신순, admin/notice/department fetch join 포함)
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우
     * @throws AccessDeniedException 조회 권한이 없는 role인 경우
     */
    @Transactional(readOnly = true)
    public List<Banner> findAllFor(Admin admin) {
        if (adminScopeValidator.isSuperAdmin(admin)) {
            return bannerRepository.findAllByOrderByCreatedAtDesc();
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("학과 관리자의 소속 학과 정보가 없습니다.");
            }
            return bannerRepository.findByDepartmentOrAll(admin.getDepartment().getDeptId());
        }
        throw new AccessDeniedException("배너 조회 권한이 없습니다.");
    }

    /**
     * id로 배너 단건 조회. 권한 체크 없이 존재 여부만 확인한다.
     *
     * @param id 배너 PK
     * @return 조회된 배너
     * @throws IllegalArgumentException id가 null인 경우
     * @throws EntityNotFoundException  없는 id인 경우
     */
    @Transactional(readOnly = true)
    public Banner getBanner(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("배너 ID는 필수입니다.");
        }
        return bannerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("배너 정보를 찾을 수 없습니다."));
    }

    /**
     * 권한 범위 내 전체 배너 개수. — 통계용. findAllFor와 항상 같은 범위로
     * 센다.
     *
     * @param admin 로그인 관리자(권한 범위 판단용)
     * @return 권한 범위 내 전체 배너 건수
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우
     * @throws AccessDeniedException 조회 권한이 없는 role인 경우
     */
    @Transactional(readOnly = true)
    public long countAll(Admin admin) {
        if (adminScopeValidator.isSuperAdmin(admin)) {
            return bannerRepository.count();
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("학과 관리자의 소속 학과 정보가 없습니다.");
            }
            return bannerRepository.countByDepartmentOrAll(admin.getDepartment().getDeptId());
        }
        throw new AccessDeniedException("배너 통계를 조회할 권한이 없습니다.");
    }

    /**
     * 권한 범위 내 상태별 배너 개수. — activeCount/scheduledCount 통계용.
     * 화면에 보이는 목록(findAllFor)과 항상 같은 범위로 세야 숫자가 안
     * 맞는 일이 없다.
     *
     * @param status 조회할 상태(ACTIVE/SCHEDULED/INACTIVE)
     * @param admin  로그인 관리자(권한 범위 판단용)
     * @return 권한 범위 내 해당 상태 배너 건수
     * @throws IllegalArgumentException status가 비어있는 경우
     * @throws IllegalStateException    DEPT_ADMIN인데 소속 학과가 없는 경우
     * @throws AccessDeniedException    조회 권한이 없는 role인 경우
     */
    @Transactional(readOnly = true)
    public long countByStatus(String status, Admin admin) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("배너 상태는 필수입니다.");
        }

        if (adminScopeValidator.isSuperAdmin(admin)) {
            return bannerRepository.countByStatus(status);
        }

        if (adminScopeValidator.isDeptAdmin(admin)) {
            if (admin.getDepartment() == null) {
                throw new IllegalStateException("학과 관리자의 소속 학과 정보가 없습니다.");
            }
            return bannerRepository.countByStatusAndDepartmentOrAll(status, admin.getDepartment().getDeptId());
        }
        throw new AccessDeniedException("배너 통계를 조회할 권한이 없습니다.");
    }

    /**
     * 배너에 연결 가능한 공지 목록. — 등록/수정 모달의 연결 공지 select용.
     * 권한 범위는 {@link #findAllFor(Admin)}와 동일(DEPT_ADMIN은 본인 학과
     * +전체, SUPER_ADMIN은 전체) — 애초에 등록 못 할 공지는 드롭다운에도 안
     * 보이게 한다. 상태도 PUBLISHED만 남긴다(NoticeService 참고).
     *
     * @param admin 로그인 관리자(권한 판단용)
     * @return 권한 범위 내 게시중 공지 목록(최신순)
     */
    @Transactional(readOnly = true)
    public List<Notice> findLinkableNotices(Admin admin) {
        return noticeService.findAllForLink(admin);
    }

    /**
     * 새 배너를 등록한다. DEPT_ADMIN은 공지 연결이 필수이고, 연결한 공지는
     * 본인 학과 또는 전체 대상이어야 한다. SUPER_ADMIN은 공지 연결이 선택이다.
     *
     * @param dto   등록 DTO(Controller에서 @Valid 검증 통과)
     * @param admin 등록자(로그인 관리자)
     * @return 저장된 배너
     * @throws IllegalArgumentException 노출 종료일이 시작일보다 빠르거나,
     *                                  DEPT_ADMIN이 공지 연결 없이 등록하는 경우
     * @throws EntityNotFoundException  noticeId에 해당하는 공지가 없는 경우
     * @throws AccessDeniedException    다른 학과 공지를 연결하려는 경우
     */
    @Transactional
    public Banner createBanner(BannerDTO dto, Admin admin) {
        validateDate(dto);

        Notice notice = resolveNotice(dto.getNoticeId());
        validateNoticeForAssignment(admin, notice);

        Banner banner = new Banner();
        banner.setAdmin(admin);
        banner.setNotice(notice);
        banner.setTitle(dto.getTitle().trim());
        banner.setSubtitle(TextNormalizer.emptyToNull(dto.getSubtitle()));
        banner.setStatus(normalizeStatus(dto.getStatus()));
        banner.setStartDate(dto.getStartDate());
        banner.setEndDate(dto.getEndDate());

        return bannerRepository.save(banner);
    }

    /**
     * 기존 배너를 수정한다. 기존 배너 접근 권한과, 새로 연결하려는 공지에
     * 대한 권한을 각각 따로 검사한다 — DEPT_ADMIN이 본인 접근 가능한
     * 배너를 다른 학과 공지로 재연결하려는 시도를 막기 위함이다.
     *
     * @param id    수정할 배너 PK
     * @param dto   수정 DTO(Controller에서 @Valid 검증 통과)
     * @param admin 수정 요청 관리자(권한 판단)
     * @return 수정된 배너
     * @throws EntityNotFoundException 없는 배너/공지 id인 경우
     * @throws AccessDeniedException   기존 배너 접근 권한이 없거나, 다른 학과 공지로 재연결하려는 경우
     */
    @Transactional
    public Banner updateBanner(Long id, BannerDTO dto, Admin admin) {
        validateDate(dto);

        Banner banner = getBanner(id);
        validateBannerAccess(admin, banner);

        Notice notice = resolveNotice(dto.getNoticeId());
        validateNoticeForAssignment(admin, notice);

        banner.setNotice(notice);
        banner.setTitle(dto.getTitle().trim());
        banner.setSubtitle(TextNormalizer.emptyToNull(dto.getSubtitle()));
        banner.setStatus(normalizeStatus(dto.getStatus()));
        banner.setStartDate(dto.getStartDate());
        banner.setEndDate(dto.getEndDate());

        return bannerRepository.save(banner);
    }

    /**
     * 배너를 삭제한다(물리 삭제).
     *
     * @param id    삭제할 배너 PK
     * @param admin 삭제 요청 관리자(권한 판단)
     * @throws EntityNotFoundException 없는 배너 id인 경우
     * @throws AccessDeniedException   접근 권한이 없는 경우
     */
    @Transactional
    public void deleteBanner(Long id, Admin admin) {
        Banner banner = getBanner(id);
        validateBannerAccess(admin, banner);
        bannerRepository.delete(banner);
    }

    /**
     * noticeId를 Notice 엔티티로 변환한다. null이면 "연결 안 함"이라 그대로
     * null을 반환한다.
     *
     * @param noticeId 변환할 공지 PK(연결 안 하면 null)
     * @return 조회된 Notice, 또는 null
     * @throws EntityNotFoundException noticeId에 해당하는 공지가 없는 경우
     */
    private Notice resolveNotice(Long noticeId) {
        if (noticeId == null) {
            return null;
        }
        return noticeService.getNotice(noticeId);
    }

    /**
     * 새로 연결(또는 재연결)하려는 공지에 대한 권한을 검사한다. Banner
     * 고유 규칙(DEPT_ADMIN은 공지 연결 필수)만 여기서 직접 판단하고,
     * SUPER/DEPT/그 외 판단 자체는 {@link AdminScopeValidator#validateAssignable}에
     * 위임한다 — 이 판단 로직을 두 곳에서 반복하지 않기 위함이다.
     *
     * @param admin  권한을 확인할 관리자
     * @param notice 새로 연결하려는 공지(전체 홍보 배너면 null)
     * @throws IllegalArgumentException DEPT_ADMIN인데 notice가 null인 경우
     * @throws AccessDeniedException    다른 학과 공지를 연결하려는 경우, 권한 자체가 없는 경우
     */
    private void validateNoticeForAssignment(Admin admin, Notice notice) {
        if (adminScopeValidator.isDeptAdmin(admin) && notice == null) {
            throw new IllegalArgumentException("학과 관리자는 연결 공지를 선택해야 합니다.");
        }
        adminScopeValidator.validateAssignable(admin, notice != null ? notice.getDepartment() : null);
    }

    /**
     * 기존 배너에 대한 접근 권한을 검사한다. Banner 고유 규칙(공지 없는
     * 배너는 DEPT_ADMIN 접근 불가)만 여기서 직접 판단하고, 나머지는
     * {@link AdminScopeValidator#validateAccess}에 위임한다.
     *
     * @param admin  권한을 확인할 관리자
     * @param banner 접근하려는 기존 배너
     * @throws AccessDeniedException 공지 없는 배너에 DEPT_ADMIN이 접근하거나, 다른 학과 배너에 접근하는 경우
     */
    private void validateBannerAccess(Admin admin, Banner banner) {
        if (adminScopeValidator.isDeptAdmin(admin) && banner.getNotice() == null) {
            throw new AccessDeniedException("공지와 연결되지 않은 배너에는 접근할 수 없습니다.");
        }
        adminScopeValidator.validateAccess(admin, banner.getNotice());
    }

    /**
     * 노출 종료일이 시작일보다 빠르면 안 된다(둘 다 있을 때만 비교).
     *
     * @param dto 검사할 DTO
     * @throws IllegalArgumentException endDate가 startDate보다 빠른 경우
     */
    private void validateDate(BannerDTO dto) {
        if (dto.getStartDate() != null
                && dto.getEndDate() != null
                && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("배너 종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    /**
     * status 값이 유효한지 확인한다. Notice와 달리 잘못된 값을 조용히
     * 기본값으로 바꾸지 않고 명시적으로 거부한다 — status가 select로만
     * 들어오는 값이라 무결성을 더 엄격히 지키는 편을 택했다.
     *
     * @param status 원본 값
     * @return 유효하면 그대로 반환
     * @throws IllegalArgumentException ACTIVE/SCHEDULED/INACTIVE가 아닌 경우
     */
    private String normalizeStatus(String status) {
        if ("ACTIVE".equals(status) || "SCHEDULED".equals(status) || "INACTIVE".equals(status)) {
            return status;
        }
        throw new IllegalArgumentException("올바르지 않은 배너 상태입니다.");
    }
}