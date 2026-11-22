package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 실제 Controller, Service, Repository와 MariaDB를 연결해 확인한다.
// integration 태그가 붙은 테스트는 Gradle의 기본 테스트 실행에서 제외한다.
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    // DisplayName은 실행 결과에 표시할 테스트 이름이다.
    @DisplayName("게시글 조회 API가 제목과 작성자 이름을 응답한다")
    void findArticleById() throws Exception {
        // data.sql로 준비한 게시글에서 실제 ID를 가져온다.
        Article article = articleRepository.findAll().getFirst();

        // perform: 서버 포트를 열지 않고 지정한 HTTP 요청 처리를 실행한다.
        mockMvc.perform(get("/articles/{id}", article.getId()))
                // andDo(print()): 요청·응답을 콘솔에 출력한다.
                .andDo(print())
                // andExpect: 응답 상태가 200인지 확인한다.
                .andExpect(status().isOk())
                // $.title은 title 필드를 선택하고, value는 예상값과 비교한다.
                .andExpect(jsonPath("$.title").value(article.getTitle()))
                .andExpect(jsonPath("$.writerName").value(article.getMember().getName()));
    }
}
