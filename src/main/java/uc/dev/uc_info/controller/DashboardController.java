package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uc.dev.uc_info.repository.BannerRepository;
import uc.dev.uc_info.repository.NoticeRepository;
import uc.dev.uc_info.repository.UserRepository;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final NoticeRepository noticeRepository;
    private final BannerRepository bannerRepository;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var waitingNotices = noticeRepository.findByStatusOrderByCreatedAtDesc("WAITING");

        model.addAttribute("publishedCount",  noticeRepository.countByStatus("PUBLISHED"));
        model.addAttribute("waitingCount",    (long) waitingNotices.size());
        model.addAttribute("waitingNotices",  waitingNotices);
        model.addAttribute("activeBannerCount", bannerRepository.countByStatus("ACTIVE"));
        model.addAttribute("totalUserCount",  userRepository.count());
        model.addAttribute("recentNotices",
                noticeRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED")
                        .stream().limit(5).toList());
        return "dashboard/dashboard";
    }
}