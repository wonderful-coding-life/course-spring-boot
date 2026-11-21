package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 기본 CRUD는 JpaRepository의 findAll, findById, save, delete를 그대로 사용한다.
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일이 일치하는 회원을 조회한다. 없으면 빈 Optional을 반환한다.
    Optional<Member> findByEmail(String email);
}
