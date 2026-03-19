package com.coworking.reservationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "member-service", url = "${clients.member.url:http://localhost:8082}")
public interface MemberClient {

    @GetMapping("/api/members/{id}/eligibility")
    EligibilityResponse eligibility(@PathVariable("id") Long memberId);

    @PutMapping("/api/members/{id}/bookings/increment")
    void incrementBookings(@PathVariable("id") Long memberId);

    @PutMapping("/api/members/{id}/bookings/decrement")
    void decrementBookings(@PathVariable("id") Long memberId);

    record EligibilityResponse(boolean eligible, boolean suspended) {
    }
}
