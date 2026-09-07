package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.dto.TrackingStatDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.TrackingService;

import java.util.List;

/**
 * 공지 열람 현황 컨트롤러. 폼/모달이 없는 조회 전용 화면이다. 재발송
 * 액션은 여기 두지 않고 {@code /resend} 화면으로 링크만 연결한다
 */
@Controller
@RequestMapping("/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    /**
     * 공지 열람 현황 목록 화면.
     *
     * @param model     화면 전달용 모델
     * @param principal 로그인 관리자 정보
     * @return "tracking/tracking"
     */
    @GetMapping
    public String list(
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();
        List<TrackingStatDTO> stats =
                trackingService.findTrackingStats(admin);

        model.addAttribute("stats", stats);
        model.addAttribute("totalCount", stats.size());

        return "tracking/tracking";
    }
}