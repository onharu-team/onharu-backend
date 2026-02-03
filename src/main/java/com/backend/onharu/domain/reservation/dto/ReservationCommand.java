package com.backend.onharu.domain.reservation.dto;

import com.backend.onharu.domain.common.enums.ReservationType;

public class ReservationCommand {
    /**
     * 예약 생성 커맨드
     */
    public record CreateReservationCommand(
            Long storeScheduleId,
            Integer people
    ) {
        public CreateReservationCommand {
            // if (storeScheduleId == null) {
            //     throw new CoreException(BAD_REQUEST);
            // }
            // if (people == null || people <= 0) {
            //     throw new CoreException(BAD_REQUEST);
            // }
        }
    }

    /**
     * 예약 취소 커맨드
     */
    public record CancelReservationCommand(
            Long reservationId,
            String cancelReason
    ) {
        public CancelReservationCommand {
            // if (reservationId == null) {
            //     throw new CoreException(BAD_REQUEST);
            // }
        }
    }

    /**
     * 예약 거절 커맨드
     */
    public record RejectReservationCommand(
            Long reservationId,
            String rejectReason
    ) {
        public RejectReservationCommand {
            // if (reservationId == null) {
            //     throw new CoreException(BAD_REQUEST);
            // }
        }
    }

    /**
     * 예약 완료 처리 커맨드
     */
    public record CompleteReservationCommand(
            Long reservationId
    ) {
        public CompleteReservationCommand {
            // if (reservationId == null) {
            //     throw new CoreException(BAD_REQUEST);
            // }
        }
    }

    /**
     * 예약 상태 변경 커맨드
     */
    public record ChangeReservationStatusCommand(
            Long reservationId,
            ReservationType status
    ) {
        public ChangeReservationStatusCommand {
            // if (reservationId == null) {
            //     throw new CoreException(BAD_REQUEST);
            // }
            // if (status == null) {
            //     throw new CoreException(BAD_REQUEST);
            // }
        }
    }
}
