package com.backend.onharu.domain.reservation.service;

import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
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

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindAllByStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdAndStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetByStoreScheduleIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("ReservationQueryService 단위 테스트")
class ReservationQueryServiceTest {

    @Autowired
    private ReservationQueryService reservationQueryService;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

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
                .userType(UserType.CHILD)
                .statusType(StatusType.ACTIVE)
                .build()
        );
    }

    /**
     * 테스트용 Child 생성 헬퍼 메서드 (User와 함께 생성)
     */
    private Child createTestChild(String loginId, String name, String phone, String certificate, Boolean isVerified) {
        User user = createTestUser(loginId, name, phone);
        return childJpaRepository.save(
            Child.builder()
                .user(user)
                .certificate(certificate)
                .isVerified(isVerified != null ? isVerified : true)
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
        User user = userJpaRepository.save(
            User.builder()
                .loginId(loginId)
                .password("password123")
                .name(name)
                .phone(phone)
                .userType(UserType.OWNER)
                .statusType(StatusType.ACTIVE)
                .build()
        );

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
    @DisplayName("예약 단건 조회 테스트")
    class GetReservationTest {
        
        @Test
        @DisplayName("조회 실패 - 예약 ID가 존재하지 않는 경우")
        public void shouldThrowExceptionWhenReservationIsNotFound() {
            // given
            Long reservationId = 99L;

            // when
            CoreException coreException = Assertions.assertThrows(
                CoreException.class, 
                () -> reservationQueryService.getReservation(
                    new GetReservationByIdQuery(reservationId)
                )
            );

            // then
            assertThat(coreException.getErrorType()).isEqualTo(RESERVATION_NOT_FOUND);
        }

        @Test
        @DisplayName("조회 성공")
        @Rollback(value = false)
        public void shouldGetReservation() {
            // given
            Child savedChild = createTestChild("test_child_query", "테스트 아동 조회", "01055556666", "/certificates/query_test.pdf", true);
            Level level = createTestLevel("새싹");
            Owner savedOwner = createTestOwner("test_owner_query", "테스트 사업자 조회", "01011112222", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("테스트 가게", savedOwner, category);
            StoreSchedule saveDummyStoreSchedules1 = saveDummyStoreSchedules(savedStore, 10, 11);

            // 예약 생성
            Reservation savedReservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild)
                    .storeSchedule(saveDummyStoreSchedules1)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            );

            // when           
            Reservation reservation = reservationQueryService.getReservation(
                new GetReservationByIdQuery(savedReservation.getId())
            );  // 예약 조회

            // then
            assertThat(reservation).isNotNull();
            assertThat(reservation.getId()).isEqualTo(savedReservation.getId());
            assertThat(reservation.getPeople()).isEqualTo(2);
            assertThat(reservation.getStatus()).isEqualTo(ReservationType.WAITING);
            
            System.out.println("예약 조회 성공 - Reservation ID: " + reservation.getId());
            System.out.println("   - 아동 ID: " + reservation.getChild().getId());
            System.out.println("   - 가게 일정 ID: " + reservation.getStoreSchedule().getId());
            System.out.println("   - 인원 수: " + reservation.getPeople());
            System.out.println("   - 상태: " + reservation.getStatus());
        }
    }

    @Nested
    @DisplayName("아동 ID로 예약 목록 조회 테스트")
    class FindAllByChildIdTest {
        
        @Test
        @DisplayName("조회 성공 - 아동의 예약 목록 조회")
        @Rollback(value = false)
        public void shouldGetReservationsByChildId() {
            // given
            Child savedChild = createTestChild(
                "test_child_list", 
                "테스트 아동 목록", 
                "01077778888", 
                "/certificates/list_test.pdf", 
                true); // 아동 생성
            Level level = createTestLevel("새싹");
            Owner savedOwner = createTestOwner("test_owner_list", "테스트 사업자 목록", "01022223333", "새싹", "2234567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("테스트 가게 목록", savedOwner, category);
            saveDummyReservations(savedChild, savedStore); // 예약 더미 데이터 생성

            // when
            List<Reservation> reservations = reservationQueryService.findByChildId(
                new FindByChildIdQuery(savedChild.getId())
            ); // 아동 ID로 예약 목록 조회

            // then
            assertThat(reservations).hasSize(3);
            assertThat(reservations).allMatch(r -> r.getChild().getId().equals(savedChild.getId()));
            
            System.out.println("✅ 아동 ID로 예약 목록 조회 성공");
            System.out.println("   - 아동 ID: " + savedChild.getId());
            System.out.println("   - 예약 개수: " + reservations.size());
            reservations.forEach(r -> {
                System.out.println("     * 예약 ID: " + r.getId() + ", 상태: " + r.getStatus() + ", 인원: " + r.getPeople());
            });
        }
    }

    @Nested
    @DisplayName("가게 일정 ID로 예약 조회 테스트")
    class GetByStoreScheduleIdTest {
        
        @Test
        @DisplayName("조회 성공 - 가게 일정 ID로 예약 조회")
        @Rollback(value = false)
        public void shouldGetReservationByStoreScheduleId() {
            // given
            Child savedChild = createTestChild("test_child_schedule", "테스트 아동 일정", "01099990000", "/certificates/schedule_test.pdf", true);
            Level level = createTestLevel("새싹");
            Owner savedOwner = createTestOwner("test_owner_schedule", "테스트 사업자 일정", "01055556666", "새싹", "5554567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("테스트 가게 일정", savedOwner, category);
            StoreSchedule storeSchedule = saveDummyStoreSchedules(savedStore, 10, 11);
            Long storeScheduleId = storeSchedule.getId(); 

            // 예약 생성
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild)
                    .storeSchedule(storeSchedule)
                    .people(4)
                    .status(ReservationType.WAITING)
                    .build()
            );

            // when
            Reservation reservation = reservationQueryService.getByStoreScheduleId(
                new GetByStoreScheduleIdQuery(storeScheduleId)
            );

            // then
            assertThat(reservation).isNotNull();
            assertThat(reservation.getStoreSchedule().getId()).isEqualTo(storeScheduleId);
            
            System.out.println("✅ 가게 일정 ID로 예약 조회 성공");
            System.out.println("   - 가게 일정 ID: " + storeScheduleId);
            System.out.println("   - 예약 ID: " + reservation.getId());
            System.out.println("   - 인원 수: " + reservation.getPeople());
        }

        @Test
        @DisplayName("조회 실패 - 가게 일정 ID로 예약 조회")
        @Rollback(value = false)
        public void shouldGetNullWhenReservationIsNotFound() {
            // given
            Long storeScheduleId = 99L;

            // when
            Reservation reservation = reservationQueryService.getByStoreScheduleId(new GetByStoreScheduleIdQuery(storeScheduleId));

            // then
            assertThat(reservation).isNull();
        }
    }

    @Nested
    @DisplayName("예약 상태로 예약 목록 조회 테스트")
    class FindAllByStatusTest {
        
        @Test
        @DisplayName("조회 성공 - 상태로 예약 목록 조회")
        @Rollback(value = false)
        public void shouldGetReservationsByStatus() {
            // given
            Child savedChild1 = createTestChild("test_child_status1", "테스트 아동 상태1", "01011111111", "/certificates/status_test1.pdf", true);
            Child savedChild2 = createTestChild("test_child_status2", "테스트 아동 상태2", "01022222222", "/certificates/status_test2.pdf", true);
            Level level = createTestLevel("새싹");
            Owner savedOwner = createTestOwner("test_owner_status", "테스트 사업자 상태", "01033334444", "새싹", "3334567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("테스트 가게 상태", savedOwner, category);
            
            StoreSchedule saveDummyStoreSchedules1 = saveDummyStoreSchedules(savedStore, 10, 11);
            StoreSchedule saveDummyStoreSchedules2 = saveDummyStoreSchedules(savedStore, 12, 13);
            StoreSchedule saveDummyStoreSchedules3 = saveDummyStoreSchedules(savedStore, 14, 15);

            // 예약1 생성
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild1)
                    .storeSchedule(saveDummyStoreSchedules1)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            );
            
            // 예약2 생성
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild2)
                    .storeSchedule(saveDummyStoreSchedules2)
                    .people(3)
                    .status(ReservationType.WAITING)
                    .build()
            );
            
            // 예약3 생성
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild1)
                    .storeSchedule(saveDummyStoreSchedules3)
                    .people(1)
                    .status(ReservationType.COMPLETED)
                    .build()
            );

            // when
            List<Reservation> waitingReservations = reservationQueryService.findAllByStatus(
                new FindAllByStatusQuery(ReservationType.WAITING)
            );

            // then
            assertThat(waitingReservations).hasSize(2);
            assertThat(waitingReservations).allMatch(r -> r.getStatus().equals(ReservationType.WAITING));
            
            System.out.println("✅ 상태로 예약 목록 조회 성공");
            System.out.println("   - 조회 상태: WAITING");
            System.out.println("   - 예약 개수: " + waitingReservations.size());
            waitingReservations.forEach(r -> {
                System.out.println("     * 예약 ID: " + r.getId() + ", 아동 ID: " + r.getChild().getId() + ", 인원: " + r.getPeople());
            });
        }
    }

    @Nested
    @DisplayName("아동 ID와 상태로 예약 목록 조회 테스트")
    class FindByChildIdAndStatusTest {
        
        @Test
        @DisplayName("조회 성공 - 아동 ID와 상태로 예약 목록 조회")
        @Rollback(value = false)
        public void shouldGetReservationsByChildIdAndStatus() {
            // given
            Child savedChild = createTestChild("test_child_filter", "테스트 아동 필터", "01033333333", "/certificates/filter_test.pdf", true);
            Level level = createTestLevel("새싹");
            Owner savedOwner = createTestOwner("test_owner_filter", "테스트 사업자 필터", "01044445555", "새싹", "4444567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("테스트 가게 필터", savedOwner, category);
            
            StoreSchedule saveDummyStoreSchedules1 = saveDummyStoreSchedules(savedStore, 10, 11);
            StoreSchedule saveDummyStoreSchedules2 = saveDummyStoreSchedules(savedStore, 12, 13);
            StoreSchedule saveDummyStoreSchedules3 = saveDummyStoreSchedules(savedStore, 14, 15);

            // 예약1 생성
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild)
                    .storeSchedule(saveDummyStoreSchedules1)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            );
            
            // 예약2 생성
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild)
                    .storeSchedule(saveDummyStoreSchedules2)
                    .people(3)
                    .status(ReservationType.WAITING)
                    .build()
            );
            
            // 예약3 생성
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild)
                    .storeSchedule(saveDummyStoreSchedules3)
                    .people(1)
                    .status(ReservationType.COMPLETED)
                    .build()
            );

            // when
            List<Reservation> waitingReservations = reservationQueryService.findByChildIdAndStatus(
                new FindByChildIdAndStatusQuery(savedChild.getId(), ReservationType.WAITING)
            ); // 아동 ID와 상태로 예약 목록 조회

            // then
            assertThat(waitingReservations).hasSize(2);
            assertThat(waitingReservations).allMatch(r -> 
                r.getChild().getId().equals(savedChild.getId()) && 
                r.getStatus().equals(ReservationType.WAITING)
            );
            
            System.out.println("✅ 아동 ID와 상태로 예약 목록 조회 성공");
            System.out.println("   - 아동 ID: " + savedChild.getId());
            System.out.println("   - 조회 상태: WAITING");
            System.out.println("   - 예약 개수: " + waitingReservations.size());
            waitingReservations.forEach(r -> {
                System.out.println("     * 예약 ID: " + r.getId() + ", 가게 일정 ID: " + r.getStoreSchedule().getId() + ", 인원: " + r.getPeople());
            });
        }
    }

    /**
     * 테스트용 StoreSchedule 생성 헬퍼 메서드
     * 
     * @param store 가게 엔티티 (필수)
     * @param startHour 시작 시간 (시)
     * @param endHour 종료 시간 (시)
     * @return 생성된 StoreSchedule
     */
    private StoreSchedule saveDummyStoreSchedules(Store store, int startHour, int endHour) {
        return storeScheduleJpaRepository.save(
            StoreSchedule.builder()
                .store(store)
                .scheduleDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(startHour, 0))
                .endTime(LocalTime.of(endHour, 0))
                .maxPeople(10)
                .build()
        );
    }
    
    /**
     * 테스트용 예약 더미 데이터 생성
     * 
     * @param child 아동 엔티티
     * @param store 가게 엔티티
     * @return 생성된 예약 목록
     */
    private List<Reservation> saveDummyReservations(Child child, Store store) {
        // 가게 일정 3개 생성
        StoreSchedule storeSchedule1 = saveDummyStoreSchedules(store, 10, 11);
        StoreSchedule storeSchedule2 = saveDummyStoreSchedules(store, 12, 13);
        StoreSchedule storeSchedule3 = saveDummyStoreSchedules(store, 14, 15);

        return reservationJpaRepository.saveAll(List.of(
            Reservation.builder()
                .child(child)
                .storeSchedule(storeSchedule1)
                .people(2)
                .status(ReservationType.WAITING)
                .build(),
            Reservation.builder()
                .child(child)
                .storeSchedule(storeSchedule2)
                .people(3)
                .status(ReservationType.WAITING)
                .build(),
            Reservation.builder()
                .child(child)
                .storeSchedule(storeSchedule3)
                .people(1)
                .status(ReservationType.COMPLETED)
                .build()
        ));
    }
}
