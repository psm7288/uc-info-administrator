package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.repository.NoticeRepository;

@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeRepository noticeRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("notices", noticeRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("totalCount", noticeRepository.count());
        model.addAttribute("publishedCount", noticeRepository.countByStatus("PUBLISHED"));
        return "notice/notice";
    }

    @GetMapping("/new")
    public String writeForm() {
        return "notice/notice-write";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        noticeRepository.findById(id).ifPresent(n -> model.addAttribute("notice", n));
        return "notice/notice-edit";
    }
}