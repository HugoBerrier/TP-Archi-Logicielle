package com.coworking.reservationservice.service.state;

import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.model.ReservationStatus;
import org.springframework.stereotype.Component;

@Component
public class ConfirmedReservationState implements ReservationStateHandler {

    @Override
    public void cancel(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELLED);
    }

    @Override
    public void complete(Reservation reservation) {
        reservation.setStatus(ReservationStatus.COMPLETED);
    }
}
