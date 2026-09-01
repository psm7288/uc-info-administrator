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
package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.dto.BannerDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.BannerService;

/**
 * 배너 관리 컨트롤러.
 *
 * <p>배너 목록 조회와 등록, 수정, 삭제 요청을 처리한다.
 * 등록/수정은 별도 페이지가 아닌 bannerModal을 사용한다.</p>
 *
 * <p>Service에서 발생한 도메인 예외는 직접 처리하지 않고
 * GlobalExceptionAdvice에 위임한다.</p>
 */
@Controller
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 배너 관리 목록 화면을 조회한다.
     *
     * @param model 화면에 전달할 데이터
     * @param principal 로그인 관리자 정보
     * @return 배너 관리 화면
     */
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();
        setListModel(model, admin);

        if (!model.containsAttribute("bannerDTO")) {
            model.addAttribute("bannerDTO", new BannerDTO());
        }

        return "banner/banner";
    }

    /**
     * 새 배너를 등록한다.
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("bannerDTO") BannerDTO dto,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Admin admin = principal.getAdmin();

        if (bindingResult.hasErrors()) {
            setListModel(model, admin);
            model.addAttribute("openBannerModal", true);
            return "banner/banner";
        }

        bannerService.createBanner(dto, admin);
        return "redirect:/banners";
    }

    /**
     * 기존 배너를 수정한다.
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("bannerDTO") BannerDTO dto,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Admin admin = principal.getAdmin();

        if (bindingResult.hasErrors()) {
            setListModel(model, admin);
            model.addAttribute("openBannerModal", true);
            model.addAttribute("editingBannerId", id);
            return "banner/banner";
        }

        bannerService.updateBanner(id, dto, admin);
        return "redirect:/banners";
    }

    /**
     * 배너를 삭제한다.
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        bannerService.deleteBanner(id, principal.getAdmin());
        return "redirect:/banners";
    }

    /**
     * 목록 화면에 공통으로 필요한 데이터를 채운다.
     *
     * <p>등록/수정 검증 실패 시에도 동일한 화면을 다시 렌더링해야 하므로
     * 중복 코드를 별도 메서드로 분리한다.</p>
     */
    private void setListModel(Model model, Admin admin) {
        model.addAttribute("banners", bannerService.findAllFor(admin));
        model.addAttribute("activeCount", bannerService.countByStatus("ACTIVE"));
        model.addAttribute("scheduledCount", bannerService.countByStatus("SCHEDULED"));
        model.addAttribute("linkableNotices", bannerService.findLinkableNotices());
    }
}