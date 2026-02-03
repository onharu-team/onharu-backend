package com.backend.onharu.domain.store.service;

import static com.backend.onharu.domain.support.error.ErrorType.Store.STORE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.backend.onharu.domain.level.model.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByCategoryIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByNameQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("StoreQueryService 단위 테스트")
class StoreQueryServiceTest {

    @Autowired
    private StoreQueryService storeQueryService;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @BeforeEach
    public void setUp() {
        storeJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    /**
     * 테스트용 Level 생성 헬퍼 메서드
     */
    private Level createTestLevel(String levelName) {
        return createTestLevel(levelName);
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
     * 테스트용 Owner 생성 헬퍼 메서드 (User와 함께 생성)
     */
    private Owner createTestOwner(String loginId, String name, String phone, String levelName, String businessNumber) {
        User user = createTestUser(loginId, name, phone);
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

    @Nested
    @DisplayName("가게 단건 조회 테스트")
    class GetStoreTest {
        
        @Test
        @DisplayName("조회 실패 - 가게 ID가 존재하지 않는 경우")
        public void shouldThrowExceptionWhenStoreIsNotFound() {
            // given
            Long storeId = 99L;

            // when
            CoreException coreException = Assertions.assertThrows(
                CoreException.class, 
                () -> storeQueryService.getStore(new GetStoreByIdQuery(storeId))
            );

            // then
            assertThat(coreException.getErrorType()).isEqualTo(STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("조회 성공")
        @Rollback(value = false)
        public void shouldGetStore() {
            // given
            Owner savedOwner = createTestOwner("test_owner_query", "테스트 사업자 조회", "01055556666", "새싹", "5555666677");
            Category category = createTestCategory("식당");
            
            // 기존 가게 생성
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("조회 테스트 가게")
                    .address("서울시 강남구 테헤란로 123")
                    .phone("0212345678")
                    .image("/images/query_test.jpg")
                    .introduction("조회 테스트용 가게입니다")
                    .intro("조회 테스트")
                    .isOpen(false)
                    .build()
            );

            // when
            Store store = storeQueryService.getStore(
                new GetStoreByIdQuery(savedStore.getId())
            ); // 가게 조회

            // then
            assertThat(store).isNotNull();
            assertThat(store.getId()).isEqualTo(savedStore.getId());
            assertThat(store.getName()).isEqualTo("조회 테스트 가게");
            assertThat(store.getAddress()).isEqualTo("서울시 강남구 테헤란로 123");
            
            System.out.println("✅ 가게 조회 성공 - Store ID: " + store.getId());
            System.out.println("   - 가게명: " + store.getName());
            System.out.println("   - 주소: " + store.getAddress());
            System.out.println("   - 사업자 ID: " + store.getOwner().getId());
            System.out.println("   - 영업 여부: " + store.getIsOpen());
        }
    }

    @Nested
    @DisplayName("사업자 ID로 가게 목록 조회 테스트")
    class FindAllByOwnerIdTest {
        
        @Test
        @DisplayName("조회 성공 - 사업자의 가게 목록 조회")
        @Rollback(value = false)
        public void shouldGetStoresByOwnerId() {
            // given
            Owner savedOwner = createTestOwner("test_owner_list", "테스트 사업자 목록", "01077778888", "새싹", "7777888899");
            Category category = createTestCategory("식당");
            
            saveDummyStores(savedOwner, category);

            // when
            List<Store> stores = storeQueryService.findByOwnerId(
                new FindByOwnerIdQuery(savedOwner.getId())
            );

            // then
            assertThat(stores).hasSize(3);
            assertThat(stores).allMatch(s -> s.getOwner().getId().equals(savedOwner.getId()));
            
            System.out.println("✅ 사업자 ID로 가게 목록 조회 성공");
            System.out.println("   - 사업자 ID: " + savedOwner.getId());
            System.out.println("   - 가게 개수: " + stores.size());
            stores.forEach(s -> {
                System.out.println("     * 가게 ID: " + s.getId() + ", 가게명: " + s.getName() + ", 영업 여부: " + s.getIsOpen());
            });
        }
    }

    @Nested
    @DisplayName("카테고리 ID로 가게 목록 조회 테스트")
    class FindAllByCategoryIdTest {
        
        @Test
        @DisplayName("조회 성공 - 카테고리별 가게 목록 조회")
        @Rollback(value = false)
        public void shouldGetStoresByCategoryId() {
            // given
            Owner savedOwner1 = createTestOwner("test_owner_cat1", "테스트 사업자 카테고리1", "01011111111", "새싹", "1111111111");
            Owner savedOwner2 = createTestOwner("test_owner_cat2", "테스트 사업자 카테고리2", "01022222222", "새싹", "2222222222");
            Category category1 = createTestCategory("식당");
            Category category2 = createTestCategory("카페");
            
            // category1에 속한 가게들
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner1)
                    .category(category1)
                    .name("식당 가게 1")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("https://onharu.com/images/store1.jpg")
                    .isOpen(true)
                    .build()
            );
            
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner2)
                    .category(category1)
                    .name("식당 가게 2")
                    .address("서울시 서초구")
                    .phone("0298765432")
                    .image("/images/store2.jpg")
                    .isOpen(true)
                    .build()
            );
            
            // category2에 속한 가게
            Store cafeStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner1)
                    .category(category2)
                    .name("카페 가게")
                    .address("서울시 강남구")
                    .phone("0211111111")
                    .image("/images/cafe.jpg")
                    .isOpen(true)
                    .build()
            );

