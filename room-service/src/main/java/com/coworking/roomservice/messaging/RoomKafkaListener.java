package com.coworking.roomservice.messaging;

import com.coworking.roomservice.service.RoomService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RoomKafkaListener {

    private final RoomService roomService;
    private final ObjectMapper objectMapper;

    public RoomKafkaListener(RoomService roomService, ObjectMapper objectMapper) {
        this.roomService = roomService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "reservation.created")
    public void onReservationCreated(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        Long roomId = node.get("roomId").asLong();
        boolean activeNow = node.get("activeNow").asBoolean();
        if (activeNow) {
            roomService.setAvailability(roomId, false);
        }
    }

    @KafkaListener(topics = "reservation.released")
    public void onReservationReleased(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        Long roomId = node.get("roomId").asLong();
        boolean activeNow = node.get("activeNow").asBoolean();
        if (activeNow) {
            roomService.setAvailability(roomId, true);
        }
    }
}
