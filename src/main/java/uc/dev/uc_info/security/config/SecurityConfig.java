package uc.dev.uc_info.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;
import java.io.IOException;

/**
 * Spring Security 설정. 정적 리소스/로그인은 permitAll, /shuttles/**는
 * SUPER_ADMIN 전용, 나머지는 인증 필요로 URL 인가를 건다. "DEPT_ADMIN은
 * 본인 학과만" 같은 데이터 범위 제한은 URL로 표현 못 해서 Service 레이어에서
 * admin.department 기준으로 필터링한다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.security.csp:}")
    private String cspDirectives;

    @Value("${app.security.cspReportOnly:false}")
    private boolean cspReportOnly;

    @Value("${app.security.csrfEnabled:true}")
    private boolean csrfEnabled;

    /**
     * HTTP 보안 필터체인을 구성한다. 보안 헤더(CSP/Referrer-Policy 등), URL
     * 인가 규칙, 접근 거부 처리, 폼 로그인/로그아웃을 설정하며, csrfEnabled가
     * false면(dev 프로파일) CSRF를 끈다.
     *
     * @param http 설정할 HttpSecurity 빌더
     * @return 구성된 SecurityFilterChain
     * @throws Exception HttpSecurity 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception{
        http
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .referrerPolicy(ref -> ref.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                        .contentTypeOptions(Customizer.withDefaults())
                        .contentSecurityPolicy(csp -> {
                            if (cspDirectives != null && !cspDirectives.isBlank()) {
                                String sanitized = cspDirectives.replaceAll("[\r\n]+", " ");
                                csp.policyDirectives(sanitized);
                                if (cspReportOnly) {
                                    csp.reportOnly();
                                }
                            }
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(   "/login",
                                "/css/**", "/js/**", "/images/**", "/script/**",
                                "/sitemap.xml", "/robots.txt", "/favicon.ico").permitAll()
                        .requestMatchers("/shuttles/**").hasRole("SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(this::handleAccessDenied)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID")
                );
        if (!csrfEnabled) {
            http.csrf(csrf -> csrf.disable());
        }
        return http.build();
    }

    /**
     * URL 인가 단계에서 막힌 요청을 처리한다. 로그인 자체를 안 했으면 로그인
     * 화면으로, 로그인은 했지만 권한이 없으면 대시보드로 보내면서
     * GlobalExceptionAdvice와 같은 방식(flash+에러모달)으로 안내한다.
     *
     * @param request  요청(로그인 여부 확인용)
     * @param response 응답(리다이렉트 처리용)
     * @param ex       Security가 던진 접근 거부 예외
     * @throws IOException 리다이렉트(sendRedirect) 중 입출력 오류가 발생한 경우
     */
    private void handleAccessDenied(HttpServletRequest request,
                                    HttpServletResponse response,
                                    AccessDeniedException ex) throws IOException{
        if (request.getUserPrincipal() == null) {
            response.sendRedirect("/login");
            return;
        }
        String targetPath = "/dashboard";

        FlashMap flashMap = new FlashMap();
        flashMap.put("errorMessage", "이 메뉴에 접근할 권한이 없습니다.");
        flashMap.setTargetRequestPath(targetPath);

        new SessionFlashMapManager().saveOutputFlashMap(flashMap, request, response);
        response.sendRedirect(targetPath);
    }
}