package com.backend.onharu.domain.reservation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.ChangeReservationStatusCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CompleteReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;

@DisplayName("ReservationCommand 단위 테스트")
class ReservationCommandTest {

    @Nested
    @DisplayName("CreateReservationCommand 생성자 테스트")
    class CreateReservationCommandTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        public void shouldCreateReservationCommand() {
            // given
            Long childId = 855L;
            Long storeScheduleId = 1L;
            Integer people = 2;

            // when
            CreateReservationCommand command = new CreateReservationCommand(
                storeScheduleId, people
            );

            // then
            assertThat(command.storeScheduleId()).isEqualTo(storeScheduleId);
            assertThat(command.people()).isEqualTo(people);
        }
    }

    @Nested
    @DisplayName("CancelReservationCommand 생성자 테스트")
    class CancelReservationCommandTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        public void shouldCreateCancelReservationCommand() {
            // given
            Long id = 1L;
            String cancelReason = "일정 변경";

            // when
            CancelReservationCommand command = new CancelReservationCommand(id, cancelReason);

            // then
            assertThat(command.reservationId()).isEqualTo(id);
            assertThat(command.cancelReason()).isEqualTo(cancelReason);
        }
    }

    @Nested
    @DisplayName("CompleteReservationCommand 생성자 테스트")
    class CompleteReservationCommandTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        public void shouldCreateCompleteReservationCommand() {
            // given
            Long id = 1L;

            // when
            CompleteReservationCommand command = new CompleteReservationCommand(id);

            // then
            assertThat(command.reservationId()).isEqualTo(id);
        }
    }

    @Nested
    @DisplayName("ChangeReservationStatusCommand 생성자 테스트")
    class ChangeReservationStatusCommandTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        public void shouldCreateChangeReservationStatusCommand() {
            // given
            Long id = 1L;
            ReservationType status = ReservationType.COMPLETED;

            // when
            ChangeReservationStatusCommand command = new ChangeReservationStatusCommand(id, status);

            // then
            assertThat(command.reservationId()).isEqualTo(id);
            assertThat(command.status()).isEqualTo(status);
        }
    }
}
