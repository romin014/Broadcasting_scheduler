package com.example.broadcasting_scheduler.schedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.broadcasting_scheduler.member.MemberService;
import com.example.broadcasting_scheduler.member.dto.MemberResponseDto;
import com.example.broadcasting_scheduler.schedule.dto.ScheduleResponseDto;
import com.example.broadcasting_scheduler.schedule.DayOfWeekSetting;
import com.example.broadcasting_scheduler.schedule.DayOfWeekSettingRepository;
import com.example.broadcasting_scheduler.schedule.SundaySetting;
import com.example.broadcasting_scheduler.schedule.SundaySettingRepository;
import com.example.broadcasting_scheduler.member.Member;
import com.example.broadcasting_scheduler.member.MemberAvailability;
import com.example.broadcasting_scheduler.member.MemberAvailabilityRepository;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final MemberService memberService;
    private final DayOfWeekSettingRepository dayOfWeekSettingRepository;
    private final SundaySettingRepository sundaySettingRepository;
    private final MemberAvailabilityRepository memberAvailabilityRepository;
    private final com.example.broadcasting_scheduler.member.MemberRepository memberRepository;

    public ScheduleController(ScheduleService scheduleService, MemberService memberService,
            DayOfWeekSettingRepository dayOfWeekSettingRepository, SundaySettingRepository sundaySettingRepository,
            MemberAvailabilityRepository memberAvailabilityRepository,
            com.example.broadcasting_scheduler.member.MemberRepository memberRepository) {
        this.scheduleService = scheduleService;
        this.memberService = memberService;
        this.dayOfWeekSettingRepository = dayOfWeekSettingRepository;
        this.sundaySettingRepository = sundaySettingRepository;
        this.memberAvailabilityRepository = memberAvailabilityRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/input")
    public String inputPage(@RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            Model model) {
        // 현재 날짜 기준으로 기본값 설정
        if (year == 0 || month == 0) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        List<MemberResponseDto> members = memberService.getAllMembers();

        // 요일별 설정 불러오기 (기본값: 모두 활성화)
        Map<Integer, DayOfWeekSetting> daySettings = new HashMap<>();
        Map<Integer, String> dayNames = new HashMap<>();
        dayNames.put(1, "월요일");
        dayNames.put(2, "화요일");
        dayNames.put(3, "수요일");
        dayNames.put(4, "목요일");
        dayNames.put(5, "금요일");
        dayNames.put(6, "토요일");

        for (int day = 1; day <= 6; day++) {
            DayOfWeekSetting setting = dayOfWeekSettingRepository.findByDayOfWeek(day)
                    .orElse(new DayOfWeekSetting(day, true, true));
            daySettings.put(day, setting);
        }

        // 주일 예배 설정 불러오기 (기본값: 모두 활성화)
        SundaySetting sundaySetting = sundaySettingRepository.findFirstByOrderByIdAsc()
                .orElse(new SundaySetting(true, true, true));

        // 멤버별 요일별 가용성 불러오기
        Map<Long, Map<Integer, MemberAvailability>> memberAvailabilities = new HashMap<>();
        for (MemberResponseDto member : members) {
            Member memberEntity = memberRepository.findById(member.getId()).orElse(null);
            if (memberEntity != null) {
                List<MemberAvailability> availabilities = memberAvailabilityRepository.findByMember(memberEntity);
                Map<Integer, MemberAvailability> dayMap = new HashMap<>();
                for (MemberAvailability avail : availabilities) {
                    dayMap.put(avail.getDayOfWeek(), avail);
                }
                memberAvailabilities.put(member.getId(), dayMap);
            }
        }

        model.addAttribute("members", members);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("worshipTypes", WorshipType.values());
        model.addAttribute("daySettings", daySettings);
        model.addAttribute("dayNames", dayNames);
        model.addAttribute("sundaySetting", sundaySetting);
        model.addAttribute("memberAvailabilities", memberAvailabilities);

        return "schedule/input";
    }

    @PostMapping("/generate")
    public String generateSchedule(@RequestParam int year,
            @RequestParam int month,
            @RequestParam Map<String, String> allParams,
            Model model) {
        // 폼 데이터에서 가용성 정보 추출
        Map<Long, Map<String, Boolean>> memberAvailability = new HashMap<>();

        List<MemberResponseDto> members = memberService.getAllMembers();
        for (MemberResponseDto member : members) {
            Map<String, Boolean> availability = new HashMap<>();
            String weekdayKey = "availability_" + member.getId() + "_weekday";
            String sundayKey = "availability_" + member.getId() + "_sunday";
            String dawnKey = "availability_" + member.getId() + "_dawn";

            availability.put("weekday", "on".equals(allParams.get(weekdayKey)));
            availability.put("sunday", "on".equals(allParams.get(sundayKey)));
            availability.put("dawn", "on".equals(allParams.get(dawnKey)));

            memberAvailability.put(member.getId(), availability);
        }

        // 요일별 설정 추출 (1=월요일, 2=화요일, ..., 6=토요일)
        Map<Integer, Map<String, Boolean>> dayOfWeekSettings = new HashMap<>();
        for (int day = 1; day <= 6; day++) { // 월요일부터 토요일까지
            Map<String, Boolean> settings = new HashMap<>();
            String weekdayKey = "day_" + day + "_weekday";
            String dawnKey = "day_" + day + "_dawn";

            settings.put("weekday", "on".equals(allParams.get(weekdayKey)));
            settings.put("dawn", "on".equals(allParams.get(dawnKey)));
            dayOfWeekSettings.put(day, settings);
        }

        // 주일 예배 설정 추출
        Map<String, Boolean> sundaySettings = new HashMap<>();
        sundaySettings.put("sunday1", "on".equals(allParams.get("sunday_1")));
        sundaySettings.put("sunday2", "on".equals(allParams.get("sunday_2")));
        sundaySettings.put("sunday3", "on".equals(allParams.get("sunday_3")));

        // 스케줄 생성
        scheduleService.generateMonthlySchedule(year, month, memberAvailability, dayOfWeekSettings, sundaySettings);

        return "redirect:/schedule/view?year=" + year + "&month=" + month;
    }

    @GetMapping("/view")
    public String viewPage(@RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            Model model) {
        // 현재 날짜 기준으로 기본값 설정
        if (year == 0 || month == 0) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        List<Schedule> schedules = scheduleService.getMonthlySchedule(year, month);
        List<ScheduleResponseDto> scheduleDtos = schedules.stream()
                .map(ScheduleResponseDto::new)
                .collect(Collectors.toList());

        // 달력 형식으로 변환 (날짜를 문자열 키로 변환)
        Map<String, Map<String, String>> calendarData = new HashMap<>();
        for (ScheduleResponseDto dto : scheduleDtos) {
            String dateKey = dto.getDate().toString();
            calendarData.putIfAbsent(dateKey, new HashMap<>());
            calendarData.get(dateKey).put(dto.getWorshipType().name(), dto.getMemberName());
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        // 달력에 표시할 날짜 리스트 생성 (첫 주 시작일부터 마지막 주 종료일까지)
        int dayOfWeek = start.getDayOfWeek().getValue() % 7;
        LocalDate calendarStart = start.minusDays(dayOfWeek);
        int endDayOfWeek = end.getDayOfWeek().getValue() % 7;
        int daysToAdd = (7 - endDayOfWeek) % 7;
        LocalDate calendarEnd = end.plusDays(daysToAdd);

        List<Map<String, Object>> calendarDates = new ArrayList<>();
        for (LocalDate date = calendarStart; !date.isAfter(calendarEnd); date = date.plusDays(1)) {
            Map<String, Object> dateInfo = new HashMap<>();
            dateInfo.put("date", date);
            dateInfo.put("dayOfMonth", date.getDayOfMonth());
            dateInfo.put("monthValue", date.getMonthValue());
            dateInfo.put("dayOfWeekValue", date.getDayOfWeek().getValue());
            dateInfo.put("dateKey", date.toString());
            dateInfo.put("isCurrentMonth", date.getMonthValue() == month);
            calendarDates.add(dateInfo);
        }

        model.addAttribute("calendarData", calendarData);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("calendarDates", calendarDates);
        model.addAttribute("worshipTypes", WorshipType.values());

        return "schedule/view";
    }
}
