package com.backend.onharu.infra.db.owner;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.owner.model.Owner;

/**
 * 사업자 JPA Repository
 */
public interface OwnerJpaRepository extends JpaRepository<Owner, Long> {
    /**
     * User의 loginId로 Owner 조회
     * 
     * @param loginId 사용자 로그인 ID
     * @return Owner (없으면 Optional.empty())
     */
    Optional<Owner> findByUser_LoginId(String loginId);

    /**
     * 사용자 ID (UserId) 로 Owner 조회
     *
     * @param userId 사용자 ID
     * @return Owner (없으면 Optional.empty())
     */
    Optional<Owner> findByUser_Id(Long userId);
}
