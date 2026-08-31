package uc.dev.uc_info.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring 기본 /error 디스패치를 templates/error/error.html 로 연결하는
 * 컨트롤러.<br> "error/error"는 Spring Boot 기본 명명 규칙(error/404, error/4xx,
 * error)과 안 맞아 자동으로 안 잡히므로, ErrorController를 직접 구현해 뷰
 * 이름과 모델을 직접 채운다.<br> 이 빈이 있으면 기본 BasicErrorController 대신 쓰인다.
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * /error 디스패치를 처리해서 error/error.html 을 렌더링한다.
     *
     * @param request 상태 코드가 담긴 요청(RequestDispatcher 속성에서 추출)
     * @param model   화면 전달용 모델(statusCode/errorHeading/errorMessage)
     * @return "error/error"
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = statusAttr != null ? Integer.parseInt(statusAttr.toString()) : 500;

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorHeading", resolveHeading(statusCode));
        model.addAttribute("errorMessage", resolveMessage(statusCode));

        return "error/error";
    }

    /**
     * 상태 코드별 제목을 반환한다. 알려진 코드가 아니면 일반 안내로 대체한다.
     *
     * @param statusCode HTTP 상태 코드
     * @return 화면에 보여줄 제목
     */
    private String resolveHeading(int statusCode) {
        if (statusCode == 404) {
            return "페이지를 찾을 수 없습니다";
        }
        if (statusCode == 403) {
            return "접근 권한이 없습니다";
        }
        return "오류가 발생했습니다";
    }

    /**
     * 상태 코드별 안내 문구를 반환한다. 알려진 코드가 아니면 일반 안내로 대체한다.
     *
     * @param statusCode HTTP 상태 코드
     * @return 화면에 보여줄 안내 문구
     */
    private String resolveMessage(int statusCode) {
        if (statusCode == 404) {
            return "요청하신 주소를 찾을 수 없습니다. 주소를 다시 확인해 주세요.";
        }
        if (statusCode == 403) {
            return "이 페이지에 접근할 권한이 없습니다.";
        }
        return "요청을 처리하는 중 문제가 발생했습니다.";
    }
}