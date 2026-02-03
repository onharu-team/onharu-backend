package com.backend.onharu.interfaces.api.controller.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.application.OwnerFacade;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IOwnerController;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CreateOwnerRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CreateOwnerResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetMyStoresResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetOwnerResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingDetailResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingListResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.RejectBookRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.RemoveAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.ReservationResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.SetAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateOwnerRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.StoreResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사업주 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 사업자 정보 등록, 수정, 삭제, 조회 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/owners")
@RequiredArgsConstructor
public class OwnerControllerImpl implements IOwnerController {

    private final OwnerFacade ownerFacade;

    /**
     * 사업자 정보 등록
     * 
     * POST /owners/business
     * 사업자 정보를 등록합니다.
     *
     * @param request 사업자 정보 등록 요청
     * @param businessRegistrationFile 사업자 등록 서류 파일
     * @return 생성된 사업자 정보
     */
    @Override
    @PostMapping("/business")
    public ResponseEntity<ResponseDTO<CreateOwnerResponse>> registerBusiness(
            @RequestPart CreateOwnerRequest request,
            @RequestPart MultipartFile businessRegistrationFile
    ) {
        log.info("사업자 정보 등록 요청: request={}, fileName={}", request, businessRegistrationFile.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사업자 정보 수정
     * 
     * PUT /owners/{ownerId}/business
     * 사업자 정보를 수정합니다. 
     *
     * @param ownerId 사업자 ID
     * @param request 사업자 정보 수정 요청
     * @param businessRegistrationFile 사업자 등록 서류 파일
     * @return 수정 결과
     */
    @Override
    @PutMapping("/{ownerId}/business")
    public ResponseEntity<ResponseDTO<Void>> updateBusiness(
            @PathVariable("ownerId") Long ownerId,
            @RequestPart UpdateOwnerRequest request,
            @RequestPart MultipartFile businessRegistrationFile
    ) {
        log.info("사업자 정보 수정 요청: ownerId={}, request={}, fileName={}", 
                ownerId, request, businessRegistrationFile.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사업자 정보 삭제
     * 
     * DELETE /owners/business/{ownerId}
     * 사업자 정보를 삭제합니다. 
     *
     * @param ownerId 사업자 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/business/{ownerId}")
    public ResponseEntity<ResponseDTO<Void>> closeBusiness(
            @PathVariable("ownerId") Long ownerId
    ) {
        log.info("사업자 정보 삭제 요청: ownerId={}", ownerId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사업자 정보 조회
     * 
     * GET /owners/business/{ownerId}
     * 사업자 정보를 조회합니다. 
     *
     * @param ownerId 사업자 ID
     * @return 사업자 정보
     */
    @Override
    @GetMapping("/business/{ownerId}")
    public ResponseEntity<ResponseDTO<GetOwnerResponse>> getMyBusiness(
            @PathVariable("ownerId") Long ownerId
    ) {
        log.info("사업자 정보 조회 요청: ownerId={}", ownerId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사업자 가게 목록 조회
     * 
     * GET /owners/stores
     * 사업자의 가게 목록을 조회합니다.
     *
     * @return 사업자 가게 목록
     */
    @Override
    @GetMapping("/stores")
    public ResponseEntity<ResponseDTO<GetMyStoresResponse>> getMyStores() {
        log.info("사업자 가게 목록 조회 요청");

        List<Store> stores = ownerFacade.getMyStores();
        List<StoreResponse> storeResponses = stores.stream()
                .map(StoreResponse::new)
                .collect(Collectors.toList());

        GetMyStoresResponse response = new GetMyStoresResponse(storeResponses);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 예약 관리 목록 조회
     * 
     * GET /owners/reservations
     * 사업자의 예약 목록을 조회합니다.
     *
     * @return 예약 관리 목록
     */
    @Override
    @GetMapping("/stores/{storeId}/reservations")
    public ResponseEntity<ResponseDTO<GetStoreBookingListResponse>> getStoreBookings(
            @PathVariable("storeId") Long storeId
    ) {
        log.info("예약 관리 목록 조회 요청");
        List<Reservation> reservations = ownerFacade.getStoreBookings(storeId); // 가게의 예약 목록 조회
        List<ReservationResponse> reservationResponses = reservations.stream()
                .map(ReservationResponse::new)
                .collect(Collectors.toList());

        GetStoreBookingListResponse response = new GetStoreBookingListResponse(reservationResponses); // 예약 관리 목록 응답 생성

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 예약 관리 상세 조회
     * 
     * GET /owners/reservations/{reservationId}
     * 사업자의 특정 예약의 상세 정보를 조회합니다.
     *
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    @Override
    @GetMapping("/stores/{storeId}/reservations/{reservationId}")
    public ResponseEntity<ResponseDTO<GetStoreBookingDetailResponse>> getStoreBooking(
        @PathVariable("storeId") Long storeId,
        @PathVariable("reservationId") Long reservationId
    ) {
        log.info("예약 관리 상세 조회 요청: storeId={}, reservationId={}", storeId, reservationId);

        Reservation reservation = ownerFacade.getStoreBooking(reservationId, storeId);
        ReservationResponse reservationResponse = new ReservationResponse(reservation);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(new GetStoreBookingDetailResponse(reservationResponse)));
    }

    /**
     * 예약 승인
     * 
     * POST /owners/reservations/{reservationId}/approve
     * 사업자가 예약을 승인합니다.
     *
     * @param reservationId 예약 ID
     * @return 승인 결과
     */
    @Override
    @PostMapping("/reservations/{reservationId}/approve")
    public ResponseEntity<ResponseDTO<Void>> approveBook(
            @PathVariable("reservationId") Long reservationId
    ) {
        log.info("예약 승인 요청: reservationId={}", reservationId);

        ownerFacade.approveReservation(reservationId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 거절
     * 
     * POST /owners/reservations/{reservationId}/reject
     * 사업자가 예약을 거절합니다.
     *
     * @param reservationId 예약 ID
     * @return 거절 결과
     */
    @Override
    @PostMapping("/reservations/{reservationId}/reject")
    public ResponseEntity<ResponseDTO<Void>> rejectBook(
            @PathVariable("reservationId") Long reservationId,
            @RequestBody RejectBookRequest request
    ) {
        log.info("예약 거절 요청: reservationId={}, request={}", reservationId, request);
        
        ownerFacade.rejectReservation(reservationId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 가능한 날짜 생성
     * 
     * POST /owners/stores/{storeId}/available-dates
     * 예약 가능한 날짜를 생성합니다.
     *
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 생성 요청
     * @return 생성 결과
     */
    @Override
    @PostMapping("/stores/{storeId}/available-dates")
    public ResponseEntity<ResponseDTO<Void>> setAvailableDates(
            @PathVariable("storeId") Long storeId,
            @RequestBody SetAvailableDatesRequest request
    ) {
        log.info("예약 가능한 날짜 생성 요청: storeId={}, request={}", storeId, request);

        ownerFacade.setAvailableDates(storeId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 가능한 날짜 수정
     * 
     * PUT /owners/stores/{storeId}/available-dates
     * 예약 가능한 날짜를 수정합니다.
     *
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 수정 요청
     * @return 수정 결과
     */
    @Override
    @PutMapping("/stores/{storeId}/available-dates")
    public ResponseEntity<ResponseDTO<Void>> updateAvailableDates(
            @PathVariable("storeId") Long storeId,
            @RequestBody UpdateAvailableDatesRequest request
    ) {
        log.info("예약 가능한 날짜 수정 요청: storeId={}, request={}", storeId, request);

        ownerFacade.updateAvailableDates(storeId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 가능한 날짜 삭제
     * 
     * DELETE /owners/stores/{storeId}/available-dates
     * 예약 가능한 날짜를 삭제합니다.
     *
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 삭제 요청
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/stores/{storeId}/available-dates")
    public ResponseEntity<ResponseDTO<Void>> removeAvailableDates(
            @PathVariable("storeId") Long storeId,
            @RequestBody RemoveAvailableDatesRequest request
    ) {
        log.info("예약 가능한 날짜 삭제 요청: storeId={}, request={}", storeId, request);

        ownerFacade.removeAvailableDates(storeId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}
