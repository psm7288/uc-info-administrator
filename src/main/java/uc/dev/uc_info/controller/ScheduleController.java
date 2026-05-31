package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.repository.ScheduleRepository;

@Controller
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("schedules", scheduleRepository.findByVisibleTrueOrderByStartDateAsc());
        model.addAttribute("totalCount", scheduleRepository.countByVisibleTrue());
        return "schedule/schedule";
    }
}