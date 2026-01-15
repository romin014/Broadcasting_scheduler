package com.example.broadcasting_scheduler.member;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.example.broadcasting_scheduler.member.dto.MemberRequestDto;
import com.example.broadcasting_scheduler.member.dto.MemberResponseDto;
import com.example.broadcasting_scheduler.member.Specialty;
import com.example.broadcasting_scheduler.schedule.WorshipType;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberAvailabilityRepository memberAvailabilityRepository;

    public MemberService(MemberRepository memberRepository, MemberAvailabilityRepository memberAvailabilityRepository) {
        this.memberRepository = memberRepository;
        this.memberAvailabilityRepository = memberAvailabilityRepository;
    }

    public List<MemberResponseDto> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponseDto::new)
                .collect(Collectors.toList());
    }

    public MemberResponseDto createMember(MemberRequestDto requestDto) {
        Member member = new Member(
                requestDto.getName(),
                requestDto.getSpecialty(),
                0);
        Member saved = memberRepository.save(member);

        // 새 멤버의 기본 요일별 설정 생성
        initializeMemberAvailability(saved);

        return new MemberResponseDto(saved);
    }

    private void initializeMemberAvailability(Member member) {
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            MemberAvailability availability = new MemberAvailability(
                    member, dayOfWeek, true, true, true, true, true);
            memberAvailabilityRepository.save(availability);
        }
    }

    public List<MemberAvailability> getMemberAvailability(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return memberAvailabilityRepository.findByMember(member);
    }

    public void updateMemberAvailability(Long memberId, Model model) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<MemberAvailability> availabilityList = memberAvailabilityRepository.findByMember(member);

        for (MemberAvailability availability : availabilityList) {
            int dayOfWeek = availability.getDayOfWeek();
            String prefix = "day" + dayOfWeek + "_";

            availability.setWeekdayEnabled(model.asMap().containsKey(prefix + "weekday"));
            availability.setDawnEnabled(model.asMap().containsKey(prefix + "dawn"));
            availability.setSunday1Enabled(model.asMap().containsKey(prefix + "sunday1"));
            availability.setSunday2Enabled(model.asMap().containsKey(prefix + "sunday2"));
            availability.setSunday3Enabled(model.asMap().containsKey(prefix + "sunday3"));

            memberAvailabilityRepository.save(availability);
        }
    }

    public void saveMemberAvailability(Long memberId, java.util.Map<String, String> params) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<MemberAvailability> availabilityList = memberAvailabilityRepository.findByMember(member);

        for (MemberAvailability availability : availabilityList) {
            int dayOfWeek = availability.getDayOfWeek();
            String prefix = "day" + dayOfWeek + "_";

            availability.setWeekdayEnabled(params != null && params.containsKey(prefix + "weekday"));
            availability.setDawnEnabled(params != null && params.containsKey(prefix + "dawn"));
            availability.setSunday1Enabled(params != null && params.containsKey(prefix + "sunday1"));
            availability.setSunday2Enabled(params != null && params.containsKey(prefix + "sunday2"));
            availability.setSunday3Enabled(params != null && params.containsKey(prefix + "sunday3"));

            memberAvailabilityRepository.save(availability);
        }
    }

    public void updateMemberAvailability(Long memberId, Map<WorshipType, Boolean> availability) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Specialty는 availability에 따라 자동 설정
        boolean canWeekday = availability.getOrDefault(WorshipType.WEEKDAY, true);
        boolean canSunday = availability.getOrDefault(WorshipType.SUNDAY_1, true) ||
                availability.getOrDefault(WorshipType.SUNDAY_2, true) ||
                availability.getOrDefault(WorshipType.SUNDAY_3, true);
        boolean canDawn = availability.getOrDefault(WorshipType.DAWN, true);

        if (canWeekday && canSunday && canDawn) {
            member.setSpecialty(Specialty.ALL);
        } else if (canWeekday) {
            member.setSpecialty(Specialty.WEEKDAY);
        } else if (canSunday) {
            member.setSpecialty(Specialty.SUNDAY);
        } else if (canDawn) {
            member.setSpecialty(Specialty.DAWN);
        }

        memberRepository.save(member);
    }

    public MemberResponseDto getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return new MemberResponseDto(member);
    }

    public MemberResponseDto updateMember(Long id, MemberRequestDto requestDto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setName(requestDto.getName());
        member.setSpecialty(requestDto.getSpecialty());

        Member updated = memberRepository.save(member);
        return new MemberResponseDto(updated);
    }

    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new RuntimeException("Member not found");
        }

        Member member = memberRepository.findById(id).get();
        memberAvailabilityRepository.deleteByMember(member);
        memberRepository.deleteById(id);
    }
}
