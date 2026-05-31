package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.repository.BannerRepository;

@Controller
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerRepository bannerRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("banners", bannerRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("activeCount", bannerRepository.countByStatus("ACTIVE"));
        model.addAttribute("scheduledCount", bannerRepository.countByStatus("SCHEDULED"));
        return "banner/banner";
    }
}