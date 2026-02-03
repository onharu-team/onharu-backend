package com.backend.onharu.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.backend.onharu.infra.security.port.ISecuritySession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.application.validator.StoreScheduleValidator;
import com.backend.onharu.application.validator.StoreScheduleValidator.ScheduleTimeRange;
import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByUserIdQuery;
import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByIdQuery;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.service.OwnerQueryService;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CompleteReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.RejectReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByStoreIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.service.ReservationCommandService;
import com.backend.onharu.domain.reservation.service.ReservationQueryService;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.DeleteStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.UpdateStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleCommandService;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.RejectBookRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.RemoveAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.SetAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateAvailableDatesRequest;

import lombok.RequiredArgsConstructor;

/**
 * 사업자 Facade
 */
@Component
@RequiredArgsConstructor
public class OwnerFacade {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final StoreQueryService storeQueryService;
    private final StoreScheduleQueryService storeScheduleQueryService;
    private final OwnerQueryService ownerQueryService;
    private final StoreScheduleCommandService storeScheduleCommandService;
    private final StoreScheduleValidator storeScheduleValidator;

    private final ISecuritySession iSecuritySession;

    /**
     * 사업자의 가게 목록 조회
     *
     * @return 사업자의 가게 목록
     */
    public List<Store> getMyStores() {
        // SecurityContext 에서 사용자 ID 획득
        Long userId = iSecuritySession.getUsername();

        // 사업자 정보 조회 (존재 여부 확인)
        Owner owner = ownerQueryService.getOwnerByUserId(new GetOwnerByUserIdQuery(userId));

        // 사업자의 가게 목록 조회
        List<Store> stores = storeQueryService.findByOwnerId(new FindByOwnerIdQuery(owner.getId()));

        // 각 가게가 해당 사업자의 소유인지 검증
        stores.forEach(store -> store.BelongsTo(owner));

        return stores;
    }

    /**
     * 사업자의 가게 단건 조회
     * 
     * @param owenrId 사업자 ID
     * @param storeId 가게 ID
     * @return 사업자의 가게 단건
     */
    public Store getMyStore(Long owenrId, Long storeId) {
        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(owenrId));

