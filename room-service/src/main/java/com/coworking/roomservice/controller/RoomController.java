package com.coworking.roomservice.controller;

import com.coworking.roomservice.model.Room;
import com.coworking.roomservice.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Rooms", description = "Operations CRUD sur les salles")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    @Operation(summary = "Lister toutes les salles")
    public List<Room> all() {
        return roomService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une salle par son id")
    public Room one(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer une nouvelle salle")
    public Room create(@RequestBody Room room) {
        return roomService.create(room);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour une salle")
    public Room update(@PathVariable Long id, @RequestBody Room room) {
        return roomService.update(id, room);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une salle")
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Verifier la disponibilite d'une salle sur un creneau")
    public AvailabilityResponse availability(
            @PathVariable Long id,
            @RequestParam LocalDateTime startDateTime,
            @RequestParam LocalDateTime endDateTime
    ) {
        return new AvailabilityResponse(roomService.isAvailableForWindow(id, startDateTime, endDateTime));
    }

    @PutMapping("/{id}/availability")
    @Operation(summary = "Forcer le statut de disponibilite d'une salle")
    public Room updateAvailability(@PathVariable Long id, @RequestParam boolean available) {
        Room room = roomService.findById(id);
        room.setAvailable(available);
        return roomService.update(id, room);
    }

    public record AvailabilityResponse(boolean available) {
    }
}
