package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

        SettingAlarmDTO alarmDTO = new SettingAlarmDTO();
        alarmDTO.setAlarmTracking(admin.getAlarmTracking());
        alarmDTO.setAlarmSend(admin.getAlarmSend());

        model.addAttribute("accountDTO", accountDTO);
        model.addAttribute("alarmDTO", alarmDTO);

        return "setting/setting";
    }

    /**
     * 관리자 계정 정보를 수정한다.
     *
     * <p>현재 비밀번호를 확인한 후 관리자명과 새 비밀번호를 변경한다.
     * 입력값 검증에 실패하거나 현재 비밀번호가 일치하지 않는 경우
     * 설정 화면에 오류 메시지를 표시한다.</p>
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

            model.addAttribute(
                    "formError",
                    ValidationMessages.firstError(bindingResult)
            );

            return "setting/setting";
        }

        try {
            settingService.updateAccount(dto, admin);

            return "redirect:/settings";

        } catch (IllegalArgumentException e) {
            addAlarmDTO(model, admin);

            model.addAttribute(
                    "formError",
                    e.getMessage()
            );

            return "setting/setting";
        }
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

        settingService.updateAlarm(dto, admin);

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
}