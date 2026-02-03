package com.backend.onharu.domain.owner.repository;

import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByUserIdParam;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByLoginIdParam;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.service.OwnerRepositoryParam.GetOwnerByIdParam;

/**
 * 사업자 Repository 인터페이스
 * 
 * 사업자 도메인 모델의 영속성을 관리하는 Repository 인터페이스입니다.
 * 도메인 레이어에 속하며, 실제 구현은 Infrastructure 레이어에서 제공됩니다.
 */
public interface OwnerRepository {

    /**
     * 사업자를 저장합니다.
     * 
     * @param owner 저장할 사업자 엔티티
     * @return 저장된 사업자 엔티티
     */
    Owner save(Owner owner);

    /**
     * 사업자를 조회합니다.
     * 
     * @param query 사업자 ID를 포함한 Query
     * @return 조회된 사업자 엔티티
     */
    Owner getOwnerById(GetOwnerByIdParam query);

    /**
     * 로그인 ID로 사업자를 조회합니다.
     * 
     * @param query 로그인 ID를 포함한 Query
     * @return 조회된 사업자 엔티티
     */
    Owner getOwnerByLoginId(GetOwnerByLoginIdParam query);

    /**
     * 사용자 ID 로 사업자를 조회합니다.
     *
     * @param param 사용자 ID 를 포함한 Query
     * @return 조회된 사업자 엔티티
     */
    Owner getOwnerByUserId(GetOwnerByUserIdParam param);
}
