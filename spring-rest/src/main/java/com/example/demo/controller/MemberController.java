package com.example.demo.controller;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import com.example.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @GetMapping
    public List<Member> findAll(@RequestParam(name = "name", required = false) String name) {
        if (name == null || name.isBlank()) {
            return memberRepository.findAll();
        }
        return memberRepository.findByName(name);
    }

    @GetMapping("/{id}")
    public Member findById(@PathVariable("id") Long id) {
        return findMember(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Member create(@RequestBody Member member) {
        member.setId(null);
        return memberRepository.save(member);
    }

    @PostMapping("/valid")
    @ResponseStatus(HttpStatus.CREATED)
    public Member createValid(@Valid @RequestBody Member member) {
        member.setId(null);
        return memberRepository.save(member);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Member> registerAll(@RequestBody List<Member> members) {
        return memberService.registerAll(members);
    }

    @PutMapping("/{id}")
    public Member update(@PathVariable("id") Long id, @RequestBody Member member) {
        Member found = findMember(id);

        found.setName(member.getName());
        found.setEmail(member.getEmail());
        found.setAge(member.getAge());
        return memberRepository.save(found);
    }

    @PatchMapping("/{id}")
    public Member patch(@PathVariable("id") Long id, @RequestBody Member member) {
        Member found = findMember(id);

        if (member.getName() != null) {
            found.setName(member.getName());
        }
        if (member.getEmail() != null) {
            found.setEmail(member.getEmail());
        }
        if (member.getAge() != null) {
            found.setAge(member.getAge());
        }
        return memberRepository.save(found);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        Member member = findMember(id);
        memberRepository.delete(member);
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
