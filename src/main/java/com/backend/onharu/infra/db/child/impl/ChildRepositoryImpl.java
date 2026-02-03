package com.backend.onharu.infra.db.child.impl;

import com.backend.onharu.domain.child.dto.ChildRepositoryParam;
import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByIdParam;
import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByLoginIdParam;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.repository.ChildRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.infra.db.child.ChildJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 아동 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ChildRepositoryImpl implements ChildRepository {

    private final ChildJpaRepository childJpaRepository;

    @Override
    public Child save(Child child) {
        return childJpaRepository.save(child);
    }

    @Override
    public Child getChildById(GetChildByIdParam param) {
        return childJpaRepository.findById(param.id())
                .orElseThrow(() -> new CoreException(ErrorType.Child.CHILD_NOT_FOUND));
    }

    @Override
    public Child getChildByLoginId(GetChildByLoginIdParam param) {
        return childJpaRepository.findByUser_LoginId(param.loginId())
                .orElseThrow(() -> new CoreException(ErrorType.Child.CHILD_NOT_FOUND));
    }

    @Override
    public Child getChildByUserId(ChildRepositoryParam.GetChildByUserIdParam param) {
        return childJpaRepository.findByUser_Id(param.userId())
                .orElseThrow(() -> new CoreException(ErrorType.Child.CHILD_NOT_FOUND));
    }
}
