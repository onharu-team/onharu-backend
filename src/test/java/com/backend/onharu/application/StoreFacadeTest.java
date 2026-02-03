package com.backend.onharu.application;

import static com.backend.onharu.domain.support.error.ErrorType.Store.STORE_OWNER_MISMATCH;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import com.backend.onharu.domain.level.model.Level;
import org.junit.jupiter.api.Assertions;
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
import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.DeleteStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.UpdateStoreCommand;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.model.StoreTag;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("StoreFacade 단위 테스트")
class StoreFacadeTest {

    @Autowired
    private StoreFacade storeFacade;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private TagJpaRepository tagJpaRepository;

    @BeforeEach
    public void setUp() {
        // 외래 키 제약 조건을 고려한 삭제 순서 (자식 → 부모)
        storeJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        tagJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    /**
     * 테스트용 User 생성 헬퍼 메서드 (사업자용)
     */
    private User createTestUserForOwner(String loginId, String name, String phone) {
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
     * 테스트용 Owner 생성 헬퍼 메서드 (User와 함께 생성)
     */
    private Owner createTestOwner(String loginId, String name, String phone, String levelName, String businessNumber) {
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
     * 테스트용 Category 생성 헬퍼 메서드
     */
    private Category createTestCategory(String name) {
        return categoryJpaRepository.save(Category.builder().name(name).build());
    }

    /**
     * 테스트용 Store 생성 헬퍼 메서드
     */
    private Store createTestStore(String name, Owner owner, Category category) {
        return storeJpaRepository.save(Store.builder()
            .name(name)
            .owner(owner)
            .category(category)
            .address("서울시 강남구")
            .phone("0212345678")
            .image("/images/test.jpg")
            .isOpen(true)
            .build());
    }

    @Nested
    @DisplayName("가게 단건 조회 테스트")
    class GetStoreTest {
        
        @Test
        @DisplayName("가게 단건 조회 성공")
        @Rollback(value = false)
        public void shouldGetStore() {
            // given
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner("test_owner_get_store", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);

            // when
            Store result = storeFacade.getStore(store.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(store.getId());
            assertThat(result.getName()).isEqualTo("테스트 가게");
            assertThat(result.getOwner().getId()).isEqualTo(owner.getId());
            assertThat(result.getCategory().getId()).isEqualTo(category.getId());
            
            System.out.println("✅ 가게 단건 조회 성공");
            System.out.println("   - 가게 ID: " + result.getId());
            System.out.println("   - 가게 이름: " + result.getName());
        }
    }

    @Nested
    @DisplayName("가게 목록 조회 테스트")
    class GetStoresTest {
        
        @Test
        @DisplayName("가게 목록 조회 성공")
        @Rollback(value = false)
        public void shouldGetStores() {
            // given
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner("test_owner_get_stores", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store1 = createTestStore("테스트 가게1", owner, category);
            Store store2 = createTestStore("테스트 가게2", owner, category);

            // when
            List<Store> stores = storeFacade.getStores(owner.getId());

            // then
            assertThat(stores).isNotNull();
            assertThat(stores.size()).isEqualTo(2);
            assertThat(stores).allMatch(s -> s.getOwner().getId().equals(owner.getId()));
            assertThat(stores).extracting(Store::getId).contains(store1.getId(), store2.getId());
            
            System.out.println("✅ 가게 목록 조회 성공");
            System.out.println("   - 사업자 ID: " + owner.getId());
            System.out.println("   - 가게 개수: " + stores.size());
        }

        @Test
        @DisplayName("가게가 없을 때 빈 목록 반환")
        public void shouldReturnEmptyListWhenNoStores() {
            // given
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner("test_owner_empty_stores", "테스트 사업자", "01012345678", "새싹", "1234567890");

            // when
            List<Store> stores = storeFacade.getStores(owner.getId());

            // then
            assertThat(stores).isNotNull();
            assertThat(stores).isEmpty();
        }
    }

    @Nested
    @DisplayName("가게 등록 테스트")
    class CreateStoreTest {
        
        @Test
        @DisplayName("가게 등록 성공")
        @Rollback(value = false)
        public void shouldCreateStore() {
            // given
            String uniqueLoginId = "test_owner_create_" + UUID.randomUUID().toString().substring(0, 8);
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner(uniqueLoginId, "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            CreateStoreCommand command = new CreateStoreCommand(
                owner.getId(),
                category.getId(),
                "새로운 가게",
                "서울시 강남구 테헤란로",
                "0212345678",
                "37.5665",
                "126.9780",
                "/images/new_store.jpg",
                "맛있는 음식을 제공하는 가게입니다.",
                "따뜻한 식당",
                List.of(), // 태그 없음
                List.of() // 영업시간 없음
            );

            // when
            Store store = storeFacade.createStore(command, owner.getId());

            // then
            assertThat(store).isNotNull();
            assertThat(store.getId()).isNotNull();
            assertThat(store.getName()).isEqualTo("새로운 가게");
            assertThat(store.getAddress()).isEqualTo("서울시 강남구 테헤란로");
            assertThat(store.getPhone()).isEqualTo("0212345678");
            assertThat(store.getOwner().getId()).isEqualTo(owner.getId());
            assertThat(store.getCategory().getId()).isEqualTo(category.getId());
            assertThat(store.getIsOpen()).isFalse(); // 가게 생성 시 영업 상태는 false
            
            System.out.println("✅ 가게 등록 성공");
            System.out.println("   - 가게 ID: " + store.getId());
            System.out.println("   - 가게 이름: " + store.getName());
            System.out.println("   - 영업 상태: " + store.getIsOpen());
        }

        @Test
        @DisplayName("가게 등록 성공 - 태그와 함께 생성")
        @Transactional
        @Rollback(value = false)
        public void shouldCreateStoreWithTags() {
            // given
            String uniqueLoginId = "test_owner_tags_" + UUID.randomUUID().toString().substring(0, 8);
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner(uniqueLoginId, "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            CreateStoreCommand command = new CreateStoreCommand(
                owner.getId(),
                category.getId(),
                "태그 있는 가게",
                "서울시 강남구 테헤란로",
                "0212345678",
                "37.5665",
                "126.9780",
                "/images/tagged_store.jpg",
                "태그가 있는 가게입니다.",
                "태그 가게",
                List.of("커피", "디저트", "브런치"), // 태그 이름 목록
                List.of() // 영업시간 없음
            );

            // when
            Store store = storeFacade.createStore(command, owner.getId());

            // then
            assertThat(store).isNotNull();
            assertThat(store.getId()).isNotNull();
            assertThat(store.getName()).isEqualTo("태그 있는 가게");
            
            // 태그 확인 (트랜잭션 내에서 조회)
            List<StoreTag> storeTags = store.getStoreTags();
            assertThat(storeTags).isNotNull();
            assertThat(storeTags.size()).isEqualTo(3);
            assertThat(storeTags).extracting(st -> st.getTag().getName())
                .containsExactlyInAnyOrder("커피", "디저트", "브런치");
            
            // 태그가 DB에 저장되었는지 확인 (StoreQueryService를 통해 다시 조회)
            Store savedStore = storeFacade.getStore(store.getId());
            assertThat(savedStore).isNotNull();
            assertThat(savedStore.getStoreTags().size()).isEqualTo(3);
            assertThat(savedStore.getStoreTags()).extracting(st -> st.getTag().getName())
                .containsExactlyInAnyOrder("커피", "디저트", "브런치");
            
            System.out.println("✅ 가게 등록 성공 (태그 포함)");
            System.out.println("   - 가게 ID: " + store.getId());
            System.out.println("   - 가게 이름: " + store.getName());
            System.out.println("   - 태그 개수: " + storeTags.size());
            storeTags.forEach(st -> {
                System.out.println("     - 태그: " + st.getTag().getName() + " (ID: " + st.getTag().getId() + ")");
            });
        }

        @Test
        @DisplayName("가게 등록 성공 - 기존 태그 재사용")
        @Transactional
        @Rollback(value = false)
        public void shouldCreateStoreWithExistingTags() {
            // given
            String uniqueLoginId = "test_owner_reuse_" + UUID.randomUUID().toString().substring(0, 8);
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner(uniqueLoginId, "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            
            // 기존 태그 생성
            Tag existingTag = tagJpaRepository.save(
                Tag.builder()
                    .name("커피")
                    .build()
            );
            
            CreateStoreCommand command = new CreateStoreCommand(
                owner.getId(),
                category.getId(),
                "기존 태그 사용 가게",
                "서울시 강남구 테헤란로",
                "0212345678",
                "37.5665",
                "126.9780",
                "/images/reuse_tags.jpg",
                "기존 태그를 재사용하는 가게입니다.",
                "태그 재사용",
                List.of("커피", "새로운태그"), // 기존 태그 "커피"와 새로운 태그 "새로운태그"
                List.of() // 영업시간 없음
            );

            // when
            Store store = storeFacade.createStore(command, owner.getId());

            // then
            assertThat(store).isNotNull();
            assertThat(store.getId()).isNotNull();
            
            // 태그 확인 (트랜잭션 내에서 조회)
            List<StoreTag> storeTags = store.getStoreTags();
            assertThat(storeTags).isNotNull();
            assertThat(storeTags.size()).isEqualTo(2);
            
            // 기존 태그가 재사용되었는지 확인 (같은 Tag 엔티티 사용)
            StoreTag coffeeTag = storeTags.stream()
                .filter(st -> st.getTag().getName().equals("커피"))
                .findFirst()
                .orElse(null);
            assertThat(coffeeTag).isNotNull();
            assertThat(coffeeTag.getTag().getId()).isEqualTo(existingTag.getId()); // 기존 태그 재사용
            
            // 새로운 태그가 생성되었는지 확인
            StoreTag newTag = storeTags.stream()
                .filter(st -> st.getTag().getName().equals("새로운태그"))
                .findFirst()
                .orElse(null);
            assertThat(newTag).isNotNull();
            assertThat(newTag.getTag().getId()).isNotEqualTo(existingTag.getId()); // 새로운 태그
            
            // DB에서 다시 조회하여 확인
            Store savedStore = storeFacade.getStore(store.getId());
            assertThat(savedStore.getStoreTags().size()).isEqualTo(2);
            
            System.out.println("✅ 가게 등록 성공 (기존 태그 재사용)");
            System.out.println("   - 가게 ID: " + store.getId());
            System.out.println("   - 태그 개수: " + storeTags.size());
            System.out.println("   - 기존 태그 재사용: 커피 (ID: " + existingTag.getId() + ")");
            System.out.println("   - 새 태그 생성: 새로운태그 (ID: " + newTag.getTag().getId() + ")");
        }
    }

    @Nested
    @DisplayName("가게 정보 수정 테스트")
    class UpdateStoreTest {
        
        @Test
        @DisplayName("가게 정보 수정 성공")
        @Transactional
        public void shouldUpdateStore() {
            // given
            String uniqueLoginId = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner(uniqueLoginId, "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category1 = createTestCategory("식당");
            Category category2 = createTestCategory("카페");
            Store store = createTestStore("테스트 가게", owner, category1);
            
            UpdateStoreCommand command = new UpdateStoreCommand(
                store.getId(),
                category2.getId(),
                "/images/updated.jpg",
                "0298765432",
                "서울시 서초구",
                "37.4837",
                "127.0324",
                "업데이트된 가게 소개입니다.",
                "업데이트된 한줄 소개",
                true,
                List.of(), // 태그 없음
                List.of() // 영업시간 없음
            );

            // when
            storeFacade.updateStore(command, owner.getId());

            // then
            Store updatedStore = storeJpaRepository.findById(store.getId()).orElse(null);
            assertThat(updatedStore).isNotNull();
            assertThat(updatedStore.getCategory().getId()).isEqualTo(category2.getId());
            assertThat(updatedStore.getImage()).isEqualTo("/images/updated.jpg");
            assertThat(updatedStore.getPhone()).isEqualTo("0298765432");
            assertThat(updatedStore.getAddress()).isEqualTo("서울시 서초구");
            assertThat(updatedStore.getIntroduction()).isEqualTo("업데이트된 가게 소개입니다.");
            assertThat(updatedStore.getIntro()).isEqualTo("업데이트된 한줄 소개");
            assertThat(updatedStore.getIsOpen()).isTrue();
            
            System.out.println("✅ 가게 정보 수정 성공");
            System.out.println("   - 가게 ID: " + updatedStore.getId());
            System.out.println("   - 카테고리: " + updatedStore.getCategory().getName());
        }

        @Test
        @DisplayName("다른 사업자의 가게 수정 시 예외 발생")
        @Transactional
        public void shouldThrowExceptionWhenStoreBelongsToOtherOwner() {
            // given
            String uniqueLoginId1 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            String uniqueLoginId2 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            Level level = createTestLevel("새싹");
            Owner owner1 = createTestOwner(uniqueLoginId1, "테스트 사업자1", "01012345678", "새싹", "1234567890");
            Owner owner2 = createTestOwner(uniqueLoginId2, "테스트 사업자2", "01087654321", "새싹", "2234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner1, category);
            
            UpdateStoreCommand command = new UpdateStoreCommand(
                store.getId(),
                category.getId(),
                "/images/updated.jpg",
                "0298765432",
                "서울시 서초구",
                "37.4837",
                "127.0324",
                "업데이트된 가게 소개입니다.",
                "업데이트된 한줄 소개",
                true,
                List.of(), // 태그 없음
                List.of() // 영업시간 없음
            );

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> storeFacade.updateStore(command, owner2.getId())
            );
            
            assertThat(exception.getErrorType()).isEqualTo(STORE_OWNER_MISMATCH);
        }
    }

    @Nested
    @DisplayName("가게 카테고리 수정 테스트")
    class UpdateCategoryTest {
        
        @Test
        @DisplayName("가게 카테고리 수정 성공")
        @Transactional
        public void shouldUpdateCategory() {
            // given
            String uniqueLoginId1 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner(uniqueLoginId1, "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category1 = createTestCategory("식당");
            Category category2 = createTestCategory("카페");
            Store store = createTestStore("테스트 가게", owner, category1);

            // when
            storeFacade.updateCategory(store.getId(), category2.getId(), owner.getId());

            // then
            Store updatedStore = storeJpaRepository.findById(store.getId()).orElse(null);
            assertThat(updatedStore).isNotNull();
            assertThat(updatedStore.getCategory().getId()).isEqualTo(category2.getId());
            assertThat(updatedStore.getCategory().getName()).isEqualTo("카페");
            
            System.out.println("✅ 가게 카테고리 수정 성공");
            System.out.println("   - 가게 ID: " + updatedStore.getId());
            System.out.println("   - 변경 전 카테고리: " + category1.getName());
            System.out.println("   - 변경 후 카테고리: " + updatedStore.getCategory().getName());
        }

        @Test
        @DisplayName("다른 사업자의 가게 카테고리 수정 시 예외 발생")
        public void shouldThrowExceptionWhenStoreBelongsToOtherOwner() {
            // given
            String uniqueLoginId1 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            String uniqueLoginId2 = "owner_" + UUID.randomUUID().toString().substring(0, 8);
            Level level = createTestLevel("새싹");
            Owner owner1 = createTestOwner(uniqueLoginId1, "테스트 사업자1", "01012345678", "새싹", "1234567890");
            Owner owner2 = createTestOwner(uniqueLoginId2, "테스트 사업자2", "01087654321", "새싹", "2234567890");
            Category category1 = createTestCategory("식당");
            Category category2 = createTestCategory("카페");
            Store store = createTestStore("테스트 가게", owner1, category1);

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> storeFacade.updateCategory(store.getId(), category2.getId(), owner2.getId())
            );
            
            assertThat(exception.getErrorType()).isEqualTo(STORE_OWNER_MISMATCH);
        }
    }

    @Nested
    @DisplayName("가게 삭제 테스트")
    class DeleteStoreTest {
        
        @Test
        @DisplayName("가게 삭제 성공")
        @Rollback(value = false)
        public void shouldDeleteStore() {
            // given
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner("test_owner_delete_store", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);

            // when
            storeFacade.deleteStore(new DeleteStoreCommand(store.getId()), owner.getId());

            // then
            boolean exists = storeJpaRepository.existsById(store.getId());
            assertThat(exists).isFalse();
            
            System.out.println("✅ 가게 삭제 성공");
            System.out.println("   - 삭제된 가게 ID: " + store.getId());
        }

        @Test
        @DisplayName("다른 사업자의 가게 삭제 시 예외 발생")
        public void shouldThrowExceptionWhenStoreBelongsToOtherOwner() {
            // given
            Level level = createTestLevel("새싹");
            Owner owner1 = createTestOwner("test_owner1_delete", "테스트 사업자1", "01012345678", "새싹", "1234567890");
            Owner owner2 = createTestOwner("test_owner2_delete", "테스트 사업자2", "01087654321", "새싹", "2234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner1, category);

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> storeFacade.deleteStore(new DeleteStoreCommand(store.getId()), owner2.getId())
            );
            
            assertThat(exception.getErrorType()).isEqualTo(STORE_OWNER_MISMATCH);
        }
    }

    @Nested
    @DisplayName("가게 영업 상태 변경 테스트")
    class ChangeOpenStatusTest {
        
        @Test
        @DisplayName("가게 영업 상태 변경 성공 - 영업 중으로 변경")
        @Rollback(value = false)
        public void shouldChangeOpenStatusToOpen() {
            // given
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner("test_owner_change_status", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            // 초기 상태를 false로 설정
            store.changeOpenStatus(false);
            storeJpaRepository.save(store);

            // when
            storeFacade.changeOpenStatus(store.getId(), true, owner.getId());

            // then
            Store updatedStore = storeJpaRepository.findById(store.getId()).orElse(null);
            assertThat(updatedStore).isNotNull();
            assertThat(updatedStore.getIsOpen()).isTrue();
            
            System.out.println("✅ 가게 영업 상태 변경 성공 (영업 중)");
            System.out.println("   - 가게 ID: " + updatedStore.getId());
            System.out.println("   - 영업 상태: " + updatedStore.getIsOpen());
        }

        @Test
        @DisplayName("가게 영업 상태 변경 성공 - 영업 종료로 변경")
        @Rollback(value = false)
        public void shouldChangeOpenStatusToClosed() {
            // given
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner("test_owner_change_status_closed", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            // 초기 상태를 true로 설정
            store.changeOpenStatus(true);
            storeJpaRepository.save(store);

            // when
            storeFacade.changeOpenStatus(store.getId(), false, owner.getId());

            // then
            Store updatedStore = storeJpaRepository.findById(store.getId()).orElse(null);
            assertThat(updatedStore).isNotNull();
            assertThat(updatedStore.getIsOpen()).isFalse();
            
            System.out.println("✅ 가게 영업 상태 변경 성공 (영업 종료)");
            System.out.println("   - 가게 ID: " + updatedStore.getId());
            System.out.println("   - 영업 상태: " + updatedStore.getIsOpen());
        }

        @Test
        @DisplayName("다른 사업자의 가게 영업 상태 변경 시 예외 발생")
        public void shouldThrowExceptionWhenStoreBelongsToOtherOwner() {
            // given
            Level level = createTestLevel("새싹");
            Owner owner1 = createTestOwner("test_owner1_status", "테스트 사업자1", "01012345678", "새싹", "1234567890");
            Owner owner2 = createTestOwner("test_owner2_status", "테스트 사업자2", "01087654321", "새싹", "2234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner1, category);

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> storeFacade.changeOpenStatus(store.getId(), true, owner2.getId())
            );
            
            assertThat(exception.getErrorType()).isEqualTo(STORE_OWNER_MISMATCH);
        }
    }
}
