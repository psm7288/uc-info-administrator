package uc.dev.uc_info.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import uc.dev.uc_info.security.userdetails.CustomUserDetailService;

/**
 * Spring Security 설정.
 *
 * <p>인가 정책(권한 매트릭스 반영):</p>
 * <ul>
 *   <li>정적 리소스, /login : 누구나 접근(permitAll)</li>
 *   <li>/shuttles/** : 셔틀버스는 학과 무관 학교 공통 정보 → SUPER_ADMIN 전용</li>
 *   <li>그 외 전부 : 로그인한 관리자(authenticated)</li>
 * </ul>
 *
 * <p>"DEPT_ADMIN 은 본인 학과만" 같은 데이터 범위 제한은 URL 인가로 표현할 수
 * 없으므로 Service 레이어에서 admin.department 기준으로 필터링한다.</p>
 *
 * <p>@EnableMethodSecurity 로 @PreAuthorize 등 메서드 보안도 활성화되어 있어,
 * 컨트롤러/서비스 메서드에 hasRole('SUPER_ADMIN') 등을 추가로 걸 수 있다.</p>
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

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception{
        http
            .headers(headers -> headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                    .referrerPolicy(ref -> ref.policy(
                            ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
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
                    .accessDeniedHandler((req, res, e) -> {
                        if (req.getUserPrincipal() == null) {
                            res.sendRedirect("/login");
                        } else {
                            res.sendRedirect("/dashboard");
                        }
                    })
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
}