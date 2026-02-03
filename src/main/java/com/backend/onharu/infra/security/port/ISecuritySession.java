package com.backend.onharu.infra.security.port;

import com.backend.onharu.domain.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 스프링 시큐리티 세션 제어 관련 추상 인터페이스 입니다.
 */
public interface ISecuritySession {

    void login(User user, HttpServletRequest httpRequest);

    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    Long getUsername();
}
