package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.common.util.ValidationMessages;
import uc.dev.uc_info.dto.NoticeDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.NoticeService;

import java.util.List;

/**
 * 공지 관리 컨트롤러. Repository를 직접 호출하지 않고 NoticeService만 쓴다.
 * status는 PUBLISHED/DRAFT/CLOSED 3개만 쓰며, 승인(WAITING) 기능은 폐지됐다.
 * 목록 화면은 엔티티를 직접 노출하지 않고 {@link NoticeDTO}(목록 표시 겸용
 * 필드 포함)로 변환해서 넘긴다 — admin은 지연로딩이라 뷰에서 바로 쓰면 안 된다.
 */
@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지 목록 화면. 권한별 목록 + 전체/게시중 건수를 담는다. 건수도
     * 목록과 같은 권한 범위로 세서(admin 전달) 화면 상단 숫자와 아래 목록이
     * 항상 일치한다.
     *
     * @param principal 로그인 관리자 정보
     * @param model     화면 전달용 모델
     * @return "notice/notice"
     */
    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserPrincipal principal,
                       Model model) {
        Admin admin = principal.getAdmin();

        List<NoticeDTO> notices = noticeService.findNoticesFor(admin)
                .stream()
                .map(this::toListItem)
                .toList();

        model.addAttribute("notices", notices);
        model.addAttribute("totalCount", noticeService.countAll(admin));
        model.addAttribute("publishedCount", noticeService.countByStatus("PUBLISHED", admin));

        return "notice/notice";
    }

    /**
     * 새 공지 작성 화면. 빈 DTO와 학과 목록을 담는다.
     *
     * @param model 화면 전달용 모델
     * @return "notice/notice-write"
     */
    @GetMapping("/new")
    public String writeForm(Model model) {
        model.addAttribute("noticeDTO", new NoticeDTO());
        model.addAttribute("departments", noticeService.findAllDepartments());

        return "notice/notice-write";
    }

    /**
     * 새 공지 작성 처리. 검증 실패 시 작성 폼으로 되돌리며 formError에
     * 첫 번째 검증 메시지를 담고, 성공 시 목록으로 리다이렉트한다(PRG).
     *
     * @param dto       작성 DTO(@Valid 검증 대상)
     * @param result    검증 결과
     * @param principal 로그인 관리자 정보
     * @param model     검증 실패 시 재구성용 모델
     * @return 성공 시 "redirect:/notices", 실패 시 "notice/notice-write"
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("noticeDTO") NoticeDTO dto,
                         BindingResult result,
                         @AuthenticationPrincipal CustomUserPrincipal principal,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", noticeService.findAllDepartments());
            model.addAttribute("formError", ValidationMessages.firstError(result));
            return "notice/notice-write";
        }
        Admin admin = principal.getAdmin();
        noticeService.createNotice(dto, admin);

        return "redirect:/notices";
    }

    /**
     * 공지 수정 화면. GET이라 아직 제출된 값이 없으므로, 기존 공지 값으로
     * noticeDTO를 직접 채워 폼 초기값으로 쓴다. notice는 id/attachmentUrl
     * 표시용으로 함께 담는다.
     *
     * @param id        수정할 공지 PK
     * @param principal 로그인 관리자 정보
     * @param model     화면 전달용 모델
     * @return "notice/notice-edit"
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserPrincipal principal,
                           Model model) {

        Admin admin = principal.getAdmin();
        Notice notice = noticeService.getNoticeForAdmin(id, admin);

        NoticeDTO dto = new NoticeDTO();
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setCategory(notice.getCategory());
        dto.setPriority(notice.getPriority());
        dto.setDeptId(
                notice.getDepartment() != null
                        ? notice.getDepartment().getDeptId()
                        : null
        );
        dto.setTargetGrade(notice.getTargetGrade());
        dto.setStartDate(notice.getStartDate());
        dto.setEndDate(notice.getEndDate());
        dto.setStatus(notice.getStatus());

        model.addAttribute("notice", notice);
        model.addAttribute("noticeDTO", dto);
        model.addAttribute("departments", noticeService.findAllDepartments());

        return "notice/notice-edit";
    }

    /**
     * 기존 공지 수정 처리. 검증 실패 시 방금 제출한 값(dto)이 Spring에 의해
     * 이미 "noticeDTO"로 model에 남아있어 다시 채울 필요 없고, notice는
     * id/attachmentUrl 표시용으로만 다시 조회한다. formError에 첫 번째
     * 검증 메시지를 담는다.
     *
     * @param id        수정할 공지 PK
     * @param dto       수정 DTO(@Valid 검증 대상, status로 PUBLISHED/DRAFT/CLOSED 전달)
     * @param result    검증 결과
     * @param principal 로그인 관리자 정보
     * @param model     검증 실패 시 재구성용 모델
     * @return 성공 시 "redirect:/notices", 실패 시 "notice/notice-edit"
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("noticeDTO") NoticeDTO dto,
                         BindingResult result,
                         @AuthenticationPrincipal CustomUserPrincipal principal,
                         Model model) {

        Admin admin = principal.getAdmin();
        if (result.hasErrors()) {
            model.addAttribute("notice", noticeService.getNoticeForAdmin(id, admin));
            model.addAttribute("departments", noticeService.findAllDepartments());
            model.addAttribute("formError", ValidationMessages.firstError(result));
            return "notice/notice-edit";
        }
        noticeService.updateNotice(id, dto, admin);

        return "redirect:/notices";
    }

    /**
     * 공지를 DB에서 실제로 삭제한다(되돌릴 수 없음). status=CLOSED(게시종료)와는
     * 별개 기능이며, 확인은 화면의 confirm()에서 이미 처리한다.
     *
     * @param id        삭제할 공지 PK
     * @param principal 로그인 관리자 정보
     * @return "redirect:/notices"
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();
        noticeService.deleteNotice(id, admin);

        return "redirect:/notices";
    }

    /**
     * Notice 엔티티를 목록 표시용 NoticeDTO로 변환한다. admin은 지연로딩이라
     * 여기서 한 번만 adminName으로 평탄화하고, 이후 템플릿은 entity를
     * 전혀 건드리지 않는다. 검증 대상 필드(title 등)도 같이 채우지만
     * 목록 화면에서는 표시 용도로만 쓰인다.
     *
     * @param notice 변환할 공지 엔티티
     * @return 목록 표시용 NoticeDTO
     */
    private NoticeDTO toListItem(Notice notice) {
        NoticeDTO dto = new NoticeDTO();

        dto.setNoticeId(notice.getNoticeId());
        dto.setTitle(notice.getTitle());
        dto.setAdminName(notice.getAdmin().getAdminName());
        dto.setCategory(notice.getCategory());
        dto.setPriority(notice.getPriority());
        dto.setTargetGrade(notice.getTargetGrade());
        dto.setStatus(notice.getStatus());
        dto.setStartDate(notice.getStartDate());
        dto.setEndDate(notice.getEndDate());
        dto.setPinned(Boolean.TRUE.equals(notice.getPinned()));
        dto.setPushSent(Boolean.TRUE.equals(notice.getPushSent()));

        return dto;
    }
}