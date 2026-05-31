package uc.dev.uc_info.security.core;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uc.dev.uc_info.model.Admin;

import java.util.Collection;
import java.util.Collections;

public class CustomUserPrincipal implements UserDetails {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private final Admin admin;

    public CustomUserPrincipal(Admin admin){
        this.admin = admin;
    }

    public Admin getAdmin(){
        return admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(ROLE_ADMIN));
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
