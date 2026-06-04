package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.dto.ScheduleDTO;
import uc.dev.uc_info.service.ScheduleService;

/**
 * 학사 일정 관리 컨트롤러.
 *
 * <p>화면 ↔ 핸들러</p>
 * <ul>
 *   <li>GET  /schedules             : 목록 → schedule/schedule</li>
 *   <li>POST /schedules             : 등록 처리 → redirect:/schedules</li>
 *   <li>POST /schedules/{id}/edit   : 수정 처리 → redirect:/schedules</li>
 *   <li>POST /schedules/{id}/delete : 삭제 처리 → redirect:/schedules</li>
 * </ul>
 *
 * <p>일정 등록/수정은 별도 폼 페이지가 아니라 목록 화면의 모달(scheduleModal)로 처리한다.
 * 그래서 작성/수정 '폼 GET' 핸들러는 없고, 목록(GET)과 처리(POST)만 있다.
 * 모달의 대상 학과 select 에 쓸 학과 목록(departments)은 목록 화면 model 에 함께 담아야 한다.</p>
 *
 * <p>컨트롤러는 ScheduleService 만 호출한다(Repository 직접 호출 금지).
 * 로그인 admin 은 @AuthenticationPrincipal CustomUserPrincipal.getAdmin() 으로.</p>
 *
 * <h3>구현 담당자가 알아야 할 예외/검증 처리</h3>
 * <ul>
 *   <li><b>BindingResult 필수</b>: @Valid 쓰는 create/update 는 BindingResult 를
 *       DTO 바로 뒤에 둬야 한다(없으면 검증 위반 시 400). hasErrors() 면 목록/모달로 되돌림.</li>
 *   <li><b>서비스 예외는 전역에서</b>: EntityNotFoundException / 권한 예외 /
 *       IllegalArgumentException 은 여기서 잡지 말고 @ControllerAdvice(본인 담당)로.</li>
 *   <li>모달 방식이라 검증 실패 시 되돌림 처리가 폼 페이지보다 까다롭다(목록 데이터 다시 담고
 *       모달 자동 오픈 등). 플래시 속성(RedirectAttributes)으로 에러 전달도 한 방법 — 정책 결정.</li>
 * </ul>
 */
@Controller
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    // TODO(담당): 모달 대상 학과 select 용 학과 목록 주입에 DepartmentService 도 주입.

    /**
     * 학사 일정 목록 화면.
     *
     * <p>처리: 로그인 admin 권한에 맞는 일정 목록 + 통계를 화면에 전달한다.</p>
     * <p>model 에 담을 것: 일정 목록(schedules), 전체 건수(totalCount).
     * 모달용 대상 학과 목록(departments)도 함께.
     * — schedule.html 이 ${schedules}, ${totalCount} 사용.</p>
     *
     * @return "schedule/schedule"
     */
    @GetMapping
    public String list(Model model) {
        throw new UnsupportedOperationException(
                "구현 예정: 권한별 일정 목록 + totalCount + (모달용)departments model 주입");
    }

    /**
     * 일정 등록 처리.
     *
     * <p>처리: 로그인 admin 으로 createSchedule 호출 → 목록으로 리다이렉트(PRG).</p>
     * <p>예외/검증(담당자 구현 시): <b>BindingResult</b> 추가 + hasErrors 분기.
     *   서비스의 EntityNotFound/권한 예외는 전역 처리로.</p>
     *
     * @param dto 등록·검증 DTO(@Valid @ModelAttribute)
     * @return "redirect:/schedules"
     */
    @PostMapping
    public String create(@Valid @ModelAttribute ScheduleDTO dto) {
        throw new UnsupportedOperationException(
                "구현 예정: BindingResult 검증분기 → admin 획득 → createSchedule(dto, admin) → redirect:/schedules");
    }

    /**
     * 일정 수정 처리.
     *
     * <p>처리: 로그인 admin 으로 updateSchedule 호출 → 목록으로 리다이렉트.</p>
     * <p>예외/검증: create 와 동일(BindingResult 분기). 서비스 EntityNotFound/권한 예외는 전역으로.</p>
     *
     * @param id  일정 PK
     * @param dto 수정 DTO(@Valid @ModelAttribute)
     * @return "redirect:/schedules"
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute ScheduleDTO dto) {
        throw new UnsupportedOperationException(
                "구현 예정: BindingResult 검증분기 → admin 획득 → updateSchedule(id, dto, admin) → redirect:/schedules");
    }

    /**
     * 일정 삭제 처리.
     *
     * <p>처리: deleteSchedule 호출 후 목록으로 리다이렉트.</p>
     * <p>예외: 없는 id → EntityNotFoundException, 권한 위반 → 권한 예외. 전역 처리로.</p>
     *
     * @param id 일정 PK
     * @return "redirect:/schedules"
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        throw new UnsupportedOperationException(
                "구현 예정: admin 획득 → deleteSchedule(id, admin) → redirect:/schedules");
    }
}