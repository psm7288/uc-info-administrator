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
 * 공지 열람 현황 컨트롤러. 폼/모달이 없다 — 계산 결과를 보여주기만 하는
 * 조회 전용 화면이다.
 *
 * <p>이 클래스 안에 아래 1개의 요청 핸들러를 직접 작성하세요.</p>
 *
 * <h3>만들어야 할 핸들러</h3>
 * <ol>
 *   <li>{@code @GetMapping list(Model model, @AuthenticationPrincipal
 *       CustomUserPrincipal principal)} → {@code "tracking/tracking"} 반환.
 *       model에 담을 것: {@code stats}({@code trackingService
 *       .findTrackingStats(admin)}), {@code totalCount}
 *       ({@code stats.size()} — 별도 카운트 쿼리 필요 없음, 이미 계산된
 *       리스트 크기 그대로 씀).</li>
 * </ol>
 */
@Controller
@RequestMapping("/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping
    public String list(
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        Admin admin = principal.getAdmin();

        List<TrackingStatDTO> stats =
                trackingService.findTrackingStats(admin);

        model.addAttribute("stats", stats);
        model.addAttribute("totalCount", stats.size());

        return "tracking/tracking";
    }

}