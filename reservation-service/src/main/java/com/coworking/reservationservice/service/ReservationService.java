package com.coworking.reservationservice.service;

import com.coworking.reservationservice.client.MemberClient;
import com.coworking.reservationservice.client.RoomClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.model.ReservationStatus;
import com.coworking.reservationservice.repository.ReservationRepository;
import com.coworking.reservationservice.service.state.ReservationStateResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomClient roomClient;
    private final MemberClient memberClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ReservationStateResolver reservationStateResolver;

    public ReservationService(
            ReservationRepository reservationRepository,
            RoomClient roomClient,
            MemberClient memberClient,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            ReservationStateResolver reservationStateResolver
    ) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
        this.memberClient = memberClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.reservationStateResolver = reservationStateResolver;
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
        publishReservationCreated(saved);
        return saved;
    }

    public Reservation cancel(Long id) {
        Reservation reservation = findById(id);
        reservationStateResolver.resolve(reservation.getStatus()).cancel(reservation);
        Reservation saved = reservationRepository.save(reservation);
        publishReservationReleased(saved);
        return saved;
    }

    public Reservation complete(Long id) {
        Reservation reservation = findById(id);
        reservationStateResolver.resolve(reservation.getStatus()).complete(reservation);
        Reservation saved = reservationRepository.save(reservation);
        publishReservationReleased(saved);
        return saved;
    }

    public void cancelConfirmedForRoom(Long roomId) {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStatus(roomId, ReservationStatus.CONFIRMED);
        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            publishReservationReleased(reservation);
        }
    }

    public void deleteAllForMember(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        for (Reservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                publishReservationReleased(reservation);
            }
        }
        reservationRepository.deleteAll(reservations);
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

    private void publishReservationCreated(Reservation reservation) {
        try {
            kafkaTemplate.send(
                    "reservation.created",
                    objectMapper.writeValueAsString(
                            new ReservationEvent(
                                    reservation.getId(),
                                    reservation.getRoomId(),
                                    reservation.getMemberId(),
                                    isActiveNow(reservation)
                            )
                    )
            );
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot publish reservation creation event");
        }
    }

    private void publishReservationReleased(Reservation reservation) {
        try {
            kafkaTemplate.send(
                    "reservation.released",
                    objectMapper.writeValueAsString(
                            new ReservationEvent(
                                    reservation.getId(),
                                    reservation.getRoomId(),
                                    reservation.getMemberId(),
                                    isActiveNow(reservation)
                            )
                    )
            );
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot publish reservation release event");
        }
    }

    private record ReservationEvent(Long reservationId, Long roomId, Long memberId, boolean activeNow) {
    }
}
