package com.example.broadcasting_scheduler.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.broadcasting_scheduler.member.Member;
import com.example.broadcasting_scheduler.member.MemberAvailability;
import com.example.broadcasting_scheduler.member.MemberAvailabilityRepository;
import com.example.broadcasting_scheduler.member.MemberRepository;
import com.example.broadcasting_scheduler.member.Specialty;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final MemberAvailabilityRepository memberAvailabilityRepository;

    public DataInitializer(MemberRepository memberRepository,
            MemberAvailabilityRepository memberAvailabilityRepository) {
        this.memberRepository = memberRepository;
        this.memberAvailabilityRepository = memberAvailabilityRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 이미 데이터가 있으면 초기화하지 않음
        if (memberRepository.count() > 0) {
            return;
        }

        // 12명의 임의 멤버 생성
        String[] names = {
                "김철수", "이영희", "박민수", "정수진", "최동욱",
                "한지은", "윤태영", "강미영", "조성호", "임하늘",
                "오준석", "신유진"
        };

        Specialty[] specialties = {
                Specialty.ALL, Specialty.WEEKDAY, Specialty.SUNDAY, Specialty.DAWN,
                Specialty.ALL, Specialty.WEEKDAY, Specialty.SUNDAY, Specialty.DAWN,
                Specialty.ALL, Specialty.WEEKDAY, Specialty.SUNDAY, Specialty.ALL
        };

        for (int i = 0; i < names.length; i++) {
            Member member = new Member(names[i], specialties[i], 0);
            Member savedMember = memberRepository.save(member);

            // 각 멤버의 요일별 설정 생성
            for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
                MemberAvailability availability = new MemberAvailability(
                        savedMember, dayOfWeek, true, true, true, true, true);
                memberAvailabilityRepository.save(availability);
            }
        }
    }
}
