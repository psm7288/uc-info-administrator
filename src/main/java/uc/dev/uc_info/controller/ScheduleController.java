package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.common.util.ValidationMessages;
import uc.dev.uc_info.dto.ScheduleDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Schedule;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.DepartmentService;
import uc.dev.uc_info.service.ScheduleService;

import java.util.List;

/**
 * 학사 일정 관리 컨트롤러. 등록/수정은 별도 폼 페이지가 아니라 목록 화면의
 * 모달(scheduleModal)로 처리한다. 목록 화면은 엔티티를 직접 노출하지 않고
 * {@link ScheduleDTO}(목록 표시 겸용 필드 포함)로 변환해서 넘긴다 —
 * department는 지연로딩이라 뷰에서 바로 쓰면 안 된다.
 */
@Controller
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final DepartmentService departmentService;

    /**
     * 학사 일정 목록 화면. 권한별 일정 목록 + 통계 + 등록/수정 모달용 데이터를
     * model에 담는다. scheduleDTO는 이미 model에 있으면(검증 실패 후 복귀)
     * 새로 만들지 않는다.
     *
     * @param model     화면 전달용 모델
     * @param principal 로그인 관리자 정보
     * @return "schedule/schedule"
     */
    @GetMapping
    public String list(Model model,
                       @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();

        model.addAttribute("schedules", toListItems(admin));
        model.addAttribute("totalCount", scheduleService.countAll(admin));
        model.addAttribute("departments", departmentService.findAll());

        if (!model.containsAttribute("scheduleDTO")) {
            model.addAttribute("scheduleDTO",new ScheduleDTO());
        }
        return "schedule/schedule";
    }

    /**
     * 일정 등록 처리. 검증 통과 시 저장 후 목록으로 리다이렉트(PRG), 실패 시
     * 목록 model을 다시 채우고 openScheduleModal=true로 등록 모달을
     * 재오픈하며 formError에 첫 번째 검증 메시지를 담는다. Service가
     * 던지는 예외는 여기서 안 잡고 전역 예외 처리로 넘긴다.
     *
     * @param dto           등록·검증 DTO
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
            model.addAttribute("schedules", toListItems(admin));
            model.addAttribute("totalCount", scheduleService.countAll(admin));
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("openScheduleModal",true);
            model.addAttribute("formError", ValidationMessages.firstError(bindingResult));

            return "schedule/schedule";
        }
        scheduleService.createSchedule(dto, admin);

        return "redirect:/schedules";
    }

    /**
     * 일정 수정 처리. create와 동일하게 검증 실패 시 목록 model을 다시
     * 채우되, editingScheduleId도 함께 넘겨 "수정 모달이 이 일정을 편집
     * 중"인 상태로 다시 열리게 하고, formError에 첫 번째 검증 메시지를 담는다.
     *
     * @param id            수정할 일정 PK
     * @param dto           수정·검증 DTO
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
            model.addAttribute("schedules", toListItems(admin));
            model.addAttribute("totalCount", scheduleService.countAll(admin));
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("openScheduleModal",true);
            model.addAttribute("editingScheduleId", id);
            model.addAttribute("formError", ValidationMessages.firstError(bindingResult));

            return "schedule/schedule";
        }
        scheduleService.updateSchedule(id, dto, admin);

        return "redirect:/schedules";
    }

    /**
     * 일정 삭제 처리. 별도 확인 화면 없이 바로 삭제한다(화면의 삭제 확인
     * 모달에서 이미 확인시킨 뒤 호출되는 구조). 없는 id/권한 위반 예외는
     * 전역 예외 처리로 넘긴다.
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

    /**
     * 로그인 admin 권한 범위의 일정 목록을 DTO로 변환해 조회한다. create/update
     * 검증 실패 시 목록 화면 재구성에 중복 없이 재사용한다.
     *
     * @param admin 로그인 관리자
     * @return 목록 표시용 ScheduleDTO 리스트
     */
    private List<ScheduleDTO> toListItems(Admin admin) {
        return scheduleService.findSchedulesFor(admin)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    /**
     * Schedule 엔티티를 목록 표시/모달 프리필용 ScheduleDTO로 변환한다.
     * department는 지연로딩이라 여기서 deptId로 평탄화하고, 이후 템플릿은
     * entity를 전혀 건드리지 않는다.
     *
     * @param schedule 변환할 일정 엔티티
     * @return 목록 표시용 ScheduleDTO
     */
    private ScheduleDTO toListItem(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();

        dto.setScheduleId(schedule.getScheduleId());
        dto.setTitle(schedule.getTitle());
        dto.setCategory(schedule.getCategory());
        dto.setTargetGrade(schedule.getTargetGrade());
        dto.setDeptId(
                schedule.getDepartment() != null
                        ? schedule.getDepartment().getDeptId()
                        : null
        );
        dto.setStartDate(schedule.getStartDate());
        dto.setEndDate(schedule.getEndDate());
        dto.setVisible(Boolean.TRUE.equals(schedule.getVisible()));

        return dto;
    }
}