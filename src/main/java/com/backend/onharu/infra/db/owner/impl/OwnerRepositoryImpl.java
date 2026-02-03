package com.backend.onharu.infra.db.owner.impl;


import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByUserIdParam;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByLoginIdParam;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.repository.OwnerRepository;
import com.backend.onharu.domain.owner.service.OwnerRepositoryParam.GetOwnerByIdParam;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사업자 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class OwnerRepositoryImpl implements OwnerRepository {

    private final OwnerJpaRepository ownerJpaRepository;

    @Override
    public Owner save(Owner owner) {
        return ownerJpaRepository.save(owner);
    }

    @Override
    public Owner getOwnerById(GetOwnerByIdParam query) {
        return ownerJpaRepository.findById(query.id())
                .orElseThrow(() -> new CoreException(ErrorType.Owner.OWNER_NOT_FOUND));
    }

    @Override
    public Owner getOwnerByLoginId(GetOwnerByLoginIdParam query) {
        return ownerJpaRepository.findByUser_LoginId(query.loginId())
                .orElseThrow(() -> new CoreException(ErrorType.Owner.OWNER_NOT_FOUND));
    }

    @Override
    public Owner getOwnerByUserId(GetOwnerByUserIdParam query) {
        return ownerJpaRepository.findByUser_Id(query.userId())
                .orElseThrow(() -> new CoreException(ErrorType.Owner.OWNER_NOT_FOUND));
    }
}