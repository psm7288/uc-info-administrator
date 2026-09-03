package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.service.ResendService;

/**
 * 공지 재발송 컨트롤러. 폼/모달이 없다 — 목록에서 버튼 하나 누르면 바로
 * POST되는 단순 구조다.
 *
 * <p>이 클래스 안에 아래 2개의 요청 핸들러를 직접 작성하세요.</p>
 *
 * <h3>만들어야 할 핸들러</h3>
 * <ol>
 *   <li>{@code @GetMapping list(Model model, @AuthenticationPrincipal
 *       CustomUserPrincipal principal)} → {@code "resend/resend"} 반환.
 *       model에 담을 것: {@code notices}({@code resendService
 *       .findResendCandidates(admin)} — 별도 DTO 변환 없이 그대로 써도
 *       됨, admin fetch join이 이미 Repository 단에서 되어 있어서 화면에
 *       작성자 이름을 보여줘도 N+1 안 남).</li>
 *   <li>{@code @PostMapping("/{id}") resend(@PathVariable Long id,
 *       @AuthenticationPrincipal CustomUserPrincipal principal)}.
 *       {@code resendService.resend(id, admin)} 호출 후
 *       {@code "redirect:/resend"}. Service 예외는 여기서 안 잡고 전역
 *       예외 처리로 넘긴다.</li>
 * </ol>
 */
@Controller
@RequestMapping("/resend")
@RequiredArgsConstructor
public class ResendController {

    private final ResendService resendService;
}