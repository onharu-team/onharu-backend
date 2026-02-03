package com.backend.onharu.domain.child.dto;

/**
 * 아동 Repository 파라미터
 */
public class ChildRepositoryParam {
    /**
     * 아동 ID로 조회하는 파라미터
     */
    public record GetChildByIdParam(
            Long id
    ) {
    }

    /**
     * 로그인 ID로 조회하는 파라미터
     */
    public record GetChildByLoginIdParam(
            String loginId
    ) {
    }

    /**
     * 사용자 ID 로 조회하는 파라미터
     */
    public record GetChildByUserIdParam(
            Long userId
    ) {
    }
}
