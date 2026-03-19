package com.coworking.reservationservice.messaging;

import com.coworking.reservationservice.service.ReservationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationKafkaListener {

    private final ReservationService reservationService;
    private final ObjectMapper objectMapper;

    public ReservationKafkaListener(ReservationService reservationService, ObjectMapper objectMapper) {
        this.reservationService = reservationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "room.deleted")
    public void onRoomDeleted(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        Long roomId = node.get("roomId").asLong();
        reservationService.cancelConfirmedForRoom(roomId);
    }

    @KafkaListener(topics = "member.deleted")
    public void onMemberDeleted(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        Long memberId = node.get("memberId").asLong();
        reservationService.deleteAllForMember(memberId);
    }
}
