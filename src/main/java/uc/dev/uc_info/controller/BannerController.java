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
import uc.dev.uc_info.model.Banner;
import uc.dev.uc_info.model.Department;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.BannerService;

import java.util.List;

/**
 * 배너 관리 컨트롤러. Schedule과 동일하게 등록/수정은 별도 폼 페이지가
 * 아니라 목록 화면의 모달({@code bannerModal})로 처리한다. 목록 화면은
 * 엔티티를 직접 노출하지 않고 {@link BannerDTO}로 변환해서 넘긴다 —
 * admin/notice/notice.department가 전부 지연로딩이라 뷰에서 바로 쓰면 안 된다.
 */
@Controller
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 배너 목록 화면. 권한별 목록 + 통계 + 등록/수정 모달용 데이터를
     * model에 담는다. bannerDTO는 이미 model에 있으면(검증 실패 후 복귀)
     * 새로 만들지 않는다.
     *
     * @param model     화면 전달용 모델
     * @param principal 로그인 관리자 정보
     * @return "banner/banner"
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
     * 배너 등록 처리. 검증 통과 시 저장 후 목록으로 리다이렉트(PRG), 실패 시
     * 목록 model을 다시 채우고 openBannerModal=true로 등록 모달을 재오픈한다.
     * Service가 던지는 예외는 여기서 안 잡고 전역 예외 처리로 넘긴다.
     *
     * @param dto           등록·검증 DTO
     * @param bindingResult {@code @Valid} 검증 결과
     * @param model         검증 실패 시 목록 화면 재구성용 모델
     * @param principal     로그인 관리자 정보
     * @return 정상 시 "redirect:/banners", 검증 실패 시 "banner/banner"
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
     * 배너 수정 처리. create와 동일하게 검증 실패 시 목록 model을 다시
     * 채우되, editingBannerId도 함께 넘겨 "수정 모달이 이 배너를 편집
     * 중"인 상태로 다시 열리게 한다.
     *
     * @param id            수정할 배너 PK
     * @param dto           수정·검증 DTO
     * @param bindingResult {@code @Valid} 검증 결과
     * @param model         검증 실패 시 목록 화면 재구성용 모델
     * @param principal     로그인 관리자 정보
     * @return 정상 시 "redirect:/banners", 검증 실패 시 "banner/banner"
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
     * 배너 삭제 처리. 별도 확인 화면 없이 바로 삭제한다(화면의 삭제 확인
     * 모달에서 이미 확인시킨 뒤 호출되는 구조). Service 예외는 전역 예외
     * 처리로 넘긴다.
     *
     * @param id        삭제할 배너 PK
     * @param principal 로그인 관리자 정보
     * @return "redirect:/banners"
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        bannerService.deleteBanner(id, principal.getAdmin());
        return "redirect:/banners";
    }

    /**
     * 목록 화면에 공통으로 필요한 데이터를 채운다. 등록/수정 검증 실패
     * 시에도 동일한 화면을 다시 렌더링해야 하므로 중복 없이 재사용한다.
     * 통계(activeCount/scheduledCount)도 admin 권한 범위로 스코프해서
     * 아래 목록과 숫자가 항상 일치하게 한다.
     *
     * @param model 채울 모델
     * @param admin 로그인 관리자(권한 범위 판단용)
     */
    private void setListModel(Model model, Admin admin) {
        model.addAttribute("banners", toListItems(admin));
        model.addAttribute("activeCount", bannerService.countByStatus("ACTIVE", admin));
        model.addAttribute("scheduledCount", bannerService.countByStatus("SCHEDULED", admin));
        model.addAttribute("linkableNotices", bannerService.findLinkableNotices(admin));
    }

    /**
     * 로그인 admin 권한 범위의 배너 목록을 DTO로 변환해 조회한다.
     *
     * @param admin 로그인 관리자
     * @return 목록 표시용 BannerDTO 리스트
     */
    private List<BannerDTO> toListItems(Admin admin) {
        return bannerService.findAllFor(admin)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    /**
     * Banner 엔티티를 목록 표시/모달 프리필용 BannerDTO로 변환한다. admin/
     * notice/notice.department는 전부 지연로딩이라 여기서 한 번만 값을
     * 꺼내고, 이후 템플릿은 entity를 전혀 건드리지 않는다.
     *
     * @param banner 변환할 배너 엔티티
     * @return 목록 표시용 BannerDTO
     */
    private BannerDTO toListItem(Banner banner) {
        BannerDTO dto = new BannerDTO();

        dto.setBannerId(banner.getBannerId());
        dto.setTitle(banner.getTitle());
        dto.setSubtitle(banner.getSubtitle());
        dto.setStatus(banner.getStatus());
        dto.setStartDate(banner.getStartDate());
        dto.setEndDate(banner.getEndDate());

        Notice notice = banner.getNotice();
        dto.setNoticeId(notice != null ? notice.getNoticeId() : null);
        dto.setLinkedDeptName(resolveLinkedDeptName(notice));

        return dto;
    }

    /**
     * 연결된 공지를 기준으로 표시용 학과명을 계산한다.
     *
     * @param notice 배너에 연결된 공지(없으면 null)
     * @return "공지 미연결" / "전체 학과" / 실제 학과명
     */
    private String resolveLinkedDeptName(Notice notice) {
        if (notice == null) {
            return "공지 미연결";
        }
        Department department = notice.getDepartment();
        return department != null ? department.getDeptName() : "전체 학과";
    }
}