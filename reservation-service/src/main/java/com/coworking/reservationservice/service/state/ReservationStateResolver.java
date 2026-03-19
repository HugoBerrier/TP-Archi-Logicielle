package com.coworking.reservationservice.service.state;

import com.coworking.reservationservice.model.ReservationStatus;
import org.springframework.stereotype.Component;

@Component
public class ReservationStateResolver {

    private final ConfirmedReservationState confirmedReservationState;
    private final FinalReservationState finalReservationState;

    public ReservationStateResolver(
            ConfirmedReservationState confirmedReservationState,
            FinalReservationState finalReservationState
    ) {
        this.confirmedReservationState = confirmedReservationState;
        this.finalReservationState = finalReservationState;
    }

    public ReservationStateHandler resolve(ReservationStatus status) {
        if (status == ReservationStatus.CONFIRMED) {
            return confirmedReservationState;
        }
        return finalReservationState;
    }
}
