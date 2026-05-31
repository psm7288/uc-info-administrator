package uc.dev.uc_info.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.security.core.CustomUserPrincipal;

/**
 * 전 컨트롤러에 공통 모델 속성 주입.
 *
 * WHY: Thymeleaf 3.1 에서 #request/#session 제거됨.
 *      sidebar active 표시에 필요한 currentUri 와
 *      header/sidebar 의 admin 정보를 여기서 전역 주입한다.
 */
@ControllerAdvice(annotations = org.springframework.stereotype.Controller.class)
public class GlobalModelAdvice {

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("admin")
    public Admin admin(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return principal != null ? principal.getAdmin() : null;
    }
}
