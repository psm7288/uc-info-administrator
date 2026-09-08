package uc.dev.uc_info.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uc.dev.uc_info.rest.dto.UserResponse;
import uc.dev.uc_info.rest.service.UserService;

/**
 * 학생 본인 정보 REST 컨트롤러.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    /**
     * 토큰에서 꺼낸 studentId로 본인 정보를 조회한다. studentId는 URL/쿼리로
     * 안 받고, {@code JwtAuthenticationFilter}가 SecurityContext에 세팅해둔
     * 값을 그대로 쓴다
     *
     * @return 200 + 학생 정보(name/department/year/studentId)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        String studentId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.getMe(studentId));
    }
}