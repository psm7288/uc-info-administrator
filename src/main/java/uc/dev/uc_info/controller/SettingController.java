package uc.dev.uc_info.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// admin 정보는 GlobalModelAdvice 가 전역 주입하므로 model 추가 주입 불필요
@Controller
@RequestMapping("/settings")
public class SettingController {

    @GetMapping
    public String settings() {
        return "setting/setting";
    }
}