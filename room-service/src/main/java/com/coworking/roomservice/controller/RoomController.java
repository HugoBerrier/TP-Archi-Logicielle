package com.coworking.roomservice.controller;

import com.coworking.roomservice.model.Room;
import com.coworking.roomservice.service.RoomService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> all() {
        return roomService.findAll();
    }

    @GetMapping("/{id}")
    public Room one(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @PostMapping
    public Room create(@RequestBody Room room) {
        return roomService.create(room);
    }

    @PutMapping("/{id}")
    public Room update(@PathVariable Long id, @RequestBody Room room) {
        return roomService.update(id, room);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }

    @GetMapping("/{id}/availability")
    public AvailabilityResponse availability(
            @PathVariable Long id,
            @RequestParam LocalDateTime startDateTime,
            @RequestParam LocalDateTime endDateTime
    ) {
        return new AvailabilityResponse(roomService.isAvailableForWindow(id, startDateTime, endDateTime));
    }

    @PutMapping("/{id}/availability")
    public Room updateAvailability(@PathVariable Long id, @RequestParam boolean available) {
        Room room = roomService.findById(id);
        room.setAvailable(available);
        return roomService.update(id, room);
    }

    public record AvailabilityResponse(boolean available) {
    }
}
