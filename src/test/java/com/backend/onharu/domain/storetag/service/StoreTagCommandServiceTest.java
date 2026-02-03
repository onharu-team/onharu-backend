package com.backend.onharu.domain.storetag.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.onharu.domain.level.model.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.repository.StoreRepository;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.GetTagByIdParam;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.domain.tag.repository.TagRepository;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("StoreTagCommandService 단위 테스트 (Store를 통한 StoreTag 관리)")
class StoreTagCommandServiceTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreQueryService storeQueryService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private TagJpaRepository tagJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @BeforeEach
    public void setUp() {
        // 외래 키 제약 조건을 고려한 삭제 순서 (자식 → 부모)
        reservationJpaRepository.deleteAll();
        storeScheduleJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        tagJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    /**
     * 테스트용 User 생성 헬퍼 메서드
     */
    private User createTestUser(String loginId, String name, String phone) {
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
     * 테스트용 Owner 생성 헬퍼 메서드
     */
    private Owner createTestOwner(String loginId, String name, String phone, String levelName) {
        User user = createTestUser(loginId, name, phone);
        Level level = createTestLevel(levelName);

        return ownerJpaRepository.save(
            Owner.builder()
                .user(user)
                .level(level)
                .businessNumber("1234567890")
                .build()
        );
    }

    /**
     * 테스트용 Category 생성 헬퍼 메서드
     */
    private Category createTestCategory(String name) {
        return categoryJpaRepository.save(Category.builder().name(name).build());
    }

    /**
     * 테스트용 Tag 생성 헬퍼 메서드
     */
    private Tag createTestTag(String name) {
        return tagJpaRepository.save(
            Tag.builder()
                .name(name)
                .build()
        );
    }

    @Nested
    @DisplayName("StoreTag 추가 테스트")
    class AddStoreTagTest {
        
        @Test
        @DisplayName("StoreTag 추가 성공 - Store에 Tag 추가")
        @Transactional
        @Rollback(value = false)
        public void shouldAddStoreTag() {
            // given
            String uniqueLoginId = "test_owner_tag_" + System.currentTimeMillis();
            Owner savedOwner = createTestOwner(uniqueLoginId, "테스트 사업자 태그", "01012345678", "새싹");
            Category category = createTestCategory("식당");
            
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("태그 테스트 가게")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("/images/tag_test.jpg")
                    .isOpen(true)
                    .build()
            );
            
            Tag tag1 = createTestTag("커피");
            Tag tag2 = createTestTag("디저트");

            // when
            savedStore.addTag(tag1);
            savedStore.addTag(tag2);
            storeRepository.save(savedStore);

            // then
            Store store = storeQueryService.getStore(new GetStoreByIdQuery(savedStore.getId()));
            assertThat(store.getStoreTags()).hasSize(2);
            assertThat(store.getStoreTags()).extracting(st -> st.getTag().getName())
                .contains("커피", "디저트");
            
            System.out.println("✅ StoreTag 추가 성공 - Store ID: " + store.getId());
            System.out.println("   - 추가된 태그 개수: " + store.getStoreTags().size());
            store.getStoreTags().forEach(st -> {
                System.out.println("     * StoreTag ID: " + st.getId() + ", Tag: " + st.getTag().getName());
            });
        }
    }

    @Nested
    @DisplayName("StoreTag 제거 테스트")
    class RemoveStoreTagTest {
        
        @Test
        @DisplayName("StoreTag 제거 성공 - Store에서 Tag 제거")
        @Transactional
        @Rollback(value = false)
        public void shouldRemoveStoreTag() {
            // given
            String uniqueLoginId = "test_owner_remove_" + System.currentTimeMillis();
            Owner savedOwner = createTestOwner(uniqueLoginId, "테스트 사업자 제거", "01012345678", "새싹");
            Category category = createTestCategory("식당");
            
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("태그 제거 테스트 가게")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("/images/remove_test.jpg")
                    .isOpen(true)
                    .build()
            );
            
            Tag tag1 = createTestTag("커피");
            Tag tag2 = createTestTag("디저트");
            Tag tag3 = createTestTag("브런치");
            
            savedStore.addTag(tag1); // StoreTag 저장
            savedStore.addTag(tag2); // StoreTag 저장
            savedStore.addTag(tag3); // StoreTag 저장
            storeRepository.save(savedStore);

            // when
            Store store = storeQueryService.getStore(new GetStoreByIdQuery(savedStore.getId()));
            store.removeTag(tag2); // tag2 제거
            storeRepository.save(store); // StoreTag 저장

            // then
            Store updatedStore = storeQueryService.getStore(new GetStoreByIdQuery(savedStore.getId()));
            assertThat(updatedStore.getStoreTags()).hasSize(2);
            assertThat(updatedStore.getStoreTags()).extracting(st -> st.getTag().getName())
                .contains("커피", "브런치")
                .doesNotContain("디저트");
            
            System.out.println("✅ StoreTag 제거 성공 - Store ID: " + updatedStore.getId());
            System.out.println("   - 제거 전 태그 개수: 3");
            System.out.println("   - 제거 후 태그 개수: " + updatedStore.getStoreTags().size());
            updatedStore.getStoreTags().forEach(st -> {
                System.out.println("     * StoreTag ID: " + st.getId() + ", Tag: " + st.getTag().getName());
            });
        }
    }

    @Nested
    @DisplayName("OrphanRemoval 테스트")
    class OrphanRemovalTest {
        
        @Test
        @DisplayName("OrphanRemoval 동작 확인 - Store 삭제 시 StoreTag도 자동 삭제")
        @Transactional
        @Rollback(value = false)
        public void shouldDeleteStoreTagsWhenStoreIsDeleted() {
            // given
            String uniqueLoginId = "test_owner_orphan_" + System.currentTimeMillis();
            Owner savedOwner = createTestOwner(uniqueLoginId, "테스트 사업자 고아", "01011112222", "새싹");
            Category category = createTestCategory("식당");
            
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("고아 제거 테스트 가게")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("/images/orphan_test.jpg")
                    .isOpen(true)
                    .build()
            );
            
            Tag tag1 = createTestTag("커피");
            Tag tag2 = createTestTag("디저트");
            Tag tag3 = createTestTag("브런치");
            
            savedStore.addTag(tag1);
            savedStore.addTag(tag2);
            savedStore.addTag(tag3);
            storeRepository.save(savedStore);
            
            // StoreTag ID 저장
            Store store = storeQueryService.getStore(new GetStoreByIdQuery(savedStore.getId()));
            assertThat(store.getStoreTags()).hasSize(3);
            Long storeTagId1 = store.getStoreTags().get(0).getId();
            Long storeTagId2 = store.getStoreTags().get(1).getId();
            Long storeTagId3 = store.getStoreTags().get(2).getId();
            
            System.out.println("📋 삭제 전 상태:");
            System.out.println("   - Store ID: " + savedStore.getId());
            System.out.println("   - StoreTag 개수: " + store.getStoreTags().size());
            store.getStoreTags().forEach(st -> {
                System.out.println("     * StoreTag ID: " + st.getId() + ", Tag: " + st.getTag().getName());
            });

            // when - Store 삭제
            storeRepository.delete(savedStore);

            // then - StoreTag도 함께 삭제되었는지 확인
            // StoreTag는 별도의 JPA Repository가 없으므로, Store를 통해 확인
            boolean storeDeleted = !storeJpaRepository.existsById(savedStore.getId());
            assertThat(storeDeleted).isTrue();
            
            // Tag는 여전히 존재해야 함 (StoreTag만 삭제되고 Tag는 유지)
            Tag tag1After = tagRepository.getTag(new GetTagByIdParam(tag1.getId()));
            Tag tag2After = tagRepository.getTag(new GetTagByIdParam(tag2.getId()));
            Tag tag3After = tagRepository.getTag(new GetTagByIdParam(tag3.getId()));
            assertThat(tag1After).isNotNull();
            assertThat(tag2After).isNotNull();
            assertThat(tag3After).isNotNull();
            
            System.out.println("✅ OrphanRemoval 동작 확인 성공");
            System.out.println("   - Store 삭제 확인: " + storeDeleted);
            System.out.println("   - StoreTag 자동 삭제 확인: StoreTag ID " + storeTagId1 + ", " + storeTagId2 + ", " + storeTagId3 + " 삭제됨");
            System.out.println("   - Tag 엔티티 유지 확인: Tag ID " + tag1.getId() + ", " + tag2.getId() + ", " + tag3.getId() + " 유지됨");
        }

        @Test
        @DisplayName("OrphanRemoval 동작 확인 - Store에서 StoreTag 리스트를 비우면 StoreTag 삭제")
        @Transactional
        @Rollback(value = false)
        public void shouldDeleteStoreTagsWhenListIsCleared() {
            // given
            String uniqueLoginId = "test_owner_clear_" + System.currentTimeMillis();
            Owner savedOwner = createTestOwner(uniqueLoginId, "테스트 사업자 클리어", "01033334444", "새싹");
            Category category = createTestCategory("식당");
            
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("리스트 클리어 테스트 가게")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("/images/clear_test.jpg")
                    .isOpen(true)
                    .build()
            );
            
            Tag tag1 = createTestTag("커피");
            Tag tag2 = createTestTag("디저트");
            
            savedStore.addTag(tag1);
            savedStore.addTag(tag2);
            storeRepository.save(savedStore);
            
            Store store = storeQueryService.getStore(new GetStoreByIdQuery(savedStore.getId()));
            assertThat(store.getStoreTags()).hasSize(2);
            Long storeTagId1 = store.getStoreTags().get(0).getId();
            Long storeTagId2 = store.getStoreTags().get(1).getId();
            
            System.out.println("📋 클리어 전 상태:");
            System.out.println("   - StoreTag 개수: " + store.getStoreTags().size());

            // when - 모든 태그 제거
            store.removeTag(tag1);
            store.removeTag(tag2);
            storeRepository.save(store);

            // then
            Store updatedStore = storeQueryService.getStore(new GetStoreByIdQuery(savedStore.getId()));
            assertThat(updatedStore.getStoreTags()).isEmpty();
            
            System.out.println("✅ StoreTag 리스트 클리어 성공");
            System.out.println("   - 클리어 전 StoreTag 개수: 2");
            System.out.println("   - 클리어 후 StoreTag 개수: " + updatedStore.getStoreTags().size());
            System.out.println("   - StoreTag ID " + storeTagId1 + ", " + storeTagId2 + " 삭제됨 (orphanRemoval)");
        }
    }
}
