package com.coworking.roomservice.service;

import com.coworking.roomservice.model.Room;
import com.coworking.roomservice.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
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
    }

    public boolean isAvailableForWindow(Long roomId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Room room = findById(roomId);
        if (!room.isAvailable()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return endDateTime.isBefore(now) || endDateTime.isEqual(now) || startDateTime.isAfter(now);
    }
}
