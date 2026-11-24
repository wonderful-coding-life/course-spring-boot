package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 실제 Repository와 H2를 사용하며, 테스트 기본 문법과 검증 방법을 익힌다.
@SpringBootTest
class MemberRepositoryTests {
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트가 같은 데이터로 시작하도록 기본 회원 4명을 준비한다.
        memberRepository.save(Member.builder()
                .name("윤서준").email("seojun@example.com").age(20).build());
        memberRepository.save(Member.builder()
                .name("윤광철").email("kwangcheol@example.com").age(43).build());
        memberRepository.save(Member.builder()
                .name("공미영").email("miyoung@example.com").age(23).build());
        memberRepository.save(Member.builder()
                .name("김도윤").email("doyoon@example.com").age(19).build());
    }

    @AfterEach
    void tearDown() {
        // 자동 롤백 대신 직접 삭제하여 다음 테스트에 데이터를 남기지 않는다.
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("전체 회원 목록에는 준비한 회원 4명이 있다")
    void testFindAllMembers() {
        List<Member> members = memberRepository.findAll();

        assertThat(members).isNotEmpty();
        assertThat(members).hasSize(4);
        assertThat(memberRepository.count()).isEqualTo(4L);
    }

    @Test
    @DisplayName("이름으로 검색한 회원의 정보를 확인한다")
    void testFindMembersByName() {
        List<Member> members = memberRepository.findByName("윤서준");

        assertThat(members).hasSize(1);

        Member found = members.getFirst();
        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("윤서준");
        assertThat(found.getAge()).isGreaterThanOrEqualTo(20);
        assertThat(memberRepository.existsById(found.getId())).isTrue();
    }

    @Disabled("추가 실습: 애너테이션을 제거하고 빈 검색 결과를 확인합니다.")
    @Test
    @DisplayName("없는 이름을 검색하면 빈 목록을 반환한다")
    void testFindMembersByMissingName() {
        List<Member> members = memberRepository.findByName("없는이름");

        assertThat(members).isEmpty();
    }
}
