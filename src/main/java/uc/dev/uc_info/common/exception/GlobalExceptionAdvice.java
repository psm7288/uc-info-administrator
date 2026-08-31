package uc.dev.uc_info.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 관리자 화면 컨트롤러에서 발생하는 예외를 한 곳에서 잡아 flash
 * 메시지로 담고 리다이렉트한다<br> 화면이 다시 그려질 때 공통 errorModal이
 * 자동으로 열린다.<br> Service는 도메인 예외 4종만 던지고, 나머지 인프라
 * 예외/캐치올은 여기서 함께 처리한다. <br> REST API(@RestController)는 대상 아님.
 */
@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionAdvice {

    /**
     * id로 조회했는데 대상이 없을 때(없는 일정/학과 등) 처리한다. — 404 성격.
     *
     * @param ex                 발생한 예외(메시지를 그대로 화면에 노출)
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return "redirect:"로 시작하는 뷰 이름
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
     * 입력값이 비즈니스 규칙에 어긋날 때(날짜 역전 등) 처리한다. — 400 성격.
     *
     * @param ex                 발생한 예외(메시지를 그대로 화면에 노출)
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return "redirect:"로 시작하는 뷰 이름
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
     * 권한 위반(다른 학과 항목 접근/등록 시도 등)을 처리한다. — 403 성격.
     * Service 계층에서 던진 것은 Security 기본 403 처리로 안 넘어가고 항상
     * 이 경로로 온다.
     *
     * @param ex                 발생한 예외(메시지를 그대로 화면에 노출)
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return "redirect:"로 시작하는 뷰 이름
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
     * 서버 데이터 이상(DEPT_ADMIN인데 소속 학과가 없는 등)을 처리한다. 사용자
     * 잘못이 아니므로 원인은 로그에만 남기고, 화면에는 일반적인 안내만 보여준다.
     *
     * @param ex                 발생한 예외(원인은 서버 로그에만 남김)
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return "redirect:"로 시작하는 뷰 이름
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
     * DB 제약조건 위반(유니크/외래키 등)을 처리한다. Bean Validation을
     * 통과했더라도 동시 요청 등으로 DB 레벨에서만 걸릴 수 있다. — 409 성격.
     *
     * @param ex                 발생한 예외(원인은 서버 로그에만 남김)
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return "redirect:"로 시작하는 뷰 이름
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        log.warn("DB 제약조건 위반: {}", ex.getMessage());

        return redirectWithError(
                "이미 사용 중인 값이거나 처리할 수 없는 요청입니다.",
                request,
                redirectAttributes
        );
    }

    /**
     * URL 경로/쿼리 파라미터 타입이 안 맞을 때(예: /notices/abc/edit) 처리한다.
     * Controller 진입 전에 Spring이 직접 던져서 Service는 요청을 못 받아본다.
     * — 400 성격.
     *
     * @param ex                 발생한 예외
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return "redirect:"로 시작하는 뷰 이름
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return redirectWithError("잘못된 요청입니다.", request, redirectAttributes);
    }

    /**
     * 첨부파일이 설정된 최대 용량을 넘었을 때 처리한다. Controller 진입
     * 전에 발생한다. — 413 성격.
     *
     * @param ex                 발생한 예외
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return "redirect:"로 시작하는 뷰 이름
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return redirectWithError("첨부파일 용량이 너무 큽니다.", request, redirectAttributes);
    }

    /**
     * 위 어디에도 안 걸리는 예외(코드 버그 등)를 위한 마지막 안전망. 사용자
     * 에게는 원인을 숨기고, 서버 로그에는 스택트레이스까지 남긴다.
     *
     * @param ex                 발생한 예외(스택트레이스까지 서버 로그에 기록)
     * @param request            리다이렉트 대상 경로 계산용
     * @param redirectAttributes flash 메시지 전달용
     * @return "redirect:"로 시작하는 뷰 이름
     */
    @ExceptionHandler(Exception.class)
    public String handleUnknown(
            Exception ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        log.error("처리되지 않은 예외 발생", ex);

        return redirectWithError(
                "일시적인 오류가 발생했습니다. 문제가 반복되면 관리자에게 문의해 주세요.",
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
     * @return "redirect:"로 시작하는 뷰 이름
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
     * host/scheme은 버려서, Referer 조작으로 인한 오픈 리다이렉트를 막는다.
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