package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.repository.NoticeRepository;

@Controller
@RequestMapping("/resend")
@RequiredArgsConstructor
public class ResendController {

    private final NoticeRepository noticeRepository;

    @GetMapping
    public String resend(Model model) {
        model.addAttribute("notices", noticeRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED"));
        return "resend/resend";
    }
}