package com.example.demo.controller;

import com.example.demo.entity.Member;
import com.example.demo.form.MemberForm;
import com.example.demo.repository.ArticleRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

// 기본 회원 등록 다음에 검증을 추가한 등록 메서드를 비교한다.
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;

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
    public String addForm(@ModelAttribute("member") Member member) {
        // 빈 회원 객체를 전달해 입력칸과 연결한다.
        return "member-add";
    }

    @PostMapping("/members")
    public String add(@ModelAttribute("member") Member member) {
        // 입력한 값을 엔티티로 받고, ID는 DB가 새로 부여하게 한다.
        member.setId(null);
        memberRepository.save(member);
        // 저장 후 목록으로 이동해 새로고침해도 다시 등록되지 않게 한다.
        return "redirect:/members";
    }

    // 기본 등록에 입력 검증을 추가한 버전이다.
    @GetMapping("/members/new-validation")
    public String addValidationForm(@ModelAttribute("memberForm") MemberForm form) {
        return "member-add-validation";
    }

    @PostMapping("/members/validation")
    public String addValidation(@Valid @ModelAttribute("memberForm") MemberForm form,
                                BindingResult bindingResult) {
        // @Valid가 폼의 검증 규칙을 확인한다. BindingResult는 바로 뒤에 두어 오류를 받는다.
        if (memberRepository.findByEmail(form.getEmail()).isPresent()) {
            // 이메일 중복처럼 DB 조회가 필요한 검증은 직접 추가한다.
            bindingResult.rejectValue("email", "duplicate", "이미 사용 중인 이메일입니다.");
        }
        if (bindingResult.hasErrors()) {
            // 같은 화면을 반환하면 입력값과 오류 메시지가 유지된다.
            return "member-add-validation";
        }

        // 검증을 통과한 폼의 값으로 Member 엔티티를 만들어 저장한다.
        Member member = Member.builder()
                .name(form.getName()).email(form.getEmail()).age(form.getAge()).build();
        memberRepository.save(member);
        return "redirect:/members";
    }

    @GetMapping("/members/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("member", findMember(id));
        return "member-edit";
    }

    @PostMapping("/members/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @ModelAttribute("member") Member member) {
        // URL의 ID로 회원을 찾고, 입력한 값으로 바꾸어 저장한다.
        Member found = findMember(id);
        found.setName(member.getName());
        found.setEmail(member.getEmail());
        found.setAge(member.getAge());
        memberRepository.save(found);
        return "redirect:/members";
    }

    @PostMapping("/members/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Member member = findMember(id);
        // 게시글의 작성자 정보가 사라지지 않도록 삭제를 제한한다.
        if (articleRepository.existsByMember(member)) {
            // 다음 화면에서 한 번 표시할 메시지를 전달한다.
            redirectAttributes.addFlashAttribute("message", "작성한 게시글이 있는 회원은 삭제할 수 없습니다.");
            return "redirect:/members/" + id;
        }
        memberRepository.delete(member);
        return "redirect:/members";
    }

    private Member findMember(Long id) {
        // 조회 결과가 없으면 공통 예외 처리에서 404 화면을 표시한다.
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));
    }
}
