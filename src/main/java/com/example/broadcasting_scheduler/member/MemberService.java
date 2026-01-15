package com.example.broadcasting_scheduler.member;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.broadcasting_scheduler.member.dto.MemberRequestDto;
import com.example.broadcasting_scheduler.member.dto.MemberResponseDto;
import com.example.broadcasting_scheduler.member.Specialty;
import com.example.broadcasting_scheduler.schedule.WorshipType;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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
            0
        );
        Member saved = memberRepository.save(member);
        return new MemberResponseDto(saved);
    }

    public void updateMemberAvailability(Long memberId, Map<WorshipType, Boolean> availability) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        // Specialty는 availability에 따라 자동 설정
        if (availability.get(WorshipType.WEEKDAY) && 
            availability.get(WorshipType.SUNDAY) && 
            availability.get(WorshipType.DAWN)) {
            member.setSpecialty(Specialty.ALL);
        } else if (availability.get(WorshipType.WEEKDAY)) {
            member.setSpecialty(Specialty.WEEKDAY);
        } else if (availability.get(WorshipType.SUNDAY)) {
            member.setSpecialty(Specialty.SUNDAY);
        } else if (availability.get(WorshipType.DAWN)) {
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
        memberRepository.deleteById(id);
    }
}
