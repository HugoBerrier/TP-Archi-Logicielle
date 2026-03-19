package com.coworking.memberservice.messaging;

import com.coworking.memberservice.service.MemberService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MemberKafkaListener {

    private final MemberService memberService;
    private final ObjectMapper objectMapper;

    public MemberKafkaListener(MemberService memberService, ObjectMapper objectMapper) {
        this.memberService = memberService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "reservation.created")
    public void onReservationCreated(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        Long memberId = node.get("memberId").asLong();
        memberService.incrementActiveBookings(memberId);
    }

    @KafkaListener(topics = "reservation.released")
    public void onReservationReleased(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        Long memberId = node.get("memberId").asLong();
        memberService.decrementActiveBookings(memberId);
    }
}
