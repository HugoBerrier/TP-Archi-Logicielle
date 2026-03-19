package com.coworking.memberservice.controller;

import com.coworking.memberservice.model.Member;
import com.coworking.memberservice.service.MemberService;
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
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<Member> all() {
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    public Member one(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PostMapping
    public Member create(@RequestBody Member member) {
        return memberService.create(member);
    }

    @PutMapping("/{id}")
    public Member update(@PathVariable Long id, @RequestBody Member member) {
        return memberService.update(id, member);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }

    @GetMapping("/{id}/eligibility")
    public MemberService.EligibilityResponse eligibility(@PathVariable Long id) {
        return memberService.eligibility(id);
    }

    @PutMapping("/{id}/bookings/increment")
    public void incrementBookings(@PathVariable Long id) {
        memberService.incrementActiveBookings(id);
    }

    @PutMapping("/{id}/bookings/decrement")
    public void decrementBookings(@PathVariable Long id) {
        memberService.decrementActiveBookings(id);
    }
}
