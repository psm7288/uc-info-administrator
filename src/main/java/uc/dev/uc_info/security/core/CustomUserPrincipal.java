package uc.dev.uc_info.security.core;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uc.dev.uc_info.model.Admin;

import java.util.Collection;
import java.util.Collections;

/**
 * 관리자(Admin) 엔티티를 감싸는 Spring Security UserDetails 구현체.
 *
 * <p>권한은 {@link Admin#getRole()} 값을 기반으로 부여한다.
 * role 이 "DEPT_ADMIN" 이면 ROLE_DEPT_ADMIN, "SUPER_ADMIN" 이면 ROLE_SUPER_ADMIN.
 * Spring Security 의 hasRole(...) 은 자동으로 "ROLE_" 접두사를 붙여 비교하므로,
 * 여기서는 "ROLE_" + role 형태로 권한 문자열을 만든다.</p>
 *
 * <p>role 이 null 인 경우(데이터 이상) 권한 없이 처리되지 않도록 기본값을
 * ROLE_DEPT_ADMIN 으로 둔다. 운영상 모든 admin 은 role 을 가져야 한다.</p>
 */
public class CustomUserPrincipal implements UserDetails {

    /** role 컬럼이 비어있을 때의 안전 기본 권한 */
    private static final String DEFAULT_ROLE = "DEPT_ADMIN";
    private final Admin admin;

    public CustomUserPrincipal(Admin admin){
        this.admin = admin;
    }

    public Admin getAdmin(){
        return admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role =  (admin.getRole() != null && !admin.getRole().isBlank())
                ? admin.getRole()
                : DEFAULT_ROLE;
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return admin.getPassword();
    }

    @Override
    public String getUsername() {
        return admin.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
