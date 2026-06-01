package uc.dev.uc_info.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 독립 컨트롤러가 없는 페이지 라우팅만 담당.
 * notice/banner/tracking/resend/schedule/shuttle/scholarship/setting/dashboard 는
 * 각각의 전용 컨트롤러로 이동됨.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login/login";
    }
}