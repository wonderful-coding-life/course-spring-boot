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
    // @Disabled("사유")를 붙이면 이 테스트의 실행을 건너뛴다. 클래스에 붙이면 전체를 건너뛴다.
    void findArticleById() throws Exception {
        // data.sql로 준비한 게시글에서 실제 ID를 가져온다.
        Article article = articleRepository.findAll().getFirst();

        // perform: 서버 포트를 열지 않고 지정한 HTTP 요청 처리를 실행한다.
        // jsonPath는 응답 JSON에서 확인할 값을 선택한다.
        // $: JSON 전체의 시작점 / .필드명: 객체의 필드 / [0]: 배열의 첫 항목
        // 예: $.title은 제목, $.member.name은 중첩 객체의 이름, $.content[0].title은 목록의 첫 제목이다.
        // 배열 전체가 응답이면 $[0].title을 사용한다. [*]는 배열의 모든 항목을 뜻한다.
        // 위의 중첩 객체와 배열 경로는 문법 예시이며, 실제 응답 구조에 맞춰 사용한다.
        mockMvc.perform(get("/articles/{id}", article.getId()))
                // andDo(print()): 요청 URL, 응답 상태, 응답 본문 등을 콘솔에 출력한다.
                .andDo(print())
                // andExpect: 예상 결과와 다르면 테스트가 실패한다. 여기서는 HTTP 200을 확인한다.
                .andExpect(status().isOk())
                // value: jsonPath로 선택한 값이 괄호 안의 예상값과 같은지 확인한다.
                .andExpect(jsonPath("$.title").value(article.getTitle()))
                .andExpect(jsonPath("$.writerName").value(article.getMember().getName()));
    }
}
