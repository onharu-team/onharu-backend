package com.backend.onharu.domain.owner.dto;

/**
 * 사업자 관련 Query DTO
 */
public class OwnerQuery {

    /**
     * 사업자 ID로 사업자 조회 Query
     */
    public record GetOwnerByIdQuery(
            Long id
    ) {
    }

    /**
     * 로그인 ID로 사업자 조회 Query
     */
    public record GetOwnerByLoginIdQuery(
            String loginId
    ) {
    }

    public record GetOwnerByUserIdQuery(
            Long userId
    ) {
    }
}
