package com.backend.onharu.infra.security.port.impl;

import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.security.LocalUser;
import com.backend.onharu.infra.security.port.ISecuritySession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class ISecuritySessionImpl implements ISecuritySession {

    @Override
    public void login(User user, HttpServletRequest httpRequest) {
        LocalUser localUser = new LocalUser(user);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(localUser, null, localUser.getAuthorities()); // 인증 객체 생성

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(usernamePasswordAuthenticationToken); // SecurityContext 에 인증 정보 저장

        httpRequest.getSession(true)
                .setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        securityContext
                ); // 세션 생성
    }

    @Override
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication); // 세션 무효화 및 인증 삭제
        }
    }

    @Override
    public Long getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LocalUser localUser = (LocalUser) authentication.getPrincipal();

        return Long.valueOf(localUser.getUsername());
    }
}
