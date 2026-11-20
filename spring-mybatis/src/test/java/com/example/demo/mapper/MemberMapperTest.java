package com.example.demo.mapper;

import com.example.demo.model.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberMapperTest {
    @Autowired
    private MemberMapper memberMapper;

    @Test
    void testMemberCrud() {
        // 등록 전 회원 수를 확인합니다.
        int countBefore = memberMapper.findAll().size();

        // 회원을 등록합니다.
        Member member = Member.builder()
                .name("Test Member")
                .email("mybatis-test@example.com")
                .age(20)
                .build();

        assertThat(memberMapper.insert(member)).isEqualTo(1);
        assertThat(member.getId()).isNotNull();

        // 등록한 회원과 전체 회원 목록을 조회합니다.
        Member found = memberMapper.findById(member.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test Member");
        assertThat(memberMapper.findAll()).hasSize(countBefore + 1);

        // 회원 정보를 수정합니다.
        found.setName("Updated Member");
        assertThat(memberMapper.update(found)).isEqualTo(1);
        assertThat(memberMapper.findById(member.getId()).getName()).isEqualTo("Updated Member");

        // 회원을 삭제합니다.
        assertThat(memberMapper.deleteById(member.getId())).isEqualTo(1);
        assertThat(memberMapper.findById(member.getId())).isNull();
    }

    @Test
    void testFindByEmail() {
        Member member = Member.builder()
                .name("Email Test Member")
                .email("mybatis-email-test@example.com")
                .age(25)
                .build();
        memberMapper.insert(member);

        Member found = memberMapper.findByEmail(member.getEmail());
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(member.getId());
        assertThat(memberMapper.findByEmail("missing@example.com")).isNull();
    }

    @Test
    void testFindAllOrderBy() {
        memberMapper.insert(Member.builder().name("First").email("mybatis-order-first@example.com").age(20).build());
        memberMapper.insert(Member.builder().name("Second").email("mybatis-order-second@example.com").age(30).build());

        List<Member> members = memberMapper.findAllOrderBy("id", "desc");
        assertThat(members.size()).isGreaterThanOrEqualTo(2);
        for (int i = 1; i < members.size(); i++) {
            assertThat(members.get(i - 1).getId()).isGreaterThan(members.get(i).getId());
        }
    }
}
