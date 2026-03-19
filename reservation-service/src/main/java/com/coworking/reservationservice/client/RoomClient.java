package com.coworking.reservationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

import java.time.LocalDateTime;

@FeignClient(name = "room-service", url = "${clients.room.url:http://localhost:8081}")
public interface RoomClient {

    @GetMapping("/api/rooms/{id}/availability")
    AvailabilityResponse availability(
            @PathVariable("id") Long roomId,
            @RequestParam("startDateTime") LocalDateTime startDateTime,
            @RequestParam("endDateTime") LocalDateTime endDateTime
    );

    @PutMapping("/api/rooms/{id}/availability")
    void updateAvailability(@PathVariable("id") Long roomId, @RequestParam("available") boolean available);

    record AvailabilityResponse(boolean available) {
    }
}
