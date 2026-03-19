package com.coworking.reservationservice.repository;

import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByRoomIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            Long roomId,
            ReservationStatus status,
            LocalDateTime endDateTime,
            LocalDateTime startDateTime
    );

    List<Reservation> findByRoomIdAndStatus(Long roomId, ReservationStatus status);

    List<Reservation> findByMemberId(Long memberId);
}
