// LÀM "CẦU NỐI" GIỮA ENTITY USER VÀ SPRING SECURITY
// ĐỂ CUNG CẤP THÔNG TIN CHO SPRING SECURITY KHI XÁC THỰC NGƯỜI DÙNG
package com.foodorder.backend.security;

import com.foodorder.backend.user.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_USER, ROLE_ADMIN, ...
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
    public Long getId() {
        return user.getId();
    }


    // Những method dưới có thể dùng khi khóa tài khoản
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isVerified();
    }

    public User getUser() {
        return user;
    }
}
