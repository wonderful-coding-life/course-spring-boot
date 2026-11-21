package com.example.demo.mapper;

import com.example.demo.model.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@SpringBootTest
// sql/schema.sql, sql/data.sql로 준비한 MariaDB를 사용한다.
// 자동 검증 대신 로그로 결과를 확인한다. 테스트가 끝나면 변경 내용은 롤백된다.
@Transactional
class MemberMapperTests {
    @Autowired
    private MemberMapper memberMapper;

    @Test
    void testInsertMember() {
        log.info("등록 전 회원 수: {}", memberMapper.findAll().size());

        // 새 회원을 등록하면 DB에서 생성한 ID가 member에 들어간다.
        Member member = Member.builder()
                .name("정혁")
                .email("JungHyuk@campus.com")
                .age(10)
                .build();
        int insertedRows = memberMapper.insert(member);
        log.info("등록한 행 수: {}, 생성된 회원 ID: {}", insertedRows, member.getId());
        log.info("등록한 회원: {}", member);
        log.info("등록 후 회원 수: {}", memberMapper.findAll().size());
    }

    @Test
    void testFindById() {
        // 샘플 이메일로 찾은 회원의 실제 ID를 사용해 조회한다.
        Member member = memberMapper.findByEmail("SeojunYoon@campus.com");
        Member found = memberMapper.findById(member.getId());
        log.info("ID로 조회한 회원: {}", found);
    }

    @Test
    void testUpdateMember() {
        // 기존 샘플 회원을 조회해 나이를 변경한다. 테스트 후에는 롤백된다.
        Member member = memberMapper.findByEmail("SeojunYoon@campus.com");
        log.info("수정 전 회원: {}", member);

        member.setAge(11);
        int updatedRows = memberMapper.update(member);
        log.info("수정한 행 수: {}", updatedRows);
        log.info("수정 후 회원: {}", memberMapper.findById(member.getId()));
    }

    @Test
    void testDeleteMember() {
        // 기존 회원은 게시글이 있을 수 있으므로, 삭제할 회원을 따로 등록한다.
        Member member = Member.builder()
                .name("정혁")
                .email("JungHyuk@campus.com")
                .age(10)
                .build();
        memberMapper.insert(member);
        log.info("삭제 전 회원: {}", memberMapper.findById(member.getId()));

        // 삭제 후 같은 ID로 조회하면 null이 나온다.
        int deletedRows = memberMapper.deleteById(member.getId());
        log.info("삭제한 행 수: {}", deletedRows);
        log.info("삭제 후 조회 결과: {}", memberMapper.findById(member.getId()));
        log.info("삭제 후 회원 수: {}", memberMapper.findAll().size());
    }

    @Test
    void testFindByEmail() {
        // 기존 샘플 회원의 이메일로 조회한다.
        Member found = memberMapper.findByEmail("SeojunYoon@campus.com");
        log.info("이메일로 조회한 회원: {}", found);

        // 등록되지 않은 이메일이면 null을 반환한다.
        log.info("없는 이메일로 조회한 결과: {}", memberMapper.findByEmail("missing@example.com"));
    }

    @Test
    void testFindAllOrderBy() {
        // id, desc를 전달해 기존 회원을 ID가 큰 순서로 조회한다.
        List<Member> members = memberMapper.findAllOrderBy("id", "desc");
        log.info("ID 내림차순으로 조회한 회원 수: {}", members.size());
        for (Member member : members) {
            log.info("회원 ID: {}, 이름: {}, 나이: {}", member.getId(), member.getName(), member.getAge());
        }
    }
}
