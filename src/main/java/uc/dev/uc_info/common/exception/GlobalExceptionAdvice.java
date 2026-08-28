package uc.dev.uc_info.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 관리자 화면 컨트롤러({@code @Controller})에서 발생하는 도메인 예외를
 * 한 곳에서 공통으로 처리하는 전역 예외 처리기.
 *
 * <p>{@code uc.dev.uc_info.controller.GlobalModelAdvice} 와 동일한 스코프
 * ({@code @Controller} 애노테이션이 붙은 컨트롤러만 대상)로 잡아서, 추후 만들
 * REST API 용 {@code @RestController} 에는 이 처리가 적용되지 않는다 — REST
 * 쪽은 JSON 에러 응답이 필요하므로 별도로 처리해야 한다(여기서 다루지 않음).</p>
 *
 * <p><b>처리 방식</b>: 각 Service 도메인 예외를 잡아서 메시지를 flash 속성
 * ({@code errorMessage})에 담고, 요청이 들어온 화면으로 다시 리다이렉트한다.
 * 그 화면이 다시 렌더링될 때 {@code fragments/modals.html} 의 공통
 * {@code errorModal} 이 {@code errorMessage} 존재 여부를 보고 자동으로 열린다.
 * X 버튼이나 배경 클릭으로 닫을 수 있다(js/common/modal.js 의
 * {@code openModal}/{@code closeAllModals} 를 그대로 재사용).</p>
 *
 * <p><b>리다이렉트 대상 결정({@link #safeRedirectPath})</b>: Referer 헤더 값을
 * scheme/host 까지 포함해서 그대로 신뢰하면, 클라이언트가 Referer 를 조작해
 * 외부 사이트로 리다이렉트시키는 <b>오픈 리다이렉트(open redirect)</b> 취약점이
 * 생긴다. 그래서 host/scheme 은 버리고 경로(+쿼리)만 취해서, 항상 우리 서버
 * 안에서만 리다이렉트되도록 한다. Referer 가 없거나 파싱에 실패하면
 * 대시보드로 보낸다.</p>
 */
@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionAdvice {

    /**
     * id 로 조회했는데 대상이 없을 때(없는 일정/학과 등). — 404 성격.
     *
     * @param ex                 발생한 예외(메시지를 그대로 화면에 노출)
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return 원래 화면(경로)으로 리다이렉트, 없으면 대시보드
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return redirectWithError(ex.getMessage(), request, redirectAttributes);
    }

    /**
     * 입력값이 비즈니스 규칙에 어긋날 때(날짜 역전 등 — Bean Validation 으로
     * 못 잡는 필드 간 비교/DB 의존 검증). — 400 성격.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return redirectWithError(ex.getMessage(), request, redirectAttributes);
    }

    /**
     * 권한 위반(다른 학과 항목 접근/등록 시도 등). — 403 성격.
     *
     * <p>Spring Security 의 {@link AccessDeniedException} 과 동일한 클래스다.
     * 이 핸들러가 Spring MVC 단계에서 먼저 잡기 때문에, Service 계층에서 던진
     * 권한 예외는 Security 의 기본 403 처리(AccessDeniedHandler)로 넘어가지
     * 않고 항상 이 경로로 온다.</p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return redirectWithError(ex.getMessage(), request, redirectAttributes);
    }

    /**
     * 서버 데이터 이상(DEPT_ADMIN 인데 소속 학과가 없는 등 정상적으로는
     * 발생하면 안 되는 상황). 사용자 잘못이 아니므로 서버 로그에 warn 으로
     * 남기고, 화면에는 원본 메시지 대신 일반적인 안내만 보여준다.
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        log.warn("데이터 이상으로 처리 중단: {}", ex.getMessage(), ex);

        return redirectWithError(
                "일시적인 오류가 발생했습니다. 관리자에게 문의해 주세요.",
                request,
                redirectAttributes
        );
    }

    /**
     * flash 속성에 에러 메시지를 담고, 안전하게 계산한 경로로 되돌린다.
     *
     * @param message            화면에 보여줄 에러 메시지
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return "redirect:" 로 시작하는 뷰 이름
     */
    private String redirectWithError(
            String message,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {

        redirectAttributes.addFlashAttribute("errorMessage", message);

        return "redirect:" + safeRedirectPath(request);
    }

    /**
     * Referer 헤더에서 "경로(path)+쿼리"만 추출해서 리다이렉트 대상으로 쓴다.
     *
     * <p>Referer 값을 scheme/host 까지 포함해서 그대로 리다이렉트에 쓰면,
     * 클라이언트가 이 헤더를 조작해 외부 사이트로 리다이렉트시키는 오픈
     * 리다이렉트 취약점이 생긴다. host/scheme 은 버리고 경로만 취하면,
     * Referer 에 어떤 값이 오더라도 항상 우리 서버 안의 상대 경로로만
     * 리다이렉트되므로 이 위험이 원천적으로 없어진다.</p>
     *
     * @param request Referer 확인용
     * @return 안전하게 추출한 상대 경로(+쿼리). 없거나 파싱 실패 시 "/dashboard"
     */
    private String safeRedirectPath(HttpServletRequest request) {
        String referer = request.getHeader("Referer");

        if (referer == null) {
            return "/dashboard";
        }

        try {
            URI uri = new URI(referer);
            String path = uri.getRawPath();
            if (path == null || path.isBlank()) {
                return "/dashboard";
            }
            String query = uri.getRawQuery();

            return query != null ? path + "?" + query : path;
        } catch (URISyntaxException e) {
            return "/dashboard";
        }
    }
}