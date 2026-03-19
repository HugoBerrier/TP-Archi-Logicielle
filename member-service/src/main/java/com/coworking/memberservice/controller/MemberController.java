package com.coworking.memberservice.controller;

import com.coworking.memberservice.model.Member;
import com.coworking.memberservice.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Operations CRUD sur les membres")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    @Operation(summary = "Lister tous les membres")
    public List<Member> all() {
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un membre par son id")
    public Member one(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer un membre")
    public Member create(@RequestBody Member member) {
        return memberService.create(member);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour un membre")
    public Member update(@PathVariable Long id, @RequestBody Member member) {
        return memberService.update(id, member);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un membre")
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }

    @GetMapping("/{id}/eligibility")
    @Operation(summary = "Verifier si un membre peut reserver")
    public MemberService.EligibilityResponse eligibility(@PathVariable Long id) {
        return memberService.eligibility(id);
    }

    @PutMapping("/{id}/bookings/increment")
    @Operation(summary = "Incrementer le nombre de reservations actives")
    public void incrementBookings(@PathVariable Long id) {
        memberService.incrementActiveBookings(id);
    }

    @PutMapping("/{id}/bookings/decrement")
    @Operation(summary = "Decrementer le nombre de reservations actives")
    public void decrementBookings(@PathVariable Long id) {
        memberService.decrementActiveBookings(id);
    }
}
