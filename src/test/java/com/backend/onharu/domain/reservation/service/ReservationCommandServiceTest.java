package com.backend.onharu.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

import com.backend.onharu.domain.level.model.Level;
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
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.ChangeReservationStatusCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CompleteReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("ReservationCommandService 단위 테스트")
class ReservationCommandServiceTest {

    @Autowired
    private ReservationCommandService reservationCommandService;

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
     * 테스트용 Child 생성 헬퍼 메서드 (기본값 사용)
     */
    private Child createTestChild(String loginId, String name, String phone) {
        return createTestChild(loginId, name, phone, "/certificates/test.pdf", true);
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
    @DisplayName("예약 생성 테스트")
    class CreateReservationTest {
        
        @Test
        @DisplayName("예약 생성 성공")
        @Rollback(value = false)
        public void shouldCreateReservation() {
            // given
            Child savedChild = createTestChild("test_child", "테스트 아동", "01012345678");
            Level level = createTestLevel("새싹");
            Owner savedOwner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("테스트 가게", savedOwner, category);
            StoreSchedule saveDummyStoreSchedules = saveDummyStoreSchedules(savedStore, 10, 11);
            Integer people = 2; // 인원 수

            // when
            Reservation reservation = reservationCommandService.createReservation(
                new CreateReservationCommand(saveDummyStoreSchedules.getId(), people),
                saveDummyStoreSchedules,
                savedChild
            );

            // then
            assertThat(reservation).isNotNull();
            assertThat(reservation.getId()).isNotNull();
            assertThat(reservation.getChild().getId()).isEqualTo(savedChild.getId());
            assertThat(reservation.getStoreSchedule().getId()).isEqualTo(saveDummyStoreSchedules.getId());
            assertThat(reservation.getPeople()).isEqualTo(people);
            assertThat(reservation.getStatus()).isEqualTo(ReservationType.WAITING);
            assertThat(reservation.getReservationAt()).isNotNull();
            
            // DB에 저장되었는지 확인
            Reservation savedReservation = reservationQueryService.getReservation(
                new GetReservationByIdQuery(reservation.getId())
            );
            assertThat(savedReservation).isNotNull();
            System.out.println("✅ 예약 생성 성공 - Reservation ID: " + reservation.getId());
            System.out.println("   - 아동 ID: " + savedChild.getId());
            System.out.println("   - 가게 일정 ID: " + saveDummyStoreSchedules.getId());
            System.out.println("   - 인원 수: " + people);
            System.out.println("   - 상태: " + reservation.getStatus());
        }
    }

    @Nested
    @DisplayName("예약 취소 테스트")
    class CancelReservationTest {
        
        @Test
        @DisplayName("예약 취소 성공")
        @Rollback(value = false)
        public void shouldCancelReservation() {
            // given
            Child savedChild = createTestChild("test_child2", "테스트 아동2", "01087654321", "/certificates/test2.pdf", true);
            Level level = createTestLevel("새싹");
            Owner savedOwner = createTestOwner("test_owner2", "테스트 사업자2", "01022223333", "새싹", "2234567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("테스트 가게2", savedOwner, category);
            StoreSchedule saveDummyStoreSchedules = saveDummyStoreSchedules(savedStore, 10, 11);

            Reservation savedReservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild) // 아동 설정
                    .storeSchedule(saveDummyStoreSchedules) // 가게 일정 ID 설정
                    .people(2) // 인원 수 설정
                    .status(ReservationType.WAITING) // 예약 상태 설정
                    .build()
            );
            
            String cancelReason = "일정 변경으로 인한 취소";

            // when
            reservationCommandService.cancelReservation(
                new CancelReservationCommand(savedReservation.getId(), cancelReason)
            );

            // then
            Reservation reservation = reservationQueryService.getReservation(
                new GetReservationByIdQuery(savedReservation.getId())
            );
            assertThat(reservation.getStatus()).isEqualTo(ReservationType.CANCELED);
            assertThat(reservation.getCancelReason()).isEqualTo(cancelReason);
            
            System.out.println("예약 취소 성공 - Reservation ID: " + reservation.getId());
            System.out.println("   - 상태: " + reservation.getStatus());
            System.out.println("   - 취소 사유: " + reservation.getCancelReason());
        }
    }

    @Nested
    @DisplayName("예약 완료 처리 테스트")
    class CompleteReservationTest {
        
        @Test
        @DisplayName("예약 완료 처리 성공")
        @Rollback(value = false)
        public void shouldCompleteReservation() {
            // given
            Child savedChild = createTestChild("test_child3", "테스트 아동3", "01011112222", "/certificates/test3.pdf", true);
            Level level = createTestLevel("새싹");
            Owner savedOwner = createTestOwner("test_owner3", "테스트 사업자3", "01033334444", "새싹", "3334567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("테스트 가게3", savedOwner, category);
            StoreSchedule saveDummyStoreSchedules = saveDummyStoreSchedules(savedStore, 10, 11);

            Reservation savedReservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild) // 아동 설정
                    .storeSchedule(saveDummyStoreSchedules) // 가게 일정 ID 설정
                    .people(3) // 인원 수 설정
                    .status(ReservationType.WAITING) // 예약 상태 설정
                    .build()
            );

            // when
            reservationCommandService.completeReservation(
                new CompleteReservationCommand(savedReservation.getId())
            );

            // then
            Reservation reservation = reservationQueryService.getReservation(
                new GetReservationByIdQuery(savedReservation.getId())
            );
            assertThat(reservation.getStatus()).isEqualTo(ReservationType.COMPLETED);
            
            System.out.println("✅ 예약 완료 처리 성공 - Reservation ID: " + reservation.getId());
            System.out.println("   - 상태: " + reservation.getStatus());
        }
    }

    @Nested
    @DisplayName("예약 상태 변경 테스트")
    class ChangeReservationStatusTest {
        
        @Test
        @DisplayName("예약 상태 변경 성공")
        @Rollback(value = false)
        public void shouldChangeReservationStatus() {
            // given
            Child savedChild = createTestChild("test_child4", "테스트 아동4", "01033334444", "/certificates/test4.pdf", true);
            Level level = createTestLevel("새싹");
            Owner savedOwner = createTestOwner("test_owner4", "테스트 사업자4", "01044445555", "새싹", "4444567890");
            Category category = createTestCategory("식당");
            Store savedStore = createTestStore("테스트 가게4", savedOwner, category);
            StoreSchedule saveDummyStoreSchedules = saveDummyStoreSchedules(savedStore, 10, 11);

            Reservation savedReservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(savedChild)
                    .storeSchedule(saveDummyStoreSchedules)
                    .people(1)
                    .status(ReservationType.WAITING)
                    .build()
            );
            
            ReservationType newStatus = ReservationType.COMPLETED;

            // when
            reservationCommandService.changeReservationStatus(
                new ChangeReservationStatusCommand(savedReservation.getId(), newStatus)
            );

            // then
            Reservation reservation = reservationQueryService.getReservation(
                new GetReservationByIdQuery(savedReservation.getId())
            );
            assertThat(reservation.getStatus()).isEqualTo(newStatus);
            
            System.out.println("✅ 예약 상태 변경 성공 - Reservation ID: " + reservation.getId());
            System.out.println("   - 변경 전 상태: WAITING");
            System.out.println("   - 변경 후 상태: " + reservation.getStatus());
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
}
