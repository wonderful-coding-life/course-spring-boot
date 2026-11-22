package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 로그인할 때 입력한 이메일로 회원을 조회한다.
    Optional<Member> findByEmail(String email);
}
