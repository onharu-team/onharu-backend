package com.backend.onharu.domain.owner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByIdQuery;
import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByUserIdQuery;
import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByLoginIdQuery;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByLoginIdParam;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByUserIdParam;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.repository.OwnerRepository;
import com.backend.onharu.domain.owner.service.OwnerRepositoryParam.GetOwnerByIdParam;

import lombok.RequiredArgsConstructor;

/**
 * 사업자 Query Service
 * 
 * 사업자 도메인의 상태를 조회하는 비즈니스 로직을 처리하는 서비스입니다.
 * Query 패턴을 사용하여 도메인 모델의 조회 작업을 캡슐화합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerQueryService {

    private final OwnerRepository ownerRepository;

    /**
     * 사업자 ID로 사업자를 조회합니다.
     * 
     * @param query 사업자 ID를 포함한 Query
     * @return 조회된 사업자 엔티티
     */
    public Owner getOwnerById(GetOwnerByIdQuery query) {
        return ownerRepository.getOwnerById(new GetOwnerByIdParam(query.id()));
    }

    /**
     * 로그인 ID로 사용자를 조회합니다.
     * 
     * @param query 로그인 ID를 포함한 Query
     * @return 조회된 사용자 엔티티 (없으면 null)
     */
    public Owner getOwnerByLoginId(GetOwnerByLoginIdQuery query) {
        return ownerRepository.getOwnerByLoginId(new GetOwnerByLoginIdParam(query.loginId()));
    }

    /**
     * 사용자 ID(UserId) 로 사업자를 조회합니다.
     *
     * @param query 사용자 ID를 포함한 Query
     * @return 조회된 사업자 엔티티
     */
    public Owner getOwnerByUserId(GetOwnerByUserIdQuery query) {
        return ownerRepository.getOwnerByUserId(new GetOwnerByUserIdParam(query.userId()));
    }
}
