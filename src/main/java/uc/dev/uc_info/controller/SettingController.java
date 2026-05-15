package uc.dev.uc_info.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingController {

    @GetMapping("/settings")
    public String settingPage() {
        return "setting";
    }
}