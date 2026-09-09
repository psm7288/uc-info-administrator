package uc.dev.uc_info.rest.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * {@code /api/**}(Flutter 학생 앱) REST 엔드포인트 전용 예외 처리.
 *
 * <p>REST API는 JSON 에러 응답이 필요해서 완전히 분리했다.
 * {@code basePackages}로 {@code rest.controller} 패키지에만
 * 적용되게 범위를 좁혔다</p>
 *
 * <p>Service 계층은 REST든 관리자 웹이든 동일하게 4종 예외
 * (EntityNotFoundException/IllegalArgumentException/IllegalStateException/
 * AccessDeniedException)만 던지는 원칙을 그대로 따른다</p>
 */
@RestControllerAdvice(basePackages = "uc.dev.uc_info.rest.controller")
public class RestExceptionAdvice {

    /**
     * 존재하지 않는 리소스 조회 시 404로 응답한다.
     *
     * @param e Service가 던진 예외
     * @return 404 + 에러 메시지
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }

    /**
     * 잘못된 요청값 시 400으로 응답한다.
     *
     * @param e Service가 던진 예외
     * @return 400 + 에러 메시지
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    /**
     * 서버 내부 상태 이상 시 409(Conflict)로 응답한다.
     * REST는 클라이언트가 구분해서 처리할 수 있게 상태코드 자체를 분리한다.
     *
     * @param e Service가 던진 예외
     * @return 409 + 에러 메시지
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage()));
    }

    /**
     * 인증/본인확인 실패, 접근 권한 없음을 403으로 응답한다. 학생 인증
     * (verify)이 이름/학과/학번 불일치일 때도 이 예외를 쓴다.
     *
     * @param e Service가 던진 예외
     * @return 403 + 에러 메시지
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage()));
    }

    /**
     * 예상 못 한 예외를 500으로 잡아준다.
     * 내부 예외 메시지·스택트레이스는 절대 그대로 노출하지 않는다(정보 노출 방지).
     *
     * @param e 잡히지 않은 예외
     * @return 500 + 일반화된 메시지
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "일시적인 오류가 발생했습니다."));
    }
}