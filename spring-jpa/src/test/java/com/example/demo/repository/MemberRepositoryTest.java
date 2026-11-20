package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// 각 테스트가 끝나면 DB에 등록, 수정, 삭제한 내용을 롤백합니다.
@Transactional
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void memberCrud() {
        long countBefore = memberRepository.count();

        // 회원 등록
        Member member = Member.builder().name("Test Member")
                .email("jpa-member-test@example.com").age(20).build();
        memberRepository.save(member);
        assertThat(member.getId()).isNotNull();

        // 회원 조회
        Member found = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Test Member");
        assertThat(memberRepository.findAll()).hasSize((int) countBefore + 1);
        assertThat(memberRepository.findByEmail(member.getEmail())).isPresent();

        // 회원 수정
        found.setName("Updated Member");
        memberRepository.save(found);
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getName())
                .isEqualTo("Updated Member");

        // 회원 삭제
        memberRepository.delete(found);
        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }
}
