package com.example.broadcasting_scheduler.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAvailabilityRepository extends JpaRepository<MemberAvailability, Long> {
    Optional<MemberAvailability> findByMemberAndDayOfWeek(Member member, int dayOfWeek);
    List<MemberAvailability> findByMember(Member member);
    void deleteByMember(Member member);
}
