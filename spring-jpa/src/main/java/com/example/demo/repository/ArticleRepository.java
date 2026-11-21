package com.example.demo.repository;

import com.example.demo.entity.Article;
import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Article은 다룰 엔티티, Long은 게시글 ID의 타입이다.
 * 아래 기본 메서드는 JpaRepository에서 상속받으므로 따로 선언하지 않아도 된다.
 * - save(article): 새 게시글을 저장하거나, 조회한 게시글의 내용을 수정해 저장한다.
 * - findById(id): ID로 한 건을 조회한다. 없으면 빈 Optional을 반환한다.
 * - findAll(): 모든 게시글을 List로 반환한다.
 * - deleteById(id): 해당 ID의 게시글을 삭제한다.
 * - existsById(id): 해당 ID의 게시글이 있으면 true, 없으면 false를 반환한다.
 * - count(): 전체 게시글 수를 반환한다.
 * - findAll(pageable): 요청한 페이지의 게시글과 전체 개수 등의 정보를 Page로 반환한다.
 */
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Pageable은 페이지 번호, 한 페이지의 개수, 정렬 조건을 담는다. 페이지 번호는 0부터 시작한다.
    // 예: PageRequest.of(0, 10, Sort.by("id").descending())는 ID 내림차순으로 첫 10건을 요청한다.
    // Page의 getContent()는 조회된 목록, getTotalElements()는 전체 개수, getTotalPages()는 전체 페이지 수다.

    // 제목에 keyword가 포함된 게시글 목록을 조회한다.
    List<Article> findByTitleContaining(String keyword);

    // 해당 회원이 작성한 게시글 목록을 조회한다. 회원의 ID를 기준으로 검색한다.
    List<Article> findByMember(Member member);
}
