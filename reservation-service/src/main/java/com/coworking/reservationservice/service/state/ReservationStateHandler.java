package com.coworking.reservationservice.service.state;

import com.coworking.reservationservice.model.Reservation;

public interface ReservationStateHandler {

    void cancel(Reservation reservation);

    void complete(Reservation reservation);
}
