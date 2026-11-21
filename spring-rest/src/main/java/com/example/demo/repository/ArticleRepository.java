package com.example.demo.repository;

import com.example.demo.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

// 게시글 CRUD와 findAll(pageable)은 상속받은 메서드를 사용한다.
public interface ArticleRepository extends JpaRepository<Article, Long> {
}
