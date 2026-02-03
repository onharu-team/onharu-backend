package com.backend.onharu.domain.storetag.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.user.model.User;
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
import com.backend.onharu.domain.store.model.StoreTag;
import com.backend.onharu.domain.store.repository.StoreRepository;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("StoreTagQueryService 단위 테스트 (Store를 통한 StoreTag 조회)")
class StoreTagQueryServiceTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreQueryService storeQueryService;

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
    private com.backend.onharu.domain.user.model.User createTestUser(String loginId, String name, String phone) {
        return userJpaRepository.save(
            com.backend.onharu.domain.user.model.User.builder()
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
    @DisplayName("StoreTag 조회 테스트")
    class GetStoreTagsTest {
        
        @Test
        @DisplayName("조회 성공 - Store를 통해 StoreTag 목록 조회")
        @Transactional
        @Rollback(value = false)
        public void shouldGetStoreTags() {
            // given
            String uniqueLoginId = "test_owner_query_" + System.currentTimeMillis();
            Owner savedOwner = createTestOwner(uniqueLoginId, "테스트 사업자 조회", "01055556666", "새싹");
            Category category = createTestCategory("식당");
            
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("태그 조회 테스트 가게")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("/images/query_test.jpg")
                    .isOpen(true)
                    .build()
            );
            
            Tag tag1 = createTestTag("커피");
            Tag tag2 = createTestTag("디저트");
            Tag tag3 = createTestTag("브런치");
            
            savedStore.addTag(tag1); // Store에 Tag 추가
            savedStore.addTag(tag2); // Store에 Tag 추가
            savedStore.addTag(tag3); // Store에 Tag 추가
            storeRepository.save(savedStore); // Store 저장

            // when
            Store store = storeQueryService.getStore(new GetStoreByIdQuery(savedStore.getId())); // 영화 조회
            List<StoreTag> storeTags = store.getStoreTags(); // StoreTag 목록 조회

            // then
            assertThat(storeTags).hasSize(3);
            assertThat(storeTags).extracting(st -> st.getTag().getName())
                .contains("커피", "디저트", "브런치");
            
            System.out.println("✅ StoreTag 조회 성공 - Store ID: " + store.getId());
            System.out.println("   - StoreTag 개수: " + storeTags.size());
            storeTags.forEach(st -> {
                System.out.println("     * StoreTag ID: " + st.getId() + 
                    ", Store ID: " + st.getStore().getId() + 
                    ", Tag ID: " + st.getTag().getId() + 
                    ", Tag 이름: " + st.getTag().getName());
            });
        }
    }

    @Nested
    @DisplayName("StoreTag 빈 목록 조회 테스트")
    class GetEmptyStoreTagsTest {
        
        @Test
        @DisplayName("조회 성공 - 태그가 없는 Store의 StoreTag 목록 조회")
        @Transactional
        @Rollback(value = false)
        public void shouldGetEmptyStoreTags() {
            // given
            String uniqueLoginId = "test_owner_empty_" + System.currentTimeMillis();
            Owner savedOwner = createTestOwner(uniqueLoginId, "테스트 사업자 빈", "01077778888", "새싹");
            Category category = createTestCategory("식당");
            
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("태그 없는 가게")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("/images/empty_test.jpg")
                    .isOpen(true)
                    .build()
            );

            // when
            Store store = storeQueryService.getStore(new GetStoreByIdQuery(savedStore.getId()));
            List<StoreTag> storeTags = store.getStoreTags();

            // then
            assertThat(storeTags).isEmpty();
            
            System.out.println("✅ 빈 StoreTag 목록 조회 성공 - Store ID: " + store.getId());
            System.out.println("   - StoreTag 개수: " + storeTags.size());
        }
    }

    @Nested
    @DisplayName("여러 Store의 StoreTag 조회 테스트")
    class GetStoreTagsFromMultipleStoresTest {
        
        @Test
        @DisplayName("조회 성공 - 여러 Store의 StoreTag 조회")
        @Transactional
        @Rollback(value = false)
        public void shouldGetStoreTagsFromMultipleStores() {
            // given
            long timestamp = System.currentTimeMillis();
            Owner savedOwner1 = createTestOwner("test_owner_multi1_" + timestamp, "테스트 사업자 다중1", "01011111111", "새싹");
            Owner savedOwner2 = createTestOwner("test_owner_multi2_" + timestamp, "테스트 사업자 다중2", "01022222222", "새싹");
            Category category = createTestCategory("식당");
            
            Tag sharedTag = createTestTag("공통태그");
            Tag tag1 = createTestTag("가게1전용");
            Tag tag2 = createTestTag("가게2전용");
            
            Store store1 = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner1)
                    .category(category)
                    .name("가게 1")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("https://onharu.com/images/store1.jpg")
                    .isOpen(true)
                    .build()
            );
            
            Store store2 = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner2)
                    .category(category)
                    .name("가게 2")
                    .address("서울시 서초구")
                    .phone("0298765432")
                    .image("/images/store2.jpg")
                    .isOpen(true)
                    .build()
            );
            
            // store1에 태그 추가
            store1.addTag(sharedTag);
            store1.addTag(tag1);
            storeRepository.save(store1);
            
            // store2에 태그 추가
            store2.addTag(sharedTag);
            store2.addTag(tag2);
            storeRepository.save(store2);

            // when
            Store fetchedStore1 = storeQueryService.getStore(new GetStoreByIdQuery(store1.getId()));
            Store fetchedStore2 = storeQueryService.getStore(new GetStoreByIdQuery(store2.getId()));
            
            List<StoreTag> storeTags1 = fetchedStore1.getStoreTags();
            List<StoreTag> storeTags2 = fetchedStore2.getStoreTags();

            // then
            assertThat(storeTags1).hasSize(2);
            assertThat(storeTags2).hasSize(2);
            
            // 공통 태그가 두 가게 모두에 있는지 확인
            assertThat(storeTags1).extracting(st -> st.getTag().getName())
                .contains("공통태그", "가게1전용");
            assertThat(storeTags2).extracting(st -> st.getTag().getName())
                .contains("공통태그", "가게2전용");
            
            System.out.println("✅ 여러 Store의 StoreTag 조회 성공");
            System.out.println("   - 가게 1 (ID: " + fetchedStore1.getId() + ") StoreTag 개수: " + storeTags1.size());
            storeTags1.forEach(st -> {
                System.out.println("     * StoreTag ID: " + st.getId() + ", Tag: " + st.getTag().getName());
            });
            System.out.println("   - 가게 2 (ID: " + fetchedStore2.getId() + ") StoreTag 개수: " + storeTags2.size());
            storeTags2.forEach(st -> {
                System.out.println("     * StoreTag ID: " + st.getId() + ", Tag: " + st.getTag().getName());
            });
        }
    }
}
