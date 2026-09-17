package uc.dev.uc_info.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * {@code /api/**} 요청마다 {@code Authorization: Bearer <token>} 헤더를 읽어
 * 검증하고, 유효하면 studentId를 SecurityContext의 인증 주체(principal)로
 * 세팅한다.
 *
 * <p>토큰이 없거나 무효해도 여기서 요청을 막지 않는다 — 그냥 인증 정보를
 * 안 세팅하고 다음 필터로 넘긴다. 실제 접근 차단은
 * {@code ApiSecurityConfig}의 {@code authorizeHttpRequests()} 규칙이 담당한다</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 요청마다 한 번씩 실행된다({@link OncePerRequestFilter}가 보장).
     * {@link #resolveToken}으로 헤더에서 토큰을 꺼내고, 토큰이 있고
     * {@link JwtTokenProvider#validateToken}을 통과하면 studentId를
     * SecurityContext에 인증 정보로 세팅한다. 토큰이 없거나 무효하면
     * 아무 것도 안 하고 그대로 다음 필터로 넘긴다
     *
     * @param request     현재 요청
     * @param response    현재 응답
     * @param filterChain 다음 필터로 넘기기 위한 체인
     * @throws ServletException 필터 체인 처리 중 서블릿 관련 오류
     * @throws IOException      필터 체인 처리 중 입출력 오류
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String studentId = jwtTokenProvider.getStudentId(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(studentId, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * {@code Authorization} 헤더에서 {@code Bearer } 접두사를 뗀 토큰
     * 문자열을 꺼낸다.
     *
     * @param request 현재 요청
     * @return 토큰 문자열, 헤더가 없거나 형식이 다르면 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}