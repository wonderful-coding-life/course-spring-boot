package com.example.demo.repository;

import com.example.demo.entity.Article;
import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 기본 CRUD는 JpaRepository에서 상속받는다.
// findAll(pageable)은 게시글 목록과 전체 개수 등의 페이지 정보를 반환한다.
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 제목에 keyword가 포함된 게시글 목록을 조회한다.
    List<Article> findByTitleContaining(String keyword);

    // 해당 회원이 작성한 게시글 목록을 조회한다. 회원의 ID를 기준으로 검색한다.
    List<Article> findByMember(Member member);
}
