package uc.dev.uc_info.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BannerController {

    @GetMapping("/banners")
    public String bannerPage() {
        return "banner";
    }
}