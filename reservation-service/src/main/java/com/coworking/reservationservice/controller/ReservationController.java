package com.coworking.reservationservice.controller;

import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.service.ReservationService;
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
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<Reservation> all() {
        return reservationService.findAll();
    }

    @GetMapping("/{id}")
    public Reservation one(@PathVariable Long id) {
        return reservationService.findById(id);
    }

    @PostMapping
    public Reservation create(@RequestBody Reservation reservation) {
        return reservationService.create(reservation);
    }

    @PutMapping("/{id}/cancel")
    public Reservation cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }

    @PutMapping("/{id}/complete")
    public Reservation complete(@PathVariable Long id) {
        return reservationService.complete(id);
    }
}
