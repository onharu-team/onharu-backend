package com.backend.onharu.domain.storeschedule.service;

import static com.backend.onharu.domain.support.error.ErrorType.StoreSchedule.STORE_SCHEDULE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

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
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.CreateStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.DeleteStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.UpdateStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("StoreScheduleCommandService 단위 테스트")
class StoreScheduleCommandServiceTest {

    @Autowired
    private StoreScheduleCommandService storeScheduleCommandService;

    @Autowired
    private StoreScheduleQueryService storeScheduleQueryService;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

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
        storeScheduleJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
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
            .image("/images/schedule_test.jpg")
            .isOpen(true)
            .build());
    }

    @Nested
    @DisplayName("가게 일정 생성 테스트")
    class CreateStoreScheduleTest {
        
        @Test
        @DisplayName("가게 일정 생성 성공")
        @Rollback(value = false)
        public void shouldCreateStoreSchedule() {
            // given
            Owner savedOwner = createTestOwner("test_owner_schedule", "테스트 사업자 일정", "01012345678", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("일정 테스트 가게", savedOwner, category);
            
            LocalDate scheduleDate = LocalDate.now().plusDays(1); // 가게 일정 날짜
            LocalTime startTime = LocalTime.of(14, 0); // 가게 일정 시작 시간
            LocalTime endTime = LocalTime.of(15, 0); // 가게 일정 종료 시간
            Integer maxPeople = 10; // 가게 일정 최대 인원

            // when
            StoreSchedule storeSchedule = storeScheduleCommandService.createStoreSchedule(
                new CreateStoreScheduleCommand(
                    savedStore.getId(),
                    scheduleDate,
                    startTime,
                    endTime,
                    maxPeople
                ),
                savedStore
            );

            // then
            assertThat(storeSchedule).isNotNull();
            assertThat(storeSchedule.getId()).isNotNull();
            assertThat(storeSchedule.getScheduleDate()).isEqualTo(scheduleDate);
            assertThat(storeSchedule.getStartTime()).isEqualTo(startTime);
            assertThat(storeSchedule.getEndTime()).isEqualTo(endTime);
            assertThat(storeSchedule.getMaxPeople()).isEqualTo(maxPeople);
            
            // DB에 저장되었는지 확인
            StoreSchedule savedSchedule = storeScheduleQueryService.getStoreScheduleById(
                new GetStoreScheduleByIdQuery(storeSchedule.getId())
            ); // 가게 일정 조회
            assertThat(savedSchedule).isNotNull();
            assertThat(savedSchedule.getStore().getId()).isEqualTo(savedStore.getId());
            assertThat(savedSchedule.getScheduleDate()).isEqualTo(scheduleDate);
            assertThat(savedSchedule.getStartTime()).isEqualTo(startTime);
            assertThat(savedSchedule.getEndTime()).isEqualTo(endTime);
            assertThat(savedSchedule.getMaxPeople()).isEqualTo(maxPeople);
            
            System.out.println("✅ 가게 일정 생성 성공 - StoreSchedule ID: " + storeSchedule.getId());
            System.out.println("   - 가게 ID: " + savedStore.getId());
            System.out.println("   - 일정 날짜: " + scheduleDate);
            System.out.println("   - 시작 시간: " + startTime);
            System.out.println("   - 종료 시간: " + endTime);
            System.out.println("   - 최대 인원: " + maxPeople);
        }
    }

    @Nested
    @DisplayName("가게 일정 수정 테스트")
    class UpdateStoreScheduleTest {
        
        @Test
        @DisplayName("가게 일정 수정 성공")
        @Rollback(value = false)
        public void shouldUpdateStoreSchedule() {
            // given
            Owner savedOwner = createTestOwner("test_owner_update", "테스트 사업자 수정", "01087654321", "새싹", "0987654321");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("수정 테스트 가게", savedOwner, category);
            
            StoreSchedule storedSchedule = storeScheduleJpaRepository.save(
                StoreSchedule.builder()
                    .store(savedStore)
                    .scheduleDate(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 0))
                    .maxPeople(10)
                    .build()
            );
            
            LocalDate newScheduleDate = LocalDate.now().plusDays(2);
            LocalTime newStartTime = LocalTime.of(16, 0);
            LocalTime newEndTime = LocalTime.of(17, 0);
            Integer newMaxPeople = 15;

            // when
            storeScheduleCommandService.updateStoreSchedule(
                new UpdateStoreScheduleCommand(
                    storedSchedule.getId(),
                    newScheduleDate,
                    newStartTime,
                    newEndTime,
                    newMaxPeople
                )
            );

            // then
            StoreSchedule updatedSchedule = storeScheduleQueryService.getStoreScheduleById(
                new GetStoreScheduleByIdQuery(storedSchedule.getId())
            ); // 수정된 가게 일정 조회
            assertThat(updatedSchedule.getScheduleDate()).isEqualTo(newScheduleDate); // 수정된 일정 날짜
            assertThat(updatedSchedule.getStartTime()).isEqualTo(newStartTime); // 수정된 시작 시간
            assertThat(updatedSchedule.getEndTime()).isEqualTo(newEndTime); // 수정된 종료 시간
            assertThat(updatedSchedule.getMaxPeople()).isEqualTo(newMaxPeople); // 수정된 최대 인원
            
            System.out.println("✅ 가게 일정 수정 성공 - StoreSchedule ID: " + updatedSchedule.getId());
            System.out.println("   - 수정된 일정 날짜: " + updatedSchedule.getScheduleDate());
            System.out.println("   - 수정된 시작 시간: " + updatedSchedule.getStartTime());
            System.out.println("   - 수정된 종료 시간: " + updatedSchedule.getEndTime());
            System.out.println("   - 수정된 최대 인원: " + updatedSchedule.getMaxPeople());
        }
    }

    @Nested
    @DisplayName("가게 일정 삭제 테스트")
    class DeleteStoreScheduleTest {
        
        @Test
        @DisplayName("가게 일정 삭제 성공")
        @Rollback(value = false)
        public void shouldDeleteStoreSchedule() {
            // given
            Owner savedOwner = createTestOwner("test_owner_delete", "테스트 사업자 삭제", "01011112222", "새싹", "1111222233");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("삭제 테스트 가게", savedOwner, category);
            
            StoreSchedule savedSchedule = storeScheduleJpaRepository.save(
                StoreSchedule.builder()
                    .store(savedStore)
                    .scheduleDate(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 0))
                    .maxPeople(10)
                    .build()
            );

            // when
            storeScheduleCommandService.deleteStoreSchedule(
                new DeleteStoreScheduleCommand(savedSchedule.getId())
            ); // 가게 일정 삭제

            // then
            // 삭제되었는지 확인 (조회 시 예외 발생)
            CoreException coreException = Assertions.assertThrows(
                CoreException.class,
                () -> storeScheduleQueryService.getStoreScheduleById(
                    new GetStoreScheduleByIdQuery(savedSchedule.getId())
                )
            );
            assertThat(coreException.getErrorType()).isEqualTo(STORE_SCHEDULE_NOT_FOUND);
        }
    }
}
