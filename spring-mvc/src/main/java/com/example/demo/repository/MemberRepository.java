package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 회원가입에서 같은 이메일을 사용하는 회원이 있는지 확인한다.
    Optional<Member> findByEmail(String email);
}
