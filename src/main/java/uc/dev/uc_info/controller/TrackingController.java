package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.repository.NoticeRepository;
import uc.dev.uc_info.repository.UserRepository;

@Controller
@RequestMapping("/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String tracking(Model model) {
        model.addAttribute("notices", noticeRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED"));
        model.addAttribute("totalUsers", userRepository.count());
        return "tracking/tracking";
    }
}