        // 가게 정보 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(storeId));

        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);

        return store;
    }

    /**
     * 사업자 가게의 예약 목록 조회
     *
     * @param storeId 가게 ID
     * @return 사업자의 예약 목록
     */
    public List<Reservation> getStoreBookings(Long storeId) {
        // SecurityContext 에서 사용자 ID 획득
        Long userId = iSecuritySession.getUsername();

        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerByUserId(new GetOwnerByUserIdQuery(userId));

        // 가게 정보 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(storeId));

        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);

        // 예약 목록 조회
        return reservationQueryService.findByStoreId(new FindByStoreIdQuery(storeId));
    }

    /**
     * 사업자의 특정 예약의 상세 정보를 조회
     * 
     * @param reservationId 예약 ID
     * @return 사업자의 특정 예약의 상세 정보
     */
    public Reservation getStoreBooking(Long reservationId, Long storeId) {
        // 예약 정보 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(reservationId));

        // 예약이 해당 가게의 예약인지 확인
        reservation.BelongsTo(storeId);

        return reservation;
    }

    /**
     * 예약 가능한 날짜 생성
     * 
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 생성 요청
     */
    @Transactional
    public void setAvailableDates(Long storeId, SetAvailableDatesRequest request) {
        // 가게 정보 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(storeId));

        // SecurityContext 에서 사용자 ID 획득
        Long userId = iSecuritySession.getUsername();

        // 사업자 정보 조회 (존재 여부 확인)
        Owner owner = ownerQueryService.getOwnerByUserId(new GetOwnerByUserIdQuery(userId));

        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);

        // 일정 중복 검증 (요청 내부 중복 + 기존 DB 일정과의 중복)
        List<ScheduleTimeRange> timeRanges = request.storeSchedules().stream()
                .map(req -> new ScheduleTimeRange(
                        req.scheduleDate(),
                        req.startTime(),
                        req.endTime()))
                .toList();
        storeScheduleValidator.validateNoDuplicates(storeId, timeRanges, null);

        // 요청 데이터를 리스트로 변환
        List<StoreSchedule> schedules = request.storeSchedules().stream()
        .map(req -> StoreSchedule.builder()
                .store(store)
                .scheduleDate(req.scheduleDate())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .maxPeople(req.maxPeople())
                .build())
        .toList();

        // 저장
        storeScheduleCommandService.createStoreSchedules(schedules);
    }

    /**
     * 예약 가능한 날짜 수정
     * 
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 수정 요청
     */
    @Transactional
    public void updateAvailableDates(Long storeId, UpdateAvailableDatesRequest request) {
        // 가게 정보 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(storeId));

        // SecurityContext 에서 사용자 ID 획득
        Long userId = iSecuritySession.getUsername();

        // 사업자 정보 조회 (존재 여부 확인)
        Owner owner = ownerQueryService.getOwnerByUserId(new GetOwnerByUserIdQuery(userId));

        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);

        // 일정 중복 검증 (요청 내부 중복 + 기존 DB 일정과의 중복, 자기 자신은 제외)
        Set<Long> updatingScheduleIds = request.storeSchedules().stream()
                .map(req -> req.id())
                .collect(Collectors.toSet());
        List<ScheduleTimeRange> timeRanges = request.storeSchedules().stream()
                .map(req -> new ScheduleTimeRange(
                        req.scheduleDate(),
                        req.startTime(),
                        req.endTime()))
                .toList();
        storeScheduleValidator.validateNoDuplicates(storeId, timeRanges, updatingScheduleIds);

        // 각 스케줄이 해당 가게에 속하는지 검증하고 업데이트
        request.storeSchedules().forEach(req -> {
            // 스케줄 조회 및 가게 소유권 검증
            StoreSchedule schedule = storeScheduleQueryService.getStoreScheduleById(
                    new GetStoreScheduleByIdQuery(req.id()));
            
            // 스케줄이 해당 가게에 속하는지 확인
            if (!schedule.getStore().getId().equals(storeId)) {
                throw new CoreException(
                        ErrorType.Store.STORE_OWNER_MISMATCH);
            }

            // 스케줄 업데이트
            storeScheduleCommandService.updateStoreSchedule(
                    new UpdateStoreScheduleCommand(
                            req.id(),
                            req.scheduleDate(),
                            req.startTime(),
                            req.endTime(),
                            req.maxPeople()
                    )
            );
        });
    }
    
    /**
     * 예약 가능한 날짜 삭제
     * 
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 삭제 요청
     */
    public void removeAvailableDates(Long storeId, RemoveAvailableDatesRequest request) {
        // 가게 정보 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(storeId));

        // SecurityContext 에서 사용자 ID 획득
        Long userId = iSecuritySession.getUsername();

        // 사업자 정보 조회 (존재 여부 확인)
        Owner owner = ownerQueryService.getOwnerByUserId(new GetOwnerByUserIdQuery(userId));

        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);

        // 요청 데이터를 리스트로 변환
        List<StoreSchedule> schedules = request.storeScheduleIds().stream()
            .map(id -> storeScheduleQueryService.getStoreScheduleById(new GetStoreScheduleByIdQuery(id)))
            .toList();

        // 삭제
        for (StoreSchedule schedule : schedules) {
            storeScheduleCommandService.deleteStoreSchedule(new DeleteStoreScheduleCommand(schedule.getId()));
        }
    }

    /**
     * 예약 승인
     * 
     * @param reservationId 예약 ID
     */
    public void approveReservation(Long reservationId) {
        // 예약 정보 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(reservationId));

        // Store 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(reservation.getStoreSchedule().getStore().getId()));

        // SecurityContext 에서 사용자 ID 획득
        Long userId = iSecuritySession.getUsername();

        // Owner 조회
        Owner owner = ownerQueryService.getOwnerByUserId(new GetOwnerByUserIdQuery(userId));

        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);

        // 예약 승인
        reservationCommandService.completeReservation(new CompleteReservationCommand(reservation.getId()));
    }

    /**
     * 예약 거절
     * 
     * @param reservationId 예약 ID
     */
    public void rejectReservation(Long reservationId, RejectBookRequest request) {
        // 예약 정보 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(reservationId));

        // 예약 거절
        reservationCommandService.rejectReservation(new RejectReservationCommand(reservation.getId(), request.rejectReason()));
    }
}
