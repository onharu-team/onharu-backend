package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.application.ChildFacade;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IChildrenController;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 결식 아동 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 결식 아동 카드, 증명서 관리 및 관심 가게 목록 조회 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/children")
@RequiredArgsConstructor
public class ChildrenControllerImpl implements IChildrenController {

    private final ChildFacade childFacade;

    /**
     * 결식 아동 카드 등록
     * 
     * POST /children/cards
     * 새로운 결식 아동 카드를 등록합니다.
     *
     * @param request 카드 등록 요청
     * @return 결식 아동 카드 등록 결과
     */
    @Override
    @PostMapping("/cards")
    public ResponseEntity<ResponseDTO<IssueCardResponse>> issueCard(
            @RequestBody IssueCardRequest request
    ) {
        log.info("결식 아동 카드 등록 요청: {}", request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 카드 수정
     * 
     * PUT /children/cards/{cardId}
     * 특정 카드 정보를 수정합니다.
     *
     * @param cardId 카드 ID
     * @param request 카드 수정 요청
     * @return 결식 아동 카드 수정 결과
     */
    @Override
    @PutMapping("/cards/{cardId}")
    public ResponseEntity<ResponseDTO<Void>> updateCard(
            @PathVariable("cardId") Long cardId,
            @RequestBody UpdateCardRequest request
    ) {
        log.info("결식 아동 카드 수정 요청: cardId={}, request={}", cardId, request);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 카드 삭제
     * 
     * DELETE /children/cards/{cardId}
     * 특정 카드를 삭제합니다.
     *
     * @param cardId 카드 ID
     * @return
     */
    @Override
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<ResponseDTO<Void>> deleteCard(
            @PathVariable("cardId") Long cardId
    ) {
        log.info("결식 아동 카드 삭제 요청: cardId={}", cardId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 카드 재발급 요청
     * 
     * POST /children/cards/{cardId}/reissue
     * 카드 재발급을 요청합니다.
     *
     * @param cardId 카드 ID
     * @return 재발급 요청 결과
     */
    @Override
    @PostMapping("/cards/{cardId}/reissue")
    public ResponseEntity<ResponseDTO<Void>> reissueCard(
            @PathVariable("cardId") Long cardId
    ) {
        log.info("결식 아동 카드 재발급 요청: cardId={}", cardId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 카드 조회
     * 
     * GET /children/cards/{cardId}
     * 특정 카드 정보를 조회합니다.
     *
     * @param cardId 카드 ID
     * @return 카드 정보
     */
    @Override
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<ResponseDTO<GetCardResponse>> getMyCard(
            @PathVariable("cardId") Long cardId
    ) {
        log.info("결식 아동 카드 조회 요청: cardId={}", cardId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 증명서 등록
     * 
     * POST /children/certificate
     * 증명서를 등록합니다. (첨부파일 포함)
     *
     * @param request 증명서 등록 요청
     * @param file 증명서 파일
     * @return 등록 결과
     */
    @Override
    @PostMapping("/certificate")
    public ResponseEntity<ResponseDTO<Void>> uploadCertificate(
            @RequestPart UpdateCertificateRequest request,
            @RequestPart MultipartFile file
    ) {
        log.info("결식 아동 증명서 등록 요청: request={}, fileName={}", request, file.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 증명서 수정
     * 
     * PUT /children/certificate/{certificateId}
     * 증명서를 수정합니다. (첨부파일 포함)
     *
     * @param certificateId 증명서 ID
     * @param request 증명서 수정 요청
     * @param file 증명서 파일
     * @return 수정 결과
     */
    @Override
    @PutMapping("/certificate/{certificateId}")
    public ResponseEntity<ResponseDTO<Void>> updateMyCertificate(
            @PathVariable("certificateId") Long certificateId,
            @RequestPart UpdateCertificateRequest request,
            @RequestPart MultipartFile file
    ) {
        log.info("결식 아동 증명서 수정 요청: certificateId={}, request={}, fileName={}", 
                certificateId, request, file.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 증명서 삭제
     * 
     * DELETE /children/certificate/{certificateId}
     * 증명서를 삭제합니다. (첨부파일 포함)
     *
     * @param certificateId 증명서 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/certificate/{certificateId}")
    public ResponseEntity<ResponseDTO<Void>> removeMyCertificate(
            @PathVariable("certificateId") Long certificateId
    ) {
        log.info("결식 아동 증명서 삭제 요청: certificateId={}", certificateId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 결식 아동 증명서 조회
     * 
     * GET /children/certificate/{certificateId}
     * 증명서를 조회합니다. (첨부파일 포함)
     *
     * @param certificateId 증명서 ID
     * @return 증명서 정보
     */
    @Override
    @GetMapping("/certificate/{certificateId}")
    public ResponseEntity<ResponseDTO<GetCertificateResponse>> getMyCertificate(
            @PathVariable("certificateId") Long certificateId
    ) {
        log.info("결식 아동 증명서 조회 요청: certificateId={}", certificateId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 예약 생성
     * 
     * POST /children/stores/{storeId}/reservations
     * 새로운 예약을 생성합니다.
     *
     * @param storeId 가게 ID
     * @param request 예약 생성 요청
     * @return 생성된 예약 정보
     */
    @Override
    @PostMapping("/stores/{storeId}/reservations")
    public ResponseEntity<ResponseDTO<BookStoreResponse>> bookStore(
            @PathVariable("storeId") Long storeId,
            @RequestBody BookStoreRequest request
    ) {
        log.info("예약 생성 요청: storeId={}, request={}", storeId, request);

        childFacade.reserve(new CreateReservationCommand(request.storeScheduleId(), request.people()));
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 취소
     * 
     * POST /children/reservations/{reservationId}/cancel
     * 기존 예약을 취소합니다.
     *
     * @param reservationId 예약 ID
     * @return 취소 결과
     */
    @Override
    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<ResponseDTO<Void>> cancelStore(
            @PathVariable("reservationId") Long reservationId
    ) {
        log.info("예약 취소 요청: reservationId={}", reservationId);

        childFacade.cancelReservation(new CancelReservationCommand(reservationId, "예약 취소"));
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 신청 목록 조회
     * 
     * GET /children/reservations
     * 내가 신청한 예약 목록을 조회합니다.
     *
     * @return 내 예약 목록
     */
    @Override
    @GetMapping("/reservations")
    public ResponseEntity<ResponseDTO<GetMyBookingListResponse>> getMyBookings() {
        log.info("예약 신청 목록 조회 요청");

        List<Reservation> reservations = childFacade.getMyBookings(); // 내 예약 목록 조회
        List<ReservationResponse> reservationResponses = reservations.stream()
                .map(ReservationResponse::new)
                .collect(Collectors.toList());

        GetMyBookingListResponse response = new GetMyBookingListResponse(reservationResponses);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 예약 신청 상세 조회
     * 
     * GET /children/reservations/{reservationId}
     * 내가 신청한 특정 예약의 상세 정보를 조회합니다.
     *
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    @Override
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ResponseDTO<GetMyBookingDetailResponse>> getMyBooking(
            @PathVariable("reservationId") Long reservationId
    ) {
        log.info("예약 신청 상세 조회 요청: reservationId={}", reservationId);

        Reservation reservation = childFacade.getMyBooking(reservationId);
        ReservationResponse reservationResponse = new ReservationResponse(reservation);

        GetMyBookingDetailResponse response = new GetMyBookingDetailResponse(reservationResponse);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }
}
