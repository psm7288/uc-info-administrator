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