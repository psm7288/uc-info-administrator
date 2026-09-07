package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.common.util.ValidationMessages;
import uc.dev.uc_info.dto.SettingAccountDTO;
import uc.dev.uc_info.dto.SettingAlarmDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.SettingService;

/**
 * 관리자 설정 화면을 처리하는 컨트롤러.
 *
 * <p>관리자 계정 정보 수정과 알림 설정 변경 기능을 제공한다.</p>
 */
@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    /**
     * 관리자 설정 화면을 조회한다.
     *
     * @param model 화면에 전달할 모델
     * @param principal 현재 로그인한 관리자 Principal
     * @return 관리자 설정 화면
     */
    @GetMapping
    public String list(
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();

        SettingAccountDTO accountDTO = new SettingAccountDTO();
        accountDTO.setAdminName(admin.getAdminName());

        model.addAttribute("accountDTO", accountDTO);
        addAlarmDTO(model, admin);

        return "setting/setting";
    }

    /**
     * 관리자 계정 정보를 수정한다.
     *
     * <p>현재 비밀번호를 확인한 후 관리자명과 새 비밀번호를 변경한다.
     * 입력값 검증에 실패하면 설정 화면에 오류 메시지를 표시한다.</p>
     *
     * @param dto 계정 수정 요청 데이터
     * @param bindingResult 입력값 검증 결과
     * @param model 화면에 전달할 모델
     * @param principal 현재 로그인한 관리자 Principal
     * @return 처리 결과 화면
     */
    @PostMapping("/account")
    public String updateAccount(
            @Valid @ModelAttribute("accountDTO") SettingAccountDTO dto,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();

        if (bindingResult.hasErrors()) {
            addAlarmDTO(model, admin);
            model.addAttribute("formError", ValidationMessages.firstError(bindingResult));

            return "setting/setting";
        }
        Admin updatedAdmin = settingService.updateAccount(dto, admin);
        refreshSecurityContext(updatedAdmin);

        return "redirect:/settings";
    }

    /**
     * 관리자 알림 설정을 변경한다.
     *
     * @param dto 알림 설정 요청 데이터
     * @param principal 현재 로그인한 관리자 Principal
     * @return 관리자 설정 화면으로 리다이렉트
     */
    @PostMapping("/alarm")
    public String updateAlarm(
            @ModelAttribute("alarmDTO") SettingAlarmDTO dto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();

        Admin updatedAdmin = settingService.updateAlarm(dto, admin);
        refreshSecurityContext(updatedAdmin);

        return "redirect:/settings";
    }

    /**
     * 설정 화면을 다시 표시할 때 필요한 알림 설정 DTO를 모델에 추가한다.
     *
     * @param model 화면에 전달할 모델
     * @param admin 현재 로그인한 관리자
     */
    private void addAlarmDTO(Model model, Admin admin) {
        SettingAlarmDTO alarmDTO = new SettingAlarmDTO();

        alarmDTO.setAlarmTracking(admin.getAlarmTracking());
        alarmDTO.setAlarmSend(admin.getAlarmSend());

        model.addAttribute("alarmDTO", alarmDTO);
    }

    /**
     * 저장 직후 로그인 세션의 Authentication을 갱신된 Admin 정보로
     * 교체한다. 이걸 안 하면 DB엔 새 값이 저장돼도, 세션에 캐시된
     * {@code Authentication} 안의 옛 Admin 스냅샷이 그대로 남아있어서
     * {@code GlobalModelAdvice}가 전역으로 뿌려주는 {@code admin}(사이드바/
     * 헤더/이 화면 전부)이 재로그인 전까지 옛날 값을 계속 보여준다.
     *
     * @param updatedAdmin 저장 직후 최신 상태의 Admin
     */
    private void refreshSecurityContext(Admin updatedAdmin) {
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal updatedPrincipal = new CustomUserPrincipal(updatedAdmin);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedPrincipal,
                currentAuth.getCredentials(),
                updatedPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}