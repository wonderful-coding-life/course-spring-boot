package com.example.demo.controller;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// 반환한 Member 객체와 목록은 JSON으로 응답한다.
@RestController
@RequestMapping("/members")
// final 필드를 받는 생성자를 만들어 Repository를 주입받는다.
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    // GET /members: 전체 회원을 조회한다.
    @GetMapping
    public List<Member> findAll() {
        return memberRepository.findAll();
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

    // DELETE /members/회원ID: 회원을 삭제하고, 본문 없이 204로 응답한다.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        memberRepository.delete(member);
    }
}
