package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.dto.NoticeDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.NoticeService;

/**
 * 공지 관리 컨트롤러.
 *
 * <p>공지 관련 HTTP 요청을 처리하며,
 * Repository를 직접 호출하지 않고 NoticeService만 사용한다.</p>
 */
@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // ============================================================
    // 공지 목록
    // ============================================================

    /**
     * 공지 목록 화면.
     *
     * GET /notices
     */
    @GetMapping
    public String list(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Model model
    ) {
        Admin admin = principal.getAdmin();

        model.addAttribute(
                "notices",
                noticeService.findNoticesFor(admin)
        );

        model.addAttribute(
                "totalCount",
                noticeService.countAll()
        );

        model.addAttribute(
                "publishedCount",
                noticeService.countByStatus("PUBLISHED")
        );

        return "notice/notice";
    }

    // ============================================================
    // 공지 작성 화면
    // ============================================================

    /**
     * 새 공지 작성 화면.
     *
     * GET /notices/new
     */
    @GetMapping("/new")
    public String writeForm(Model model) {

        model.addAttribute(
                "noticeDTO",
                new NoticeDTO()
        );

        model.addAttribute(
                "departments",
                noticeService.findAllDepartments()
        );

        return "notice/notice-write";
    }

    // ============================================================
    // 공지 작성 처리
    // ============================================================

    /**
     * 새 공지 작성.
     *
     * POST /notices
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("noticeDTO") NoticeDTO dto,
            BindingResult result,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Model model
    ) {

        // DTO 검증 실패
        if (result.hasErrors()) {

            model.addAttribute(
                    "departments",
                    noticeService.findAllDepartments()
            );

            return "notice/notice-write";
        }

        Admin admin = principal.getAdmin();

        noticeService.createNotice(dto, admin);

        return "redirect:/notices";
    }

    // ============================================================
    // 공지 수정 화면
    // ============================================================

    /**
     * 공지 수정 화면.
     *
     * GET /notices/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Model model
    ) {

        Admin admin = principal.getAdmin();

        Notice notice =
                noticeService.getNoticeForAdmin(id, admin);

        model.addAttribute(
                "notice",
                notice
        );

        model.addAttribute(
                "departments",
                noticeService.findAllDepartments()
        );

        return "notice/notice-edit";
    }

    // ============================================================
    // 공지 수정 처리
    // ============================================================

    /**
     * 기존 공지 수정.
     *
     * POST /notices/{id}/edit
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("noticeDTO") NoticeDTO dto,
            BindingResult result,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Model model
    ) {

        Admin admin = principal.getAdmin();

        // DTO 검증 실패
        if (result.hasErrors()) {

            model.addAttribute(
                    "notice",
                    noticeService.getNoticeForAdmin(id, admin)
            );

            model.addAttribute(
                    "departments",
                    noticeService.findAllDepartments()
            );

            return "notice/notice-edit";
        }

        noticeService.updateNotice(
                id,
                dto,
                admin
        );

        return "redirect:/notices";
    }

    // ============================================================
    // 공지 승인
    // ============================================================

    /**
     * WAITING 상태 공지를 승인한다.
     *
     * POST /notices/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public String approve(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        Admin admin = principal.getAdmin();

        noticeService.approveNotice(
                id,
                admin
        );

        return "redirect:/dashboard";
    }

    // ============================================================
    // 공지 삭제
    // ============================================================

    /**
     * 공지를 CLOSED 상태로 변경한다.
     *
     * POST /notices/{id}/delete
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        Admin admin = principal.getAdmin();

        noticeService.deleteNotice(
                id,
                admin
        );

        return "redirect:/notices";
    }
}