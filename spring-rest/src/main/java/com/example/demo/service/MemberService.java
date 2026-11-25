package com.example.demo.service;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 여러 회원의 가입을 하나로 묶는다. 중간에 예외가 발생하면 앞의 가입도 취소된다.
    @Transactional
    public List<Member> registerAll(List<Member> members) {
        List<Member> savedMembers = new ArrayList<>();
        for (Member member : members) {
            // 신규 회원의 ID는 DB에서 생성한다.
            member.setId(null);
            Member savedMember = memberRepository.save(member);
            savedMembers.add(savedMember);
        }
        return savedMembers;
    }
}
