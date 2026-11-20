package com.example.demo.repository;

import com.example.demo.entity.Article;
import com.example.demo.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// 각 테스트가 끝나면 DB에 등록, 수정, 삭제한 내용을 롤백합니다.
@Transactional
class ArticleRepositoryTest {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void articleCrud() {
        Member member = memberRepository.save(Member.builder().name("Article Author")
                .email("jpa-article-test@example.com").age(20).build());

        // 게시글 등록
        Article article = Article.builder().title("First article").description("Hello JPA")
                .created(LocalDateTime.now()).updated(LocalDateTime.now()).member(member).build();
        articleRepository.save(article);
        assertThat(article.getId()).isNotNull();

        // 게시글 조회
        Article found = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("First article");

        // 게시글 수정
        found.setTitle("Updated article");
        articleRepository.save(found);
        assertThat(articleRepository.findById(article.getId()).orElseThrow().getTitle())
                .isEqualTo("Updated article");

        // 게시글 삭제
        articleRepository.delete(found);
        assertThat(articleRepository.findById(article.getId())).isEmpty();
    }

    @Test
    void findArticleAuthor() {
        Member member = memberRepository.save(Member.builder().name("Author")
                .email("jpa-author-test@example.com").age(30).build());
        Article article = articleRepository.save(Article.builder().title("Author's article")
                .description("About the author").member(member).build());

        Article found = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(found.getMember().getName()).isEqualTo("Author");
    }

    @Test
    void findArticlesByPage() {
        Member member = memberRepository.save(Member.builder().name("Page Author")
                .email("jpa-page-test@example.com").age(25).build());
        for (int i = 1; i <= 4; i++) {
            articleRepository.save(Article.builder().title("Article " + i).member(member).build());
        }

        // 페이지 번호는 0부터 시작합니다.
        PageRequest pageable = PageRequest.of(0, 3, Sort.by("id").descending());
        Page<Article> page = articleRepository.findAll(pageable);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(4);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent().get(0).getId()).isGreaterThan(page.getContent().get(1).getId());
    }
}
