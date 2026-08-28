package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.dto.ScheduleDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.DepartmentService;
import uc.dev.uc_info.service.ScheduleService;

/**
 * 학사 일정 관리 컨트롤러.
 *
 * <p>화면 ↔ 핸들러</p>
 * <ul>
 *   <li>GET  /schedules             : 목록(+등록/수정 모달용 데이터) → schedule/schedule</li>
 *   <li>POST /schedules             : 등록 처리 → redirect:/schedules</li>
 *   <li>POST /schedules/{id}/edit   : 수정 처리 → redirect:/schedules</li>
 *   <li>POST /schedules/{id}/delete : 삭제 처리 → redirect:/schedules</li>
 * </ul>
 *
 * <p>일정 등록/수정은 별도 폼 페이지가 아니라 목록 화면의 모달(scheduleModal)로 처리한다.
 * 그래서 작성/수정 '폼 GET' 핸들러는 없고, 목록(GET)과 처리(POST)만 있다. 모달의 대상
 * 학과 select 에 쓸 학과 목록(departments)은 목록 화면 model 에 항상 함께 담는다.</p>
 *
 * <p>컨트롤러는 {@link ScheduleService} 와 {@link DepartmentService} 만 호출한다
 * (Repository 직접 호출 금지). 로그인 admin 은
 * {@code @AuthenticationPrincipal CustomUserPrincipal principal} → {@code principal.getAdmin()}
 * 으로 꺼낸다.</p>
 *
 * <h3>검증/예외 처리 방식 — 실패 지점이 둘로 나뉜다</h3>
 * <ul>
 *   <li><b>폼 형식 검증 실패({@code @Valid})</b>: {@link ScheduleDTO} 의 Bean Validation
 *       ({@code @NotBlank} 등)에 걸리면 BindingResult.hasErrors() 가 true 가 되고,
 *       리다이렉트 없이 이 자리에서 목록 화면(schedule/schedule)을 다시 반환하면서
 *       모달을 재오픈한다(openScheduleModal=true, edit 인 경우 editingScheduleId 도 함께).</li>
 *   <li><b>비즈니스 규칙 위반(Service 예외)</b>: 날짜 역전, 존재하지 않는 학과,
 *       권한 위반 등은 {@link ScheduleService} 가 예외를 던지고, 이 컨트롤러는
 *       따로 잡지 않는다 — {@code GlobalExceptionAdvice}(전역)가 잡아서 에러 메시지를
 *       flash 로 담아 리다이렉트하고, 공통 errorModal 이 자동으로 열린다.</li>
 * </ul>
 */
