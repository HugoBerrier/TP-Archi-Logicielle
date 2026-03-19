package com.coworking.roomservice.service;

import com.coworking.roomservice.model.Room;
import com.coworking.roomservice.repository.RoomRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public RoomService(
            RoomRepository roomRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.roomRepository = roomRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    }

    public Room create(Room room) {
        room.setId(null);
        return roomRepository.save(room);
    }

    public Room update(Long id, Room room) {
        Room current = findById(id);
        current.setName(room.getName());
        current.setCity(room.getCity());
        current.setCapacity(room.getCapacity());
        current.setType(room.getType());
        current.setHourlyRate(room.getHourlyRate());
        current.setAvailable(room.isAvailable());
        return roomRepository.save(current);
    }

    public void delete(Long id) {
        roomRepository.delete(findById(id));
        publishRoomDeleted(id);
    }

    public boolean isAvailableForWindow(Long roomId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Room room = findById(roomId);
        if (!room.isAvailable()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return endDateTime.isBefore(now) || endDateTime.isEqual(now) || startDateTime.isAfter(now);
    }

    public void setAvailability(Long roomId, boolean available) {
        Room room = findById(roomId);
        room.setAvailable(available);
        roomRepository.save(room);
    }

    private void publishRoomDeleted(Long roomId) {
        try {
            kafkaTemplate.send("room.deleted", objectMapper.writeValueAsString(new RoomDeletedEvent(roomId)));
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot publish room deletion event");
        }
    }

    private record RoomDeletedEvent(Long roomId) {
    }
}
