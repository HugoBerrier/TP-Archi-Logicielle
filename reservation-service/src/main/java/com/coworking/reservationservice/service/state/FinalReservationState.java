package com.coworking.reservationservice.service.state;

import com.coworking.reservationservice.model.Reservation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FinalReservationState implements ReservationStateHandler {

    @Override
    public void cancel(Reservation reservation) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is already in a final state");
    }

    @Override
    public void complete(Reservation reservation) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is already in a final state");
    }
}
