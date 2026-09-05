package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.ResendService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 공지 재발송 컨트롤러.
 *
 * <p>재발송 대상 목록 조회와 개별 공지 재발송 요청을 처리한다.</p>
 */
@Controller
@RequestMapping("/resend")
@RequiredArgsConstructor
public class ResendController {

    private final ResendService resendService;

    /**
     * 재발송 대상 공지 목록을 조회한다.
     *
     * @param model 화면에 전달할 데이터
     * @param principal 로그인 관리자 정보
     * @return 재발송 관리 화면
     */
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();
        model.addAttribute("notices", resendService.findResendCandidates(admin));

        return "resend/resend";
    }

    /**
     * 선택한 공지를 재발송 처리한다.
     *
     * @param id 공지 PK
     * @param principal 로그인 관리자 정보
     * @param redirectAttributes 재발송 완료 메시지 전달용
     * @return 재발송 목록 화면으로 리다이렉트
     */
    @PostMapping("/{id}")
    public String resend(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            RedirectAttributes redirectAttributes) {

        resendService.resend(id, principal.getAdmin());
        redirectAttributes.addFlashAttribute("resendMessage", "재발송이 완료되었습니다.");

        return "redirect:/resend";
    }
}