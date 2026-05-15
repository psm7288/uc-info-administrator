package uc.dev.uc_info.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NoticeController {

    @GetMapping("/notices")
    public String noticeListPage() {
        return "notice";
    }

    @GetMapping("/notices/new")
    public String noticeWritePage() {
        return "notice-write";
    }

    @PostMapping("/notices")
    public String createNotice() {
        // TODO: 추후 NoticeRequest, NoticeService 연결 예정
        return "redirect:/notices";
    }

    @GetMapping("/notices/{id}/edit")
    public String noticeEditPage() {
        return "notice-edit";
    }

    @PostMapping("/notices/{id}/edit")
    public String updateNotice() {
        // TODO: 추후 NoticeRequest, NoticeService 연결 예정
        return "redirect:/notices";
    }
}