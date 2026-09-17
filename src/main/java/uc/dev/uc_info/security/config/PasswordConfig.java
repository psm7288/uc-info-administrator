package uc.dev.uc_info.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 설정.
 *
 * <p>BCrypt 기반 {@link PasswordEncoder} 빈을 등록한다. 관리자(admin) 계정의
 * 비밀번호를 해싱·검증하는 데 사용되며, DaoAuthenticationProvider 가 폼 로그인
 * 시 이 인코더로 입력 비밀번호와 저장된 해시를 비교한다.</p>
 *
 * <p>SecurityConfig 와 분리한 이유: PasswordEncoder 는 인증 설정 외에도
 * (시드 데이터 생성, 비밀번호 변경 등) 여러 곳에서 주입받을 수 있는 공용 빈이라
 * 독립 설정 클래스로 둔다.</p>
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt 기반 PasswordEncoder 빈.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}