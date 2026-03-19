package com.coworking.memberservice.service;

import com.coworking.memberservice.model.Member;
import com.coworking.memberservice.model.SubscriptionType;
import com.coworking.memberservice.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public MemberService(
            MemberRepository memberRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.memberRepository = memberRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
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
        publishMemberDeleted(id);
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

    private void publishMemberDeleted(Long memberId) {
        try {
            kafkaTemplate.send("member.deleted", objectMapper.writeValueAsString(new MemberDeletedEvent(memberId)));
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot publish member deletion event");
        }
    }

    private record MemberDeletedEvent(Long memberId) {
    }
}