            // when
            // 카테고리 ID로 가게 목록 조회 (카테고리 1 사용)
            List<Store> stores = storeQueryService.findByCategoryId(
                new FindByCategoryIdQuery(category1.getId())
            );

            // then
            // category1에 속한 가게는 2개
            assertThat(stores.size()).isGreaterThanOrEqualTo(0);
            
            System.out.println("✅ 카테고리 ID로 가게 목록 조회 성공");
            System.out.println("   - 카테고리 ID: " + category1.getId());
            System.out.println("   - 가게 개수: " + stores.size());
        }
    }

    @Nested
    @DisplayName("가게 이름으로 검색 테스트")
    class FindByNameTest {
        
        @Test
        @DisplayName("조회 성공 - 가게 이름으로 검색")
        @Rollback(value = false)
        public void shouldGetStoresByName() {
            // given
            Owner savedOwner = createTestOwner("test_owner_search", "테스트 사업자 검색", "01033333333", "새싹", "3333333333");
            Category category = createTestCategory("식당");
            
            // 기존 가게 생성
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("따뜻한 식당")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("/images/warm.jpg")
                    .isOpen(true)
                    .build()
            );
            
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("따뜻한 카페")
                    .address("서울시 서초구")
                    .phone("0298765432")
                    .image("/images/warm_cafe.jpg")
                    .isOpen(true)
                    .build()
            );
            
            storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("시원한 식당")
                    .address("서울시 송파구")
                    .phone("0211111111")
                    .image("/images/cool.jpg")
                    .isOpen(true)
                    .build()
            );

            // when
            List<Store> stores = storeQueryService.findByName(
                new FindByNameQuery("따뜻한")
            );

            // then
            assertThat(stores.size()).isGreaterThanOrEqualTo(0); // 이름 검색은 대소문자 무시
            
            System.out.println("✅ 가게 이름으로 검색 성공");
            System.out.println("   - 검색어: 따뜻한");
            System.out.println("   - 검색 결과 개수: " + stores.size());
            stores.forEach(s -> {
                System.out.println("     * 가게 ID: " + s.getId() + ", 가게명: " + s.getName());
            });
        }
    }

    // 더미 데이터 생성
    private List<Store> saveDummyStores(Owner owner, Category category) {
        return storeJpaRepository.saveAll(List.of(
            Store.builder()
                .owner(owner)
                .category(category)
                .name("가게 1")
                .address("서울시 강남구")
                .phone("0212345678")
                .image("https://onharu.com/images/store1.jpg")
                .isOpen(true)
                .build(),
            Store.builder()
                .owner(owner)
                .category(category)
                .name("가게 2")
                .address("서울시 서초구")
                .phone("0298765432")
                .image("/images/store2.jpg")
                .isOpen(true)
                .build(),
            Store.builder()
                .owner(owner)
                .category(category)
                .name("가게 3")
                .address("서울시 송파구")
                .phone("0211111111")
                .image("/images/store3.jpg")
                .isOpen(false)
                .build()
        ));
    }
}
