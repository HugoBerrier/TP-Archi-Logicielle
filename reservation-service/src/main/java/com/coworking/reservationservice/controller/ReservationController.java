package com.coworking.reservationservice.controller;

import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Operations sur les reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "Lister toutes les reservations")
    public List<Reservation> all() {
        return reservationService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une reservation par son id")
    public Reservation one(@PathVariable Long id) {
        return reservationService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer une reservation (avec validations)")
    public Reservation create(@RequestBody Reservation reservation) {
        return reservationService.create(reservation);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Annuler une reservation CONFIRMED")
    public Reservation cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Marquer une reservation CONFIRMED comme COMPLETED")
    public Reservation complete(@PathVariable Long id) {
        return reservationService.complete(id);
    }
}
