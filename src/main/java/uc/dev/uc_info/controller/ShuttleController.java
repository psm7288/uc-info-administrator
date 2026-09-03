package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.common.util.ValidationMessages;
import uc.dev.uc_info.dto.ShuttleDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Shuttle;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.ShuttleService;

import java.util.List;

/**
 * 셔틀버스 노선 관리 컨트롤러. 등록/수정은 별도 폼 페이지가 아니라 목록
 * 화면의 모달({@code shuttleModal})로 처리한다. 목록 화면은 엔티티를 직접
 * 노출하지 않고 {@link ShuttleDTO}로 변환해서 넘긴다.
 */
@Controller
@RequestMapping("/shuttles")
@RequiredArgsConstructor
public class ShuttleController {

    private final ShuttleService shuttleService;

    /**
     * 셔틀 노선 목록 화면. 전체 목록 + 통계 + 등록/수정 모달용 데이터를
     * model에 담는다. shuttleDTO는 이미 model에 있으면(검증 실패 후 복귀)
     * 새로 만들지 않는다.
     *
     * @param model     화면 전달용 모델
     * @param principal 로그인 관리자 정보
     * @return "shuttle/shuttle"
     */
    @GetMapping
    public String list(
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        model.addAttribute("shuttles", toListItems());
        model.addAttribute("totalCount", shuttleService.countAll());
        model.addAttribute("activeCount", shuttleService.countActive());

        if (!model.containsAttribute("shuttleDTO")) {
            model.addAttribute("shuttleDTO", new ShuttleDTO());
        }

        return "shuttle/shuttle";
    }

    /**
     * 셔틀 노선 등록 처리. 검증 통과 시 저장 후 목록으로 리다이렉트(PRG),
     * 실패 시 목록 model을 다시 채우고 openShuttleModal=true로 등록 모달을
     * 재오픈하며 formError에 첫 번째 검증 메시지를 담는다.
     *
     * @param dto           등록·검증 DTO
     * @param bindingResult {@code @Valid} 검증 결과
     * @param model         검증 실패 시 목록 화면 재구성용 모델
     * @param principal     로그인 관리자 정보
     * @return 정상 시 "redirect:/shuttles", 검증 실패 시 "shuttle/shuttle"
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("shuttleDTO") ShuttleDTO dto,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Admin admin = principal.getAdmin();

        if (bindingResult.hasErrors()) {
            model.addAttribute("shuttles", toListItems());
            model.addAttribute("totalCount", shuttleService.countAll());
            model.addAttribute("activeCount", shuttleService.countActive());
            model.addAttribute("openShuttleModal", true);
            model.addAttribute("formError", ValidationMessages.firstError(bindingResult));

            return "shuttle/shuttle";
        }

        shuttleService.createShuttle(dto, admin);

        return "redirect:/shuttles";
    }

    /**
     * 셔틀 노선 수정 처리. create와 동일하게 검증 실패 시 목록 model을
     * 다시 채우되, editingShuttleId도 함께 넘겨 "수정 모달이 이 노선을
     * 편집 중"인 상태로 다시 열리게 한다.
     *
     * @param id            수정할 노선 PK
     * @param dto           수정·검증 DTO
     * @param bindingResult {@code @Valid} 검증 결과
     * @param model         검증 실패 시 목록 화면 재구성용 모델
     * @param principal     로그인 관리자 정보
     * @return 정상 시 "redirect:/shuttles", 검증 실패 시 "shuttle/shuttle"
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("shuttleDTO") ShuttleDTO dto,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Admin admin = principal.getAdmin();

        if (bindingResult.hasErrors()) {
            model.addAttribute("shuttles", toListItems());
            model.addAttribute("totalCount", shuttleService.countAll());
            model.addAttribute("activeCount", shuttleService.countActive());
            model.addAttribute("openShuttleModal", true);
            model.addAttribute("editingShuttleId", id);
            model.addAttribute("formError", ValidationMessages.firstError(bindingResult));

            return "shuttle/shuttle";
        }
        shuttleService.updateShuttle(id, dto, admin);

        return "redirect:/shuttles";
    }

    /**
     * 셔틀 노선 삭제 처리. 별도 확인 화면 없이 바로 삭제한다(화면의 삭제
     * 확인 폼에서 이미 confirm()으로 확인시킨 뒤 호출되는 구조).
     *
     * @param id        삭제할 노선 PK
     * @param principal 로그인 관리자 정보
     * @return "redirect:/shuttles"
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Admin admin = principal.getAdmin();
        shuttleService.deleteShuttle(id, admin);

        return "redirect:/shuttles";
    }

    /**
     * 전체 노선 목록을 DTO로 변환해 조회한다. create/update 검증 실패 시
     * 목록 화면 재구성에 중복 없이 재사용한다.
     *
     * @return 목록 표시용 ShuttleDTO 리스트
     */
    private List<ShuttleDTO> toListItems() {
        return shuttleService.findAll()
                .stream()
                .map(this::toListItem)
                .toList();
    }

    /**
     * Shuttle 엔티티를 목록 표시/모달 프리필용 ShuttleDTO로 변환한다.
     *
     * @param shuttle 변환할 노선 엔티티
     * @return 목록 표시용 ShuttleDTO
     */
    private ShuttleDTO toListItem(Shuttle shuttle) {
        ShuttleDTO dto = new ShuttleDTO();

        dto.setShuttleId(shuttle.getShuttleId());
        dto.setRouteName(shuttle.getRouteName());
        dto.setDeparture(shuttle.getDeparture());
        dto.setDestination(shuttle.getDestination());
        dto.setWaypoints(shuttle.getWaypoints());
        dto.setFirstDeparture(shuttle.getFirstDeparture());
        dto.setLastDeparture(shuttle.getLastDeparture());
        dto.setStatus(shuttle.getStatus());

        return dto;
    }
}