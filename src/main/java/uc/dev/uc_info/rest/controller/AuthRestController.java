package uc.dev.uc_info.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uc.dev.uc_info.rest.dto.AuthResponse;
import uc.dev.uc_info.rest.dto.VerifyRequest;
import uc.dev.uc_info.rest.service.AuthService;

/**
 * 학생 앱 인증 REST 컨트롤러. {@code ApiSecurityConfig}에서 이 엔드포인트
 * ({@code /api/auth/verify})만 인증 없이 열려있다 — 토큰을 아직 못 받은
 * 상태에서 호출하는 게 당연한 엔드포인트라서다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    /**
     * 이름+학과+학번으로 본인을 확인하고 JWT를 발급한다.
     *
     * @param request 인증 요청 바디(name/department/studentId)
     * @return 200 + 토큰·학생 정보. 실패 시 {@code RestExceptionAdvice}가 403으로 응답
     */
    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(authService.verify(request));
    }
}