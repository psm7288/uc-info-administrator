package uc.dev.uc_info.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uc.dev.uc_info.security.jwt.JwtAuthenticationFilter;

import java.util.List;

/**
 * {@code /api/**}(Flutter 학생 앱용 REST API) 전용 보안 설정.
 *
 * <p>이 체인은 {@code @Order(1)}로 {@code SecurityConfig.filterChain()}
 * ({@code @Order(2)})보다 먼저 평가된다 — {@code /api/**} 요청은 이 체인이
 * 가로채고, 그 외 전체(관리자 웹)는 기존 필터체인이 그대로 처리한다.</p>
 */
@Configuration
@RequiredArgsConstructor
public class ApiSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * {@code /api/**} 전용 필터체인. 무상태(세션 없음)이며 JWT 필터를
     * {@code UsernamePasswordAuthenticationFilter} 앞에 끼워 넣는다.
     * {@code /api/auth/verify}만 인증 없이 열어두고 나머지는 토큰 필요.
     *
     * @param http 설정할 HttpSecurity 빌더
     * @return 구성된 SecurityFilterChain
     * @throws Exception HttpSecurity 설정 중 발생할 수 있는 예외
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(401);
                            response.getWriter().write("{\"message\":\"인증이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(403);
                            response.getWriter().write("{\"message\":\"접근 권한이 없습니다.\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/verify").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }

    /**
     * {@code /api/**}에 대한 CORS 설정. Flutter 앱이 다른 오리진에서
     * 호출하므로 필요하다. 지금은 개발 편의상 전체 오리진을 허용하는데,
     * 배포 시점엔 실제 앱이 쓰는 도메인/스킴으로 좁혀야 한다.
     *
     * @return /api/** 경로에 적용될 CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}