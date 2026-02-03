package com.backend.onharu.domain.child.dto;

/**
 * 아동 관련 Query DTO
 */
public class ChildQuery {

    /**
     * 아동 ID로 아동 조회 Query
     */
    public record GetChildByIdQuery(
            Long id
    ) {
    }

    /**
     * 로그인 ID로 아동 조회 Query
     */
    public record GetChildByLoginIdQuery(
            String loginId
    ) {
    }

    /**
     * 사용자 ID로 아동 조회 Query
     */
    public record GetChildByUserIdQuery(
            Long userId
    ) {
    }
}
