package com.backend.onharu.infra.security;

import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class LocalUser implements UserDetails {

    private final User user;

    @Override
    public String getUsername() {
        return user.getId().toString();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()));
    }

    @Override
    public boolean isAccountNonExpired() { // 유효 기간 여부
        return !user.getStatusType().equals(StatusType.DELETED);
    }

    @Override
    public boolean isAccountNonLocked() { // 잠김 여부
        StatusType status = user.getStatusType();

        return status != StatusType.LOCKED && status != StatusType.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() { // 비밀번호 만료 여부
        return true;
    }

    @Override
    public boolean isEnabled() { // 활성화 여부
        StatusType status = user.getStatusType();

        return status.equals(StatusType.ACTIVE) || status.equals(StatusType.PENDING);
    }
}
