package com.backend.onharu.domain.child.service;

import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByLoginIdQuery;
import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByUserIdQuery;
import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByIdParam;
import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByLoginIdParam;
import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByUserIdParam;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 Query Service
 * <p>
 * 사용자 도메인의 상태를 조회하는 비즈니스 로직을 처리하는 서비스입니다.
 * Query 패턴을 사용하여 도메인 모델의 조회 작업을 캡슐화합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildQueryService {

    private final ChildRepository childRepository;

    /**
     * 아동 ID로 사용자를 조회합니다.
     *
     * @param query 아동 ID를 포함한 Query
     * @return 조회된 사용자 엔티티
     */
    public Child getChildById(GetChildByIdQuery query) {
        return childRepository.getChildById(new GetChildByIdParam(query.id()));
    }

    /**
     * 로그인 ID로 사용자를 조회합니다.
     *
     * @param query 로그인 ID를 포함한 Query
     * @return 조회된 사용자 엔티티 (없으면 null)
     */
    public Child getChildByLoginId(GetChildByLoginIdQuery query) {
        return childRepository.getChildByLoginId(new GetChildByLoginIdParam(query.loginId()));
    }

    /**
     * 사용자 ID로 아동을 조회합니다.
     * @param query 사용자 ID를 포함한 Query
     * @return 조회된 아동 엔티티 (없으면 null)
     */
    public Child getChildByUserId(GetChildByUserIdQuery query) {
        return childRepository.getChildByUserId(new GetChildByUserIdParam(query.userId()));
    }
}
