package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// 기본 CRUD는 JpaRepository의 findAll, findById, save, delete를 그대로 사용한다.
public interface MemberRepository extends JpaRepository<Member, Long> {
}
