package com.example.demo.repository;

import com.example.demo.entity.Article;
import com.example.demo.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@SpringBootTest
// sql/schema.sql, sql/data.sql로 준비한 MariaDB를 사용한다.
// 자동 검증 대신 한글 로그로 실행 결과를 확인한다. 테스트가 끝나면 변경 내용은 롤백된다.
@Transactional
class ArticleRepositoryTests {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 샘플 게시글의 작성자인 윤서준을 조회한다.
        member = memberRepository.findByEmail("SeojunYoon@campus.com").orElseThrow();
    }

    @Test
    void saveArticle() {
        // member에 작성자 객체를 넣으면 DB에는 그 회원의 ID가 member_id로 저장된다.
        Article article = Article.builder()
                .title("주말에 한강에서 자전거를 탔어요")
                .description("친구와 여의도에서 반포까지 달렸어요. 강바람이 시원해서 기분이 좋았습니다.").member(member).build();

        articleRepository.save(article);

        log.info("저장한 게시글 ID: {}, 제목: {}", article.getId(), article.getTitle());
        log.info("작성자 ID: {}, 이름: {}", article.getMember().getId(), article.getMember().getName());
        log.info("생성일: {}, 수정일: {}", article.getCreated(), article.getUpdated());
    }

    @Test
    void findArticleAuthor() {
        // 기존 게시글 목록에서 하나를 선택한다. 특정 ID나 조회 순서를 가정하지 않는다.
        Article found = articleRepository.findAll().getFirst();

        log.info("게시글 제목: {}", found.getTitle());
        log.info("작성자 이름: {}, 이메일: {}", found.getMember().getName(), found.getMember().getEmail());
    }

    @Test
    void findArticlesByTitleContaining() {
        List<Article> articles = articleRepository.findByTitleContaining("동네");

        log.info("'동네' 검색 결과: {}개", articles.size());
        for (Article article : articles) {
            log.info("제목: {}", article.getTitle());
        }
    }

    @Test
    void findArticlesByMember() {
        // 회원 객체를 전달해 윤서준이 작성한 기존 게시글을 조회한다.
        List<Article> articles = articleRepository.findByMember(member);

        log.info("윤서준의 게시글: {}개", articles.size());
        for (Article article : articles) {
            log.info("제목: {}, 작성자: {}", article.getTitle(), article.getMember().getName());
        }
    }

    @Test
    void findArticlesByPage() {
        // 0은 첫 페이지, 10은 페이지 크기, descending()은 ID 내림차순이다.
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<Article> page = articleRepository.findAll(pageable);

        // getContent()는 현재 페이지의 목록, getTotalElements()는 모든 페이지를 합친 게시글 수다.
        log.info("전체 게시글 수: {}", page.getTotalElements());
        log.info("전체 페이지 수: {}", page.getTotalPages());
        log.info("현재 페이지의 게시글 수: {}", page.getContent().size());
        for (Article article : page.getContent()) {
            log.info("제목: {}, 작성자: {}", article.getTitle(), article.getMember().getName());
        }
    }
}
