package com.backend.onharu.domain.common;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 테스트 데이터 생성 헬퍼 클래스
 * <p>
 * 여러 테스트 클래스에서 공통으로 사용할 수 있는 테스트 데이터 생성 메서드를 제공합니다.
 */
@Component
public class TestDataHelper {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    /**
     * 테스트용 User 생성 (아동용)
     */
    public User createTestUserForChild(String loginId, String name, String phone) {
        return userJpaRepository.save(
                User.builder()
                        .loginId(loginId)
                        .password("password123")
                        .name(name)
                        .phone(phone)
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .build()
        );
    }

    /**
     * 테스트용 User 생성 (사업자용)
     */
    public User createTestUserForOwner(String loginId, String name, String phone) {
        return userJpaRepository.save(
                User.builder()
                        .loginId(loginId)
                        .password("password123")
                        .name(name)
                        .phone(phone)
                        .userType(UserType.OWNER)
                        .statusType(StatusType.ACTIVE)
                        .build()
        );
    }

    /**
     * 테스트용 Level 생성 헬퍼 메서드
     */
    private Level createTestLevel(String levelName) {
        return createTestLevel(levelName);
    }

    /**
     * 테스트용 Owner 생성 (User와 함께 생성)
     */
    public Owner createTestOwner(String loginId, String name, String phone, String levelName, String businessNumber) {
        User user = createTestUserForOwner(loginId, name, phone);
        Level level = createTestLevel(levelName);

        return ownerJpaRepository.save(
                Owner.builder()
                        .user(user)
                        .level(level)
                        .businessNumber(businessNumber)
                        .build()
        );
    }

    /**
     * 테스트용 Owner 생성 (기본값 사용)
     */
    public Owner createTestOwner(String loginId, String name, String phone) {
        return createTestOwner(loginId, name, phone, "새싹", "1234567890");
    }

    /**
     * 테스트용 Child 생성 (User와 함께 생성)
     */
    public Child createTestChild(String loginId, String name, String phone, String certificate, Boolean isVerified) {
        User user = createTestUserForChild(loginId, name, phone);

        return childJpaRepository.save(
                Child.builder()
                        .user(user)
                        .certificate(certificate)
                        .isVerified(isVerified != null ? isVerified : true)
                        .build()
        );
    }

    /**
     * 테스트용 Child 생성 (기본값 사용)
     */
    public Child createTestChild(String loginId, String name, String phone) {
        return createTestChild(loginId, name, phone, "/certificates/test.pdf", true);
    }

    /**
     * 테스트용 Category 생성
     */
    public Category createTestCategory(String name) {
        return categoryJpaRepository.save(Category.builder().name(name).build());
    }
}
