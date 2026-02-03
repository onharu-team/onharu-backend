package com.backend.onharu.application;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_ALREADY_EXISTS;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_CHILD_ID_MISMATCH;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("ChildFacade 단위 테스트")
class ChildFacadeTest {

    @Autowired
    private ChildFacade childFacade;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

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
        categoryJpaRepository.deleteAll();
        childJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    /**
     * 테스트용 User 생성 헬퍼 메서드 (아동용)
     */
    private User createTestUserForChild(String loginId, String name, String phone) {
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
     * 테스트용 Child 생성 헬퍼 메서드 (User와 함께 생성)
     */
    private Child createTestChild(String loginId, String name, String phone, String certificate, Boolean isVerified) {
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

    /**
     * 테스트용 StoreSchedule 생성 헬퍼 메서드
     */
    private StoreSchedule createTestStoreSchedule(Store store, int startHour, int endHour) {
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

    @Nested
    @DisplayName("예약 하기 테스트")
    class ReserveTest {

        @Test
        @DisplayName("예약 생성 성공")
        @Rollback(value = false)
        public void shouldCreateReservation() {
            // given
            Child child = createTestChild("test_child", "테스트 아동", "01012345678");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11);

            CreateReservationCommand command = new CreateReservationCommand(
                storeSchedule.getId(),
                2
            );

            // when
            childFacade.reserve(command); // 예약 생성

            // then
            Reservation reservation = reservationJpaRepository.getByStoreScheduleId(storeSchedule.getId())
                .orElse(null); // 예약 조회
            assertThat(reservation).isNotNull();
            assertThat(reservation.getChild().getId()).isEqualTo(child.getId());
            assertThat(reservation.getStoreSchedule().getId()).isEqualTo(storeSchedule.getId());
            assertThat(reservation.getPeople()).isEqualTo(2);
            assertThat(reservation.getStatus()).isEqualTo(ReservationType.WAITING);

            System.out.println("✅ 예약 생성 성공 - Reservation ID: " + reservation.getId());
            System.out.println("   - 아동 ID: " + child.getId());
            System.out.println("   - 가게 일정 ID: " + storeSchedule.getId());
            System.out.println("   - 인원 수: " + reservation.getPeople());
        }

        @Test
        @DisplayName("예약 생성 실패 - 이미 예약된 일정")
        public void shouldThrowExceptionWhenScheduleAlreadyReserved() {
            // given
            Child child1 = createTestChild("test_child1", "테스트 아동1", "01012345678");
            Child child2 = createTestChild("test_child2", "테스트 아동2", "01087654321");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11);

            // 첫 번째 예약 생성
            CreateReservationCommand firstCommand = new CreateReservationCommand(
                storeSchedule.getId(),
                2
            );
            childFacade.reserve(firstCommand);

            // 두 번째 예약 시도
            CreateReservationCommand secondCommand = new CreateReservationCommand(
                storeSchedule.getId(),
                3
            );

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> childFacade.reserve(secondCommand)
            );

            assertThat(exception.getErrorType()).isEqualTo(RESERVATION_ALREADY_EXISTS);
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
            Child child = createTestChild("test_child", "테스트 아동", "01012345678");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11);

            Reservation reservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child)
                    .storeSchedule(storeSchedule)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            );

            CancelReservationCommand command = new CancelReservationCommand(
                reservation.getId(),
                "일정 변경으로 인한 취소"
            );

            // when
            childFacade.cancelReservation(command);

            // then
            Reservation canceledReservation = reservationJpaRepository.findById(reservation.getId())
                .orElse(null);
            assertThat(canceledReservation).isNotNull();
            assertThat(canceledReservation.getStatus()).isEqualTo(ReservationType.CANCELED);
            assertThat(canceledReservation.getCancelReason()).isEqualTo("일정 변경으로 인한 취소");

            System.out.println("✅ 예약 취소 성공 - Reservation ID: " + canceledReservation.getId());
            System.out.println("   - 상태: " + canceledReservation.getStatus());
            System.out.println("   - 취소 사유: " + canceledReservation.getCancelReason());
        }

        @Test
        @DisplayName("예약 취소 실패 - 다른 아동의 예약")
        public void shouldThrowExceptionWhenReservationBelongsToOtherChild() {
            // given
            Child child1 = createTestChild("test_child1", "테스트 아동1", "01012345678"); // 아동1 생성
            Child child2 = createTestChild("test_child2", "테스트 아동2", "01087654321"); // 아동2 생성
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11); // 가게 일정 생성 (10시 ~ 11시)

            Reservation reservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child1)
                    .storeSchedule(storeSchedule)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            ); // 아동1 이 가게에 예약 생성

            CancelReservationCommand command = new CancelReservationCommand(
                reservation.getId(),
                "일정 변경으로 인한 취소"
            );

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> childFacade.cancelReservation(command) // 아동2가 예약 취소 시도
            );

            assertThat(exception.getErrorType()).isEqualTo(RESERVATION_CHILD_ID_MISMATCH);
        }
    }

    @Nested
    @DisplayName("내가 신청한 예약 목록 조회 테스트")
    class GetMyBookingsTest {

        @Test
        @DisplayName("내가 신청한 예약 목록 조회 성공")
        @Rollback(value = false)
        public void shouldGetMyBookings() {
            // given
            Child child1 = createTestChild("test_child1", "테스트 아동1", "01012345678");
            Child child2 = createTestChild("test_child2", "테스트 아동2", "01087654321");
            Level level = createTestLevel("새싹");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);

            StoreSchedule schedule1 = createTestStoreSchedule(store, 10, 11);
            StoreSchedule schedule2 = createTestStoreSchedule(store, 14, 15);
            StoreSchedule schedule3 = createTestStoreSchedule(store, 16, 17);

            // child1의 예약 2개
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(child1)
                    .storeSchedule(schedule1)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            );
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(child1)
                    .storeSchedule(schedule2)
                    .people(3)
                    .status(ReservationType.WAITING)
                    .build()
            );

            // child2의 예약 1개
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(child2)
                    .storeSchedule(schedule3)
                    .people(1)
                    .status(ReservationType.WAITING)
                    .build()
            );

            // when
            List<Reservation> myBookings = childFacade.getMyBookings();

            // then
            assertThat(myBookings).isNotNull();
            assertThat(myBookings.size()).isEqualTo(2);
            assertThat(myBookings).allMatch(r -> r.getChild().getId().equals(child1.getId()));

            System.out.println("✅ 내가 신청한 예약 목록 조회 성공");
            System.out.println("   - 아동 ID: " + child1.getId());
            System.out.println("   - 예약 개수: " + myBookings.size());
        }

        @Test
        @DisplayName("예약이 없을 때 빈 목록 반환")
        public void shouldReturnEmptyListWhenNoReservations() {
            // given
            Child child = createTestChild("test_child", "테스트 아동", "01012345678");

            // when
            List<Reservation> myBookings = childFacade.getMyBookings();

            // then
            assertThat(myBookings).isNotNull();
            assertThat(myBookings).isEmpty();
        }
    }

    @Nested
    @DisplayName("내가 신청한 특정 예약의 상세 정보 조회 테스트")
    class GetMyBookingTest {

        @Test
        @DisplayName("내가 신청한 특정 예약의 상세 정보 조회 성공")
        @Rollback(value = false)
        public void shouldGetMyBooking() {
            // given
            Child child = createTestChild("test_child", "테스트 아동", "01012345678");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11);

            Reservation reservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child)
                    .storeSchedule(storeSchedule)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            );

            // when
            Reservation result = childFacade.getMyBooking(reservation.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reservation.getId());
            assertThat(result.getChild().getId()).isEqualTo(child.getId());
            assertThat(result.getStoreSchedule().getId()).isEqualTo(storeSchedule.getId());
            assertThat(result.getPeople()).isEqualTo(2);

            System.out.println("✅ 내가 신청한 특정 예약의 상세 정보 조회 성공");
            System.out.println("   - 예약 ID: " + result.getId());
            System.out.println("   - 아동 ID: " + child.getId());
        }

        @Test
        @DisplayName("다른 아동의 예약 조회 시 예외 발생")
        public void shouldThrowExceptionWhenReservationBelongsToOtherChild() {
            // given
            Child child1 = createTestChild("test_child1", "테스트 아동1", "01012345678"); // 아동1 생성
            Child child2 = createTestChild("test_child2", "테스트 아동2", "01087654321"); // 아동2 생성
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11); // 가게 일정 생성 (10시 ~ 11시)

            Reservation reservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child1)
                    .storeSchedule(storeSchedule)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            ); // 아동1 이 가게에 예약 생성

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> childFacade.getMyBooking(reservation.getId()) // 아동2가 예약 조회 시도
            );

            assertThat(exception.getErrorType()).isEqualTo(RESERVATION_CHILD_ID_MISMATCH);
        }
    }
}
