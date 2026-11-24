package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 기본 CRUD는 JpaRepository의 findAll, findById, save, delete를 그대로 사용한다.
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이름이 일치하는 회원을 조회한다. 동명이인이 있을 수 있으므로 목록으로 반환한다.
    List<Member> findByName(String name);
}
