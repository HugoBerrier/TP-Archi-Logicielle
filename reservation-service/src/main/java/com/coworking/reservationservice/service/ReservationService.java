package com.coworking.reservationservice.service;

import com.coworking.reservationservice.client.MemberClient;
import com.coworking.reservationservice.client.RoomClient;
import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.model.ReservationStatus;
import com.coworking.reservationservice.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomClient roomClient;
    private final MemberClient memberClient;

    public ReservationService(
            ReservationRepository reservationRepository,
            RoomClient roomClient,
            MemberClient memberClient
    ) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
        this.memberClient = memberClient;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    public Reservation create(Reservation reservation) {
        validateDateWindow(reservation);
        validateRoomAvailability(reservation);
        validateMemberEligibility(reservation);

        reservation.setId(null);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepository.save(reservation);

        memberClient.incrementBookings(saved.getMemberId());
        if (isActiveNow(saved)) {
            roomClient.updateAvailability(saved.getRoomId(), false);
        }
        return saved;
    }

    public Reservation cancel(Long id) {
        Reservation reservation = findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only CONFIRMED reservation can be cancelled");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        memberClient.decrementBookings(saved.getMemberId());
        roomClient.updateAvailability(saved.getRoomId(), true);
        return saved;
    }

    public Reservation complete(Long id) {
        Reservation reservation = findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only CONFIRMED reservation can be completed");
        }
        reservation.setStatus(ReservationStatus.COMPLETED);
        Reservation saved = reservationRepository.save(reservation);
        memberClient.decrementBookings(saved.getMemberId());
        roomClient.updateAvailability(saved.getRoomId(), true);
        return saved;
    }

    private void validateDateWindow(Reservation reservation) {
        if (reservation.getStartDateTime() == null || reservation.getEndDateTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDateTime and endDateTime are required");
        }
        if (!reservation.getStartDateTime().isBefore(reservation.getEndDateTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reservation date range");
        }
    }

    private void validateRoomAvailability(Reservation reservation) {
        boolean overlapExists = reservationRepository
                .existsByRoomIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        reservation.getRoomId(),
                        ReservationStatus.CONFIRMED,
                        reservation.getEndDateTime(),
                        reservation.getStartDateTime()
                );
        if (overlapExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room already booked on this timeslot");
        }

        RoomClient.AvailabilityResponse roomAvailability = roomClient.availability(
                reservation.getRoomId(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime()
        );
        if (!roomAvailability.available()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is not available");
        }
    }

    private void validateMemberEligibility(Reservation reservation) {
        MemberClient.EligibilityResponse eligibility = memberClient.eligibility(reservation.getMemberId());
        if (!eligibility.eligible()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member is suspended or reached quota");
        }
    }

    private boolean isActiveNow(Reservation reservation) {
        return reservation.getStartDateTime().isBefore(java.time.LocalDateTime.now())
                && reservation.getEndDateTime().isAfter(java.time.LocalDateTime.now());
    }
}
