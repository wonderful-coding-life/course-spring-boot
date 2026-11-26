package com.example.demo.controller;

import com.example.demo.entity.Member;
import com.example.demo.form.MemberForm;
import com.example.demo.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.NoSuchElementException;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @GetMapping("/members")
    public String list(Model model) {
        model.addAttribute("members", memberRepository.findAll());
        return "member-list";
    }

    @GetMapping("/members/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("member", findMember(id));
        return "member-detail";
    }

    @GetMapping("/members/new")
    public String addForm() {
        return "member-add";
    }

    @PostMapping("/members")
    public String add(Member member) {
        member.setId(null);
        memberRepository.save(member);
        return "redirect:/members";
    }

    @GetMapping("/members/edit")
    public String editForm(@RequestParam("id") Long id, Model model) {
        model.addAttribute("member", findMember(id));
        return "member-edit";
    }

    @PostMapping("/members/edit")
    public String edit(Member member) {
        memberRepository.save(member);
        return "redirect:/members";
    }

    @GetMapping("/signup")
    public String signupForm(@ModelAttribute("memberForm") MemberForm memberForm) {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("memberForm") MemberForm memberForm,
                         BindingResult bindingResult) {
        // @Valid가 폼의 규칙을 검사한다. BindingResult는 폼 바로 뒤에 둔다.

        // 입력된 패스워드 두개가 서로 일치하는지 확인한다
        if (!bindingResult.hasFieldErrors("password")
                && !bindingResult.hasFieldErrors("passwordConfirm")
                && !Objects.equals(memberForm.getPassword(), memberForm.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "mismatch", "비밀번호가 일치하지 않습니다.");
        }
        // 형식 검증을 통과한 이메일에 대해서만 중복을 조회한다.
        if (!bindingResult.hasFieldErrors("email")
                && memberRepository.findByEmail(memberForm.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "duplicate", "이미 사용 중인 이메일입니다.");
        }
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        Member member = Member.builder()
                .name(memberForm.getName())
                .email(memberForm.getEmail())
                .password(memberForm.getPassword())
                .build();
        memberRepository.save(member);
        return "redirect:/";
    }

    @GetMapping("/members/delete")
    public String delete(@RequestParam("id") Long id) {
        Member member = findMember(id);
        memberRepository.delete(member);
        return "redirect:/members";
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));
    }
}
