package com.coworking.memberservice.service;

import com.coworking.memberservice.model.Member;
import com.coworking.memberservice.model.SubscriptionType;
import com.coworking.memberservice.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    public Member create(Member member) {
        member.setId(null);
        member.setMaxConcurrentBookings(quotaFor(member.getSubscriptionType()));
        member.setSuspended(false);
        if (member.getActiveBookings() == null) {
            member.setActiveBookings(0);
        }
        return memberRepository.save(member);
    }

    public Member update(Long id, Member member) {
        Member current = findById(id);
        current.setFullName(member.getFullName());
        current.setEmail(member.getEmail());
        current.setSubscriptionType(member.getSubscriptionType());
        current.setMaxConcurrentBookings(quotaFor(member.getSubscriptionType()));
        current.setSuspended(current.getActiveBookings() >= current.getMaxConcurrentBookings());
        return memberRepository.save(current);
    }

    public void delete(Long id) {
        memberRepository.delete(findById(id));
    }

    public EligibilityResponse eligibility(Long id) {
        Member member = findById(id);
        boolean eligible = !member.isSuspended() && member.getActiveBookings() < member.getMaxConcurrentBookings();
        return new EligibilityResponse(eligible, member.isSuspended());
    }

    public void incrementActiveBookings(Long id) {
        Member member = findById(id);
        member.setActiveBookings(member.getActiveBookings() + 1);
        member.setSuspended(member.getActiveBookings() >= member.getMaxConcurrentBookings());
        memberRepository.save(member);
    }

    public void decrementActiveBookings(Long id) {
        Member member = findById(id);
        int next = Math.max(0, member.getActiveBookings() - 1);
        member.setActiveBookings(next);
        member.setSuspended(next >= member.getMaxConcurrentBookings());
        memberRepository.save(member);
    }

    private int quotaFor(SubscriptionType subscriptionType) {
        return switch (subscriptionType) {
            case BASIC -> 2;
            case PRO -> 5;
            case ENTERPRISE -> 10;
        };
    }

    public record EligibilityResponse(boolean eligible, boolean suspended) {
    }
}
