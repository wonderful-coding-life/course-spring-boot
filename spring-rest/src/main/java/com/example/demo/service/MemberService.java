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

    @Transactional
    public List<Member> registerAll(List<Member> members) {
        List<Member> savedMembers = new ArrayList<>();
        for (Member member : members) {
            member.setId(null);
            Member savedMember = memberRepository.save(member);
            savedMembers.add(savedMember);
        }
        return savedMembers;
    }
}
