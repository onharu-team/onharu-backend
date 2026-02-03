package com.backend.onharu.domain.common.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Spring Security 기반 AuditorAware 구현
 *
 * <p>현재 인증된 사용자의 정보를 JPA Auditing에 제공합니다.</p>
 *
 * <h3>동작 방식:</h3>
 * <ol>
 *   <li>엔티티 저장/수정 시 현재 인증된 사용자 조회</li>
 *   <li>@CreatedBy, @LastModifiedBy에 사용자명 자동 설정</li>
 *   <li>인증되지 않은 경우 "system" 반환</li>
 * </ol>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // 엔티티 저장 시
 * User user = new User(...);
 * userRepository.save(user);
 * // createdBy가 현재 로그인한 사용자명으로 자동 설정됨
 * }</pre>
 *
 * <h3>인증되지 않은 경우:</h3>
 * <ul>
 *   <li>시스템에서 자동 생성되는 경우: "system"</li>
 *   <li>배치 작업 등: "system"</li>
 * </ul>
 */
public class SecurityAuditorAware implements AuditorAware<String> {

    /**
     * 현재 인증된 사용자의 username을 반환합니다.
     *
     * <p>다음 순서로 사용자 정보를 조회합니다:</p>
     * <ol>
     *   <li>Spring Security의 SecurityContext에서 인증된 사용자 조회 (추후 구현)</li>
     *   <li>모두 실패한 경우 "system" 반환</li>
     * </ol>
     *
     * @return 현재 인증된 사용자의 username 또는 "system"
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        // 현재는 시스템에서 자동 생성되는 경우를 위해 "system" 반환

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.ofNullable(authentication.getName()); // 사용자의 loginId 반환
        }

        return Optional.of("system");
    }
}