@Controller
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final DepartmentService departmentService;

    /**
     * 학사 일정 목록 화면.
     *
     * <p>처리: 로그인 admin 권한에 맞는 일정 목록 + 통계 + 등록/수정 모달용 데이터를
     * model 에 담는다.</p>
     *
     * <p>model</p>
     * <ul>
     *   <li>schedules   : 권한 범위의 일정 목록(숨김 포함, 시작일순)</li>
     *   <li>totalCount  : 전체 건수</li>
     *   <li>departments : 모달의 대상 학과 select 용 학과 목록</li>
     *   <li>scheduleDTO : 모달 바인딩용 빈 DTO. 이미 model 에 있으면(예: 검증 실패 후
     *       되돌아온 경우) 새로 만들지 않는다.</li>
     * </ul>
     *
     * @param model     화면 전달용 모델
     * @param principal 로그인 관리자 정보
     * @return "schedule/schedule"
     */
    @GetMapping
    public String list(Model model,
                       @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();
        model.addAttribute("schedules", scheduleService.findSchedulesFor(admin));
        model.addAttribute("totalCount", scheduleService.countAll());
        model.addAttribute("departments", departmentService.findAll());

        if (!model.containsAttribute("scheduleDTO")) {
            model.addAttribute("scheduleDTO",new ScheduleDTO());
        }
        return "schedule/schedule";
    }

    /**
     * 일정 등록 처리. — 등록 모달 제출(POST /schedules).
     *
     * <p>처리: BindingResult 에 검증 실패가 없으면 로그인 admin 으로
     * {@link ScheduleService#createSchedule} 을 호출한 뒤 목록으로 리다이렉트한다(PRG 패턴).</p>
     *
     * <p>검증 실패 시(hasErrors): 목록 화면에 필요한 model(schedules/totalCount/departments)을
     * 다시 채우고, openScheduleModal=true 로 등록 모달을 다시 열어 에러를 보여준다
     * (리다이렉트하지 않고 바로 뷰를 반환).</p>
     * <p>Service 에서 던지는 EntityNotFoundException/AccessDeniedException/
     * IllegalArgumentException 등은 여기서 처리하지 않고 전역 예외 처리
     * ({@code GlobalExceptionAdvice})로 넘긴다.</p>
     *
     * @param dto           등록·검증 DTO({@code @Valid @ModelAttribute("scheduleDTO")})
     * @param bindingResult {@code @Valid} 검증 결과
     * @param model         검증 실패 시 목록 화면 재구성용 모델
     * @param principal     로그인 관리자 정보
     * @return 정상 시 "redirect:/schedules", 검증 실패 시 "schedule/schedule"
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("scheduleDTO") ScheduleDTO dto,
                         BindingResult bindingResult,
                         Model model,
                         @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();
        if (bindingResult.hasErrors()) {
            model.addAttribute("schedules", scheduleService.findSchedulesFor(admin));
            model.addAttribute("totalCount", scheduleService.countAll());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("openScheduleModal",true);

            return "schedule/schedule";
        }
        scheduleService.createSchedule(dto, admin);

        return "redirect:/schedules";
    }

    /**
     * 일정 수정 처리. — 수정 모달 제출(POST /schedules/{id}/edit).
     *
     * <p>처리: BindingResult 에 검증 실패가 없으면 로그인 admin 으로
     * {@link ScheduleService#updateSchedule} 을 호출한 뒤 목록으로 리다이렉트한다.</p>
     *
     * <p>검증 실패 시(hasErrors): create 와 동일하게 목록 model 을 다시 채우고,
     * openScheduleModal=true 와 editingScheduleId(=id)를 함께 넘겨서 "수정 모달이
     * 해당 일정을 편집 중인 상태"로 다시 열리도록 한다.</p>
     * <p>Service 예외(EntityNotFoundException/AccessDeniedException 등)는
     * 전역 예외 처리({@code GlobalExceptionAdvice})로 넘긴다.</p>
     *
     * @param id            수정할 일정 PK
     * @param dto           수정·검증 DTO({@code @Valid @ModelAttribute("scheduleDTO")})
     * @param bindingResult {@code @Valid} 검증 결과
     * @param model         검증 실패 시 목록 화면 재구성용 모델
     * @param principal     로그인 관리자 정보
     * @return 정상 시 "redirect:/schedules", 검증 실패 시 "schedule/schedule"
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("scheduleDTO") ScheduleDTO dto,
                         BindingResult bindingResult,
                         Model model,
                         @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();
        if (bindingResult.hasErrors()) {
            model.addAttribute("schedules", scheduleService.findSchedulesFor(admin));
            model.addAttribute("totalCount", scheduleService.countAll());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("openScheduleModal",true);
            model.addAttribute("editingScheduleId", id);

            return "schedule/schedule";
        }
        scheduleService.updateSchedule(id, dto, admin);

        return "redirect:/schedules";
    }

    /**
     * 일정 삭제 처리. — 삭제 버튼(POST /schedules/{id}/delete).
     *
     * <p>처리: 로그인 admin 으로 {@link ScheduleService#deleteSchedule} 을 호출한 뒤
     * 목록으로 리다이렉트한다. 별도 확인 화면 없이 바로 삭제한다(화면에서 삭제 확인
     * 모달로 한 번 확인시킨 뒤 이 핸들러를 호출하는 구조).</p>
     * <p>없는 id → EntityNotFoundException, 권한 위반 → AccessDeniedException.
     * 둘 다 전역 예외 처리({@code GlobalExceptionAdvice})로 넘긴다.</p>
     *
     * @param id        삭제할 일정 PK
     * @param principal 로그인 관리자 정보
     * @return "redirect:/schedules"
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();
        scheduleService.deleteSchedule(id, admin);

        return "redirect:/schedules";
    }
}