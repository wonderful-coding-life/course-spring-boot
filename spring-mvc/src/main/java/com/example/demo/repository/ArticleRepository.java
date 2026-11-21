package com.example.demo.repository;

import com.example.demo.entity.Article;
import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    // 작성한 게시글이 있는 회원은 삭제하지 않도록 확인한다.
    boolean existsByMember(Member member);
}
