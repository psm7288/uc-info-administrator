package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.service.SettingService;

/**
 * 관리자 본인 계정 설정 컨트롤러. 화면 하나에 계정정보 폼과 알림 토글 폼이
 * 같이 있고, 각각 다른 POST 엔드포인트로 제출된다(모달 없이 같은 페이지
 * 안에 두 개의 별도 {@code <form>}).
 *
 * <p>이 클래스 안에 아래 3개의 요청 핸들러를 직접 작성하세요. 컨트롤러는
 * {@code SettingService}만 호출한다. 로그인 admin은
 * {@code @AuthenticationPrincipal CustomUserPrincipal principal} →
 * {@code principal.getAdmin()}으로 꺼낸다.</p>
 *
 * <h3>만들어야 할 핸들러</h3>
 * <ol>
 *   <li>{@code @GetMapping list(Model model, @AuthenticationPrincipal
 *       CustomUserPrincipal principal)} → {@code "setting/setting"} 반환.
 *       model에 담을 것: {@code admin}(현재 관리자 정보 — 화면에 기존 이름
 *       표시용, {@code GlobalModelAdvice}가 이미 전역으로 넣어주고 있으니
 *       중복 확인할 것), {@code accountDTO}(new SettingAccountDTO(), adminName만
 *       기존값으로 미리 채워둠), {@code alarmDTO}(new SettingAlarmDTO(),
 *       기존 {@code alarmTracking}/{@code alarmSend} 값으로 미리 채움).</li>
 *   <li>{@code @PostMapping("/account") updateAccount(@Valid
 *       @ModelAttribute("accountDTO") SettingAccountDTO dto, BindingResult
 *       bindingResult, Model model, @AuthenticationPrincipal
 *       CustomUserPrincipal principal)}. 검증 실패 시 목록 화면 재구성
 *       (alarmDTO도 다시 채워야 함) + {@code formError}
 *       ({@code ValidationMessages.firstError}) 담아서 그대로
 *       {@code "setting/setting"} 반환. 성공 시
 *       {@code settingService.updateAccount(dto, admin)} 호출 후
 *       {@code "redirect:/settings"}. Service가 던지는
 *       {@code IllegalArgumentException}(비밀번호 불일치)은 여기서 안 잡고
 *       전역 예외 처리로 넘긴다.</li>
 *   <li>{@code @PostMapping("/alarm") updateAlarm(@ModelAttribute("alarmDTO")
 *       SettingAlarmDTO dto, @AuthenticationPrincipal CustomUserPrincipal
 *       principal)}. 검증 애노테이션이 없는 DTO라 {@code @Valid}/
 *       {@code BindingResult} 불필요. {@code settingService.updateAlarm(dto,
 *       admin)} 호출 후 {@code "redirect:/settings"}.</li>
 * </ol>
 */
@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;
}