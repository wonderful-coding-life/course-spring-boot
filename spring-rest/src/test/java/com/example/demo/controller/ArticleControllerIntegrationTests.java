package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.entity.Member;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 실제 Controller, Service, Repository와 H2를 연결해 확인한다.
@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Article article;

    @BeforeEach
    void setUp() {
        // 외부 DB의 샘플 데이터에 의존하지 않고 테스트에 필요한 데이터만 준비한다.
        Member member = memberRepository.save(Member.builder()
                .name("정혁").email("article-test@example.com").age(10).build());
        article = articleRepository.save(Article.builder()
                .title("주말에 한강에서 자전거를 탔어요").member(member).build());
    }

    @AfterEach
    void tearDown() {
        // 게시글이 회원을 참조하므로 게시글부터 삭제한다.
        articleRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    // DisplayName은 실행 결과에 표시할 테스트 이름이다.
    @DisplayName("게시글 조회 API가 제목과 작성자 이름을 응답한다")
    void testFindArticleById() throws Exception {
        // perform: 서버 포트를 열지 않고 지정한 HTTP 요청 처리를 실행한다.
        mockMvc.perform(get("/articles/{id}", article.getId()))
                // andExpect: 응답 상태가 200인지 확인한다.
                .andExpect(status().isOk())
                // $.title은 title 필드를 선택하고, value는 예상값과 비교한다.
                .andExpect(jsonPath("$.title").value(article.getTitle()))
                .andExpect(jsonPath("$.writerName").value(article.getMember().getName()));
    }
}
