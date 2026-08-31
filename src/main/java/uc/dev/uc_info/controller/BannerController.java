package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.service.BannerService;

/**
 * 배너 관리 컨트롤러.
 *
 * <p>이 클래스 안에 아래 4개의 요청 핸들러를 직접 작성하세요. Schedule/
 * Shuttle과 동일하게 등록/수정은 별도 폼 페이지가 아니라 목록 화면의
 * 모달({@code bannerModal})로 처리한다.</p>
 *
 * <p>컨트롤러는 {@code BannerService}만 호출한다(Repository/NoticeService
 * 직접 호출 금지 — 연결 공지 목록도 반드시 {@code bannerService.
 * findLinkableNotices()}를 통해서 가져온다). 로그인 admin은
 * {@code @AuthenticationPrincipal CustomUserPrincipal principal} 파라미터로
 * 받아 {@code principal.getAdmin()}으로 꺼낸다.</p>
 *
 * <h3>만들어야 할 핸들러</h3>
 * <ol>
 *   <li>{@code @GetMapping list(Model model, @AuthenticationPrincipal
 *       CustomUserPrincipal principal)} → {@code "banner/banner"} 반환.
 *       model에 담을 것: {@code banners}({@code bannerService.findAllFor
 *       (admin)}), {@code activeCount}({@code countByStatus("ACTIVE")}),
 *       {@code scheduledCount}({@code countByStatus("SCHEDULED")}),
 *       {@code linkableNotices}({@code findLinkableNotices()} — 연결 공지
 *       select용), {@code bannerDTO}(모달 바인딩용, 이미 model에 있으면
 *       새로 만들지 않음).</li>
 *   <li>{@code @PostMapping create(@Valid @ModelAttribute("bannerDTO")
 *       BannerDTO dto, BindingResult bindingResult, Model model,
 *       @AuthenticationPrincipal CustomUserPrincipal principal)} —
 *       Schedule의 create()와 동일 패턴(BindingResult 검증 실패 시 목록
 *       model 재구성 + {@code openBannerModal=true}로 재오픈, 성공 시
 *       {@code createBanner} 호출 후 {@code "redirect:/banners"}).</li>
 *   <li>{@code @PostMapping("/{id}/edit") update(@PathVariable Long id,
 *       @Valid @ModelAttribute("bannerDTO") BannerDTO dto, BindingResult
 *       bindingResult, Model model, @AuthenticationPrincipal
 *       CustomUserPrincipal principal)} — Schedule의 update()와 동일 패턴
 *       (검증 실패 시 {@code editingBannerId}도 함께 model에 추가).</li>
 *   <li>{@code @PostMapping("/{id}/delete") delete(@PathVariable Long id,
 *       @AuthenticationPrincipal CustomUserPrincipal principal)} —
 *       Schedule의 delete()와 동일 패턴({@code deleteBanner} 호출 후
 *       {@code "redirect:/banners"}).</li>
 * </ol>
 *
 * <p>Service가 던지는 예외는 여기서 잡지 않는다 — 전역
 * {@code GlobalExceptionAdvice}가 flash+에러모달로 처리한다.</p>
 */
@Controller
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;
}