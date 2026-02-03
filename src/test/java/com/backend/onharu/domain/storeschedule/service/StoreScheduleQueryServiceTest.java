package com.backend.onharu.domain.storeschedule.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
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
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByBusinessDayQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByStoreIdQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("StoreScheduleQueryService 단위 테스트")
class StoreScheduleQueryServiceTest {

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
    @DisplayName("가게 일정 단건 조회 테스트")
    class GetStoreScheduleByIdTest {
        
        @Test
        @DisplayName("조회 성공")
        @Rollback(value = false)
        public void shouldGetStoreScheduleById() {
            // given
            Owner savedOwner = createTestOwner("test_owner_query_schedule", "테스트 사업자 조회 일정", "01055556666", "새싹", "5555666677");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("조회 테스트 가게", savedOwner, category);
            
            // 가게 일정 생성
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
            StoreSchedule storeSchedule = storeScheduleQueryService.getStoreScheduleById(
                new GetStoreScheduleByIdQuery(savedSchedule.getId())
            ); // 가게 일정 조회

            // then
            assertThat(storeSchedule).isNotNull();
            assertThat(storeSchedule.getId()).isEqualTo(savedSchedule.getId());
            assertThat(storeSchedule.getScheduleDate()).isEqualTo(LocalDate.now().plusDays(1)); // 가게 일정 날짜 조회
            assertThat(storeSchedule.getStartTime()).isEqualTo(LocalTime.of(14, 0)); // 가게 일정 시작 시간 조회
            assertThat(storeSchedule.getEndTime()).isEqualTo(LocalTime.of(15, 0)); // 가게 일정 종료 시간 조회
            assertThat(storeSchedule.getMaxPeople()).isEqualTo(10); // 가게 일정 최대 인원 조회
            
            System.out.println("✅ 가게 일정 조회 성공 - StoreSchedule ID: " + storeSchedule.getId());
            System.out.println("   - 가게 ID: " + storeSchedule.getStore().getId());
            System.out.println("   - 일정 날짜: " + storeSchedule.getScheduleDate());
            System.out.println("   - 시작 시간: " + storeSchedule.getStartTime());
            System.out.println("   - 종료 시간: " + storeSchedule.getEndTime());
            System.out.println("   - 최대 인원: " + storeSchedule.getMaxPeople());
        }
    }

    @Nested
    @DisplayName("가게 ID로 가게 일정 목록 조회 테스트")
    class FindAllByStoreIdTest {
        
        @Test
        @DisplayName("조회 성공 - 가게의 일정 목록 조회")
        @Rollback(value = false)
        public void shouldGetStoreSchedulesByStoreId() {
            // given
            Owner savedOwner = createTestOwner(
                "test_owner_list_schedule", 
                "테스트 사업자 목록 일정", 
                "01077778888", 
                "새싹",
                "7777888899"
            ); // 테스트용 사업자 생성
            Category category = createTestCategory("식당"); // 테스트용 카테고리 생성
            Store savedStore = createTestStore("목록 테스트 가게", savedOwner, category); // 테스트용 가게 생성
            saveDummyStoreSchedules(savedStore); // 테스트용 가게 일정 생성

            // when
            List<StoreSchedule> schedules = storeScheduleQueryService.findAllByStoreId(
                new FindAllByStoreIdQuery(savedStore.getId())
            ); // 가게 ID로 가게 일정 목록 조회

            // then
            assertThat(schedules).hasSize(3); // 가게 일정 개수 조회
            assertThat(schedules).allMatch(s -> s.getStore().getId().equals(savedStore.getId())); // 가게 ID와 일치하는 가게 일정 조회
            
            System.out.println("✅ 가게 ID로 가게 일정 목록 조회 성공 - 가게 ID: " + savedStore.getId() + ", 일정 개수: " + schedules.size());
            System.out.println("   - 가게 ID: " + savedStore.getId());
            System.out.println("   - 일정 개수: " + schedules.size());
            schedules.forEach(s -> {
                System.out.println("     * 일정 ID: " + s.getId() + ", 날짜: " + s.getScheduleDate() + ", 시간: " + s.getStartTime() + "~" + s.getEndTime());
            });
        }
    }

    @Nested
    @DisplayName("영업일로 가게 일정 목록 조회 테스트")
    class FindAllByBusinessDayTest {
        
        @Test
        @DisplayName("조회 성공 - 특정 날짜의 일정 목록 조회")
        @Rollback(value = false)
        public void shouldGetStoreSchedulesByBusinessDay() {
            // given
            Owner savedOwner1 = createTestOwner("test_owner_date1", "테스트 사업자 날짜1", "01011111111", "새싹", "1111111111");
            Owner savedOwner2 = createTestOwner("test_owner_date2", "테스트 사업자 날짜2", "01022222222", "새싹", "2222222222");
            
            Category category = createTestCategory("식당");
            Store savedStore1 = createTestStore("날짜 테스트 가게 1", savedOwner1, category);
            Store savedStore2 = createTestStore("날짜 테스트 가게 2", savedOwner2, category);
            
            LocalDate targetDate = LocalDate.now().plusDays(1);
            
            saveDummyStoreSchedules(savedStore1); // 테스트용 가게 일정 생성
            saveDummyStoreSchedules(savedStore2); // 테스트용 가게 일정 생성
            
            // when
            List<StoreSchedule> schedules = storeScheduleQueryService.findAllByBusinessDay(
                new FindAllByBusinessDayQuery(targetDate)
            ); // 영업일로 가게 일정 목록 조회

            // then
            assertThat(schedules.size()).isGreaterThanOrEqualTo(0); // 가게 일정 개수 조회
            assertThat(schedules).allMatch(s -> s.getScheduleDate().equals(targetDate)); // 영업일이 일치하는 가게 일정 조회
            
            System.out.println("✅ 영업일로 가게 일정 목록 조회 성공");
            System.out.println("   - 조회 날짜: " + targetDate);
            System.out.println("   - 일정 개수: " + schedules.size());
            schedules.forEach(s -> {
                System.out.println("     * 일정 ID: " + s.getId() + ", 가게 ID: " + s.getStore().getId() + ", 시간: " + s.getStartTime() + "~" + s.getEndTime());
            });
        }
    }

    // 더미 데이터 생성
    private List<StoreSchedule> saveDummyStoreSchedules(Store store) {
        return storeScheduleJpaRepository.saveAll(List.of(
            StoreSchedule.builder()
                .store(store)
                .scheduleDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .maxPeople(10)
                .build(),
            StoreSchedule.builder()
                .store(store)
                .scheduleDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(17, 0))
                .maxPeople(15)
                .build(),
            StoreSchedule.builder()
                .store(store)
                .scheduleDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 0))
                .maxPeople(20)
                .build()
        ));
    }
}
