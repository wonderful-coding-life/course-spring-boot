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

// 반환한 객체는 Accept 헤더에 따라 JSON 또는 XML로 응답한다.
@RestController
@RequestMapping("/members")
// final 필드를 받는 생성자로 Repository와 Service를 주입받는다.
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    // GET /members?name=이름: 이름이 없으면 전체 회원을, 있으면 이름이 일치하는 회원을 조회한다.
    @GetMapping
    public List<Member> findAll(
            @RequestParam(name = "name", required = false) String name) {
        // 생략한 값은 null이며, 빈 문자열이나 공백도 검색 조건이 없는 것으로 처리한다.
        if (name == null || name.isBlank()) {
            return memberRepository.findAll();
        }
        return memberRepository.findByName(name);
    }

    // GET /members/회원ID: URL의 ID로 회원을 조회한다. 없으면 404로 응답한다.
    @GetMapping("/{id}")
    public Member findById(@PathVariable("id") Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    // POST /members: 요청 JSON을 Member로 받아 등록하고, 201로 응답한다.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Member create(@RequestBody Member member) {
        // 신규 등록의 ID는 DB가 생성한다. 요청에 ID가 있어도 사용하지 않는다.
        member.setId(null);
        return memberRepository.save(member);
    }

    @PostMapping("/valid")
    @ResponseStatus(HttpStatus.CREATED)
    public Member createValid(@Valid @RequestBody Member member) {
        // 신규 등록의 ID는 DB가 생성한다. 요청에 ID가 있어도 사용하지 않는다.
        member.setId(null);
        return memberRepository.save(member);
    }

    // POST /members/batch: 한 번에 가입 처리하고 생성된 ID가 포함된 회원 목록을 반환한다.
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Member> registerAll(@RequestBody List<Member> members) {
        return memberService.registerAll(members);
    }

    // PUT /members/회원ID: 기존 회원의 이름, 이메일, 나이를 요청 내용으로 변경한다.
    @PutMapping("/{id}")
    public Member update(@PathVariable("id") Long id, @RequestBody Member member) {
        Member found = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        found.setName(member.getName());
        found.setEmail(member.getEmail());
        found.setAge(member.getAge());
        return memberRepository.save(found);
    }

    // PATCH /members/회원ID: 이름, 이메일, 나이 중 전달한 값만 수정한다.
    @PatchMapping("/{id}")
    public Member patch(@PathVariable("id") Long id, @RequestBody Member member) {
        Member found = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // 이 예제에서는 생략하거나 null로 보낸 필드는 기존 값을 유지한다.
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

    // DELETE /members/회원ID: 회원을 삭제하고, 본문 없이 204로 응답한다.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        memberRepository.delete(member);
    }
}
