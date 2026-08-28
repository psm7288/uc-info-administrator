//package uc.dev.uc_info.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//@Controller
//@RequestMapping("/dashboard")
//@RequiredArgsConstructor
//public class DashboardController {
//
//}

package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 대시보드 화면을 담당하는 컨트롤러.
 *
 * <p>로그인 성공 후 이동하는 /dashboard 요청을 처리하고,
 * 관리자 대시보드 화면을 반환한다.</p>
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /**
     * 관리자 대시보드 화면을 반환한다.
     *
     * <p>SecurityConfig에서 로그인 성공 후 /dashboard로 이동하도록
     * 설정되어 있으므로 해당 GET 요청을 처리하는 메서드가 필요하다.</p>
     *
     * @return templates/dashboard/dashboard.html 템플릿 경로
     */
    @GetMapping
    public String dashboard() {
        return "dashboard/dashboard";
    }
}