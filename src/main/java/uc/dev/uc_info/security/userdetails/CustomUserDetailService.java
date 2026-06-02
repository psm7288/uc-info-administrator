package uc.dev.uc_info.security.userdetails;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.repository.AdminRepository;
import uc.dev.uc_info.security.core.CustomUserPrincipal;

/**
 * 폼 로그인 시 username 으로 관리자(Admin)를 조회하는 UserDetailsService 구현체.
 *
 * <p>Spring Security 의 인증 흐름에서 DaoAuthenticationProvider 가 이 서비스를
 * 호출하여 username 에 해당하는 계정을 로드한다. 조회한 Admin 을
 * {@link CustomUserPrincipal} 로 감싸 반환하며, 비밀번호 비교·권한 부여는
 * Principal 과 PasswordEncoder 가 담당한다.</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final AdminRepository adminRepository;

    /**
     * username 으로 관리자를 조회해 UserDetails 로 반환한다.
     *
     * @param username 로그인 ID(admin.username)
     * @return 조회된 관리자를 감싼 {@link CustomUserPrincipal}
     * @throws UsernameNotFoundException 해당 username 의 관리자가 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 관리자 계정입니다."));

        return new CustomUserPrincipal(admin);
    }
}