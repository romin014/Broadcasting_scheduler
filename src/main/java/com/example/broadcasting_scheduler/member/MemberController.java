package com.example.broadcasting_scheduler.member;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.broadcasting_scheduler.member.dto.MemberRequestDto;
import com.example.broadcasting_scheduler.member.dto.MemberResponseDto;

@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/list")
    public String listPage(Model model) {
        List<MemberResponseDto> members = memberService.getAllMembers();
        model.addAttribute("members", members);
        model.addAttribute("specialties", Specialty.values());
        return "member/list";
    }

    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("member", new MemberRequestDto());
        model.addAttribute("specialties", Specialty.values());
        return "member/form";
    }

    @PostMapping("/add")
    public String addMember(@ModelAttribute MemberRequestDto memberRequestDto) {
        memberService.createMember(memberRequestDto);
        return "redirect:/member/list";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        MemberResponseDto member = memberService.getMemberById(id);
        MemberRequestDto requestDto = new MemberRequestDto();
        requestDto.setName(member.getName());
        requestDto.setSpecialty(member.getSpecialty());

        model.addAttribute("member", requestDto);
        model.addAttribute("memberId", id);
        model.addAttribute("specialties", Specialty.values());
        return "member/form";
    }

    @PostMapping("/edit/{id}")
    public String updateMember(@PathVariable Long id, @ModelAttribute MemberRequestDto memberRequestDto) {
        memberService.updateMember(id, memberRequestDto);
        return "redirect:/member/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return "redirect:/member/list";
    }

    @GetMapping("/availability/{id}")
    public String availabilityPage(@PathVariable Long id, Model model) {
        MemberResponseDto member = memberService.getMemberById(id);
        var availabilityList = memberService.getMemberAvailability(id);

        model.addAttribute("memberId", id);
        model.addAttribute("memberName", member.getName());
        model.addAttribute("availabilityList", availabilityList);
        model.addAttribute("daysOfWeek", new String[] { "월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일" });
        return "member/availability";
    }

    @PostMapping("/availability/{id}")
    public String updateAvailability(@PathVariable Long id,
            @RequestParam(required = false) java.util.Map<String, String> params) {
        memberService.saveMemberAvailability(id, params);
        return "redirect:/member/list";
    }
}
