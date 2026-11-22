package com.example.demo.repository;

import com.example.demo.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@SpringBootTest
// sql/schema.sql, sql/data.sql로 준비한 MariaDB를 사용한다(회원 4명).
// 자동 검증 대신 한글 로그로 실행 결과를 확인한다. 테스트가 끝나면 변경 내용은 롤백된다.
@Transactional
class MemberRepositoryTests {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void saveMember() {
        log.info("등록 전 회원 수: {}", memberRepository.count());

        Member member = Member.builder().name("정혁")
                .email("JungHyuk@campus.com").age(10).build();
        memberRepository.save(member);

        log.info("등록한 회원: {}", member);
        log.info("등록 후 회원 수: {}", memberRepository.count());
    }

    @Test
    void rejectDuplicateEmail() {
        // 윤서준의 이메일을 다시 사용하면 DB의 UNIQUE 제약으로 예외가 발생한다.
        Member duplicate = Member.builder().name("정혁")
                .email("SeojunYoon@campus.com").age(10).build();

        try {
            memberRepository.save(duplicate);
            log.warn("중복 이메일이 저장되었습니다. DB의 UNIQUE 제약을 확인하세요.");
        } catch (DataIntegrityViolationException e) {
            log.info("이메일 중복으로 저장할 수 없습니다: {}", e.getMessage());
        }
        // 예외가 발생한 뒤에는 추가 DB 작업 없이 테스트를 끝내고 롤백한다.
    }

    @Test
    void findMemberById() {
        // 이메일로 찾은 회원의 실제 ID를 사용해 조회한다.
        Member member = memberRepository.findByEmail("SeojunYoon@campus.com").orElseThrow();
        Member found = memberRepository.findById(member.getId()).orElseThrow();
        log.info("ID로 조회한 회원: {}", found);
    }

    @Test
    void findAllMembers() {
        List<Member> members = memberRepository.findAll();
        log.info("전체 회원 수: {}", members.size());
        for (Member member : members) {
            log.info("회원: {}", member);
        }
    }

    @Test
    void findMemberByEmail() {
        Member member = memberRepository.findByEmail("KwangcheolYoon@campus.com").orElseThrow();
        log.info("이메일로 조회한 회원: {}", member);
    }

    @Test
    void findMembersByName() {
        List<Member> members = memberRepository.findByName("윤서준");
        for (Member member : members) {
            log.info("이름이 윤서준인 회원: {}", member);
        }
        log.info("없는 이름으로 조회한 결과: {}", memberRepository.findByName("없는회원"));
    }

    @Test
    void findMembersByNameContaining() {
        // 윤서준, 윤광철, 김도윤의 이름에는 모두 '윤'이 들어간다.
        List<Member> members = memberRepository.findByNameContaining("윤");
        log.info("이름에 '윤'이 포함된 회원 수: {}", members.size());
        for (Member member : members) {
            log.info("검색된 회원: {}", member);
        }
    }

    @Test
    void findMembersByAgeGreaterThanEqual() {
        // 23세인 공미영과 43세인 윤광철이 조회된다.
        List<Member> members = memberRepository.findByAgeGreaterThanEqual(23);
        for (Member member : members) {
            log.info("23세 이상인 회원: {}", member);
        }
    }

    @Test
    void findMembersByNameAndAgeGreaterThanEqual() {
        List<Member> members = memberRepository.findByNameAndAgeGreaterThanEqual("윤광철", 43);
        for (Member member : members) {
            log.info("이름이 윤광철이고 43세 이상인 회원: {}", member);
        }
        log.info("윤광철이면서 44세 이상인 회원: {}",
                memberRepository.findByNameAndAgeGreaterThanEqual("윤광철", 44));
        log.info("없는 이름이면서 10세 이상인 회원: {}",
                memberRepository.findByNameAndAgeGreaterThanEqual("없는회원", 10));
    }

    @Test
    void existsMemberByEmail() {
        log.info("윤서준의 이메일이 존재하는가? {}",
                memberRepository.existsByEmail("SeojunYoon@campus.com"));
        log.info("미등록 이메일이 존재하는가? {}",
                memberRepository.existsByEmail("missing@example.com"));
    }

    @Test
    void updateMember() {
        // 수정할 회원은 ID를 가정하지 않고 이메일로 찾는다.
        Member member = memberRepository.findByEmail("SeojunYoon@campus.com").orElseThrow();
        log.info("수정 전 회원: {}", member);

        member.setName("수정된 윤서준");
        memberRepository.save(member);

        // 관리 중인 객체의 변경 결과다. 테스트가 끝나면 롤백된다.
        log.info("수정 후 회원: {}", memberRepository.findById(member.getId()).orElseThrow());
    }

    @Test
    void deleteMember() {
        // 샘플 회원은 모두 게시글이 있으므로, 삭제할 회원을 따로 등록한다.
        Member member = Member.builder().name("정혁")
                .email("JungHyuk@campus.com").age(10).build();
        memberRepository.save(member);
        memberRepository.delete(member);

        log.info("삭제 후에도 회원이 조회되는가? {}",
                memberRepository.findById(member.getId()).isPresent());
        log.info("삭제 후 회원 수: {}", memberRepository.count());
    }

    @Test
    void sameMemberInPersistenceContext() {
        // 이메일로 찾은 회원을 실제 ID와 이메일로 다시 조회한다.
        Member member = memberRepository.findByEmail("SeojunYoon@campus.com").orElseThrow();
        Member foundById = memberRepository.findById(member.getId()).orElseThrow();
        Member foundByEmail = memberRepository.findByEmail("SeojunYoon@campus.com").orElseThrow();

        // 같은 영속성 컨텍스트에서 같은 회원은 같은 객체로 관리된다. ==로 비교한다.
        log.info("ID로 다시 조회한 회원과 같은 객체인가? {}", member == foundById);
        log.info("이메일로 조회한 회원과 같은 객체인가? {}", member == foundByEmail);
    }
}
