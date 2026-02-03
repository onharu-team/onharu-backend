package com.backend.onharu.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.backend.onharu.domain.level.model.Level;
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
import com.backend.onharu.domain.store.dto.StoreCommand.ChangeOpenStatusCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.UpdateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("StoreCommandService 단위 테스트")
class StoreCommandServiceTest {

    @Autowired
    private StoreCommandService storeCommandService;

    @Autowired
    private StoreQueryService storeQueryService;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @BeforeEach
    public void setUp() {
        // 외래 키 제약 조건을 고려한 삭제 순서 (자식 → 부모)
        reservationJpaRepository.deleteAll();
        storeScheduleJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        childJpaRepository.deleteAll();
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
    @DisplayName("가게 생성 테스트")
    class CreateStoreTest {
        
        @Test
        @DisplayName("가게 생성 성공")
        @Rollback(value = false)
        public void shouldCreateStore() {
            // given
            Owner savedOwner = createTestOwner("test_owner", "테스트 사업자", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            List<String> tagNames = List.of("테스트 태그1", "테스트 태그2");
            
            CreateStoreCommand command = new CreateStoreCommand(
                savedOwner.getId(),
                category.getId(),
                "따뜻한 식당",
                "서울시 강남구 테헤란로 123",
                "0212345678",
                "37.5665",
                "126.9780",
                "https://onharu.com/images/store1.jpg",
                "따뜻한 마음으로 환영합니다!",
                "따뜻한 한 끼 식사",
                tagNames,
                List.of()
            );

            // when
            // 가게 생성
            Store store = storeCommandService.createStore(
                command,
                savedOwner,
                category
            );

            // then
            assertThat(store).isNotNull();
            assertThat(store.getId()).isNotNull();
            assertThat(store.getName()).isEqualTo("따뜻한 식당");
            assertThat(store.getAddress()).isEqualTo("서울시 강남구 테헤란로 123");
            assertThat(store.getIsOpen()).isFalse();
            
            // DB에 저장되었는지 확인
            Store savedStore = storeQueryService.getStore(
                new GetStoreByIdQuery(store.getId())
            );
            assertThat(savedStore).isNotNull();
            
            System.out.println("✅ 가게 생성 성공 - Store ID: " + store.getId());
            System.out.println("   - 가게명: " + store.getName());
            System.out.println("   - 주소: " + store.getAddress());
            System.out.println("   - 사업자 ID: " + savedOwner.getId());
            System.out.println("   - 영업 여부: " + store.getIsOpen());
        }
    }

    @Nested
    @DisplayName("가게 정보 수정 테스트")
    class UpdateStoreTest {
        
        @Test
        @DisplayName("가게 정보 수정 성공")
        @Rollback(value = false)
        public void shouldUpdateStore() {
            // given
            Owner savedOwner = createTestOwner("test_owner2", "테스트 사업자2", "01087654321", "새싹", "0987654321");
            Category category1 = createTestCategory("식당");
            Category category2 = createTestCategory("카페");
            
            // 기존 가게 생성
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category1)
                    .name("기존 가게")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("/images/old.jpg")
                    .introduction("기존 소개")
                    .intro("기존 한줄 소개")                
                    .isOpen(false)
                    .build()
            );
            
            // 가게 정보 수정 Command 생성
            UpdateStoreCommand command = new UpdateStoreCommand(
                savedStore.getId(),
                category2.getId(),
                "/images/new.jpg",
                "0298765432",
                "서울시 서초구",
                "37.4838",
                "127.0324",
                "새로운 소개",
                "새로운 한줄 소개",
                false,
                List.of(),
                List.of()
            );

            // when            
            storeCommandService.updateStore(command, category2); // 가게 정보 수정

            // then
            Store updatedStore = storeQueryService.getStore(
                new GetStoreByIdQuery(savedStore.getId())
            ); // 가게 정보 수정 후 조회

            assertThat(updatedStore.getImage()).isEqualTo("/images/new.jpg");
            assertThat(updatedStore.getPhone()).isEqualTo("0298765432");
            assertThat(updatedStore.getAddress()).isEqualTo("서울시 서초구");
            assertThat(updatedStore.getIsOpen()).isFalse();
            
            System.out.println("✅ 가게 정보 수정 성공 - Store ID: " + updatedStore.getId());
            System.out.println("   - 수정된 이미지: " + updatedStore.getImage());
            System.out.println("   - 수정된 전화번호: " + updatedStore.getPhone());
            System.out.println("   - 수정된 주소: " + updatedStore.getAddress());
            System.out.println("   - 수정된 영업 여부: " + updatedStore.getIsOpen());
        }
    }

    @Nested
    @DisplayName("가게 영업 상태 변경 테스트")
    class ChangeOpenStatusTest {
        
        @Test
        @DisplayName("가게 영업 상태 변경 성공")
        @Rollback(value = false)
        public void shouldChangeOpenStatus() {
            // given
            Owner savedOwner = createTestOwner("test_owner3", "테스트 사업자3", "01011112222", "새싹", "1111222233");
            Category category = createTestCategory("식당");
            
            // 기존 가게 생성
            Store savedStore = storeJpaRepository.save(
                Store.builder()
                    .owner(savedOwner)
                    .category(category)
                    .name("영업 상태 테스트 가게")
                    .address("서울시 강남구")
                    .phone("0212345678")
                    .image("/images/test.jpg")
                    .isOpen(false)
                    .build()
            );

            // when
            storeCommandService.changeOpenStatus(
                new ChangeOpenStatusCommand(savedStore.getId(), true)
            ); // 가게 영업 상태 변경

            // then
            Store updatedStore = storeQueryService.getStore(
                new GetStoreByIdQuery(savedStore.getId())
            ); // 가게 영업 상태 변경 후 조회
            assertThat(updatedStore.getIsOpen()).isTrue();
            
            System.out.println("✅ 가게 영업 상태 변경 성공 - Store ID: " + updatedStore.getId());
            System.out.println("   - 변경 전 상태: true");
            System.out.println("   - 변경 후 상태: " + updatedStore.getIsOpen());
        }
    }
}
