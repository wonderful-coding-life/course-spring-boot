package com.example.demo.service;

import com.example.demo.dto.ArticleResponse;
import com.example.demo.entity.Article;
import com.example.demo.entity.Member;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// 스프링과 DB 없이 Mockito로 Service만 확인한다.
// JUnit에 Mockito를 연결해 @Mock, @InjectMocks가 붙은 필드를 준비한다.
@ExtendWith(MockitoExtension.class)
class ArticleServiceTests {
    // DB에 접근하지 않는 가짜 Repository를 만든다.
    @Mock
    private ArticleRepository articleRepository;

    // 조회 테스트에서는 호출하지 않지만, Service의 생성자 의존성을 채우기 위해 준비한다.
    @Mock
    private MemberRepository memberRepository;

    // Mockito가 실제 Service 객체를 만들고 가짜 Repository들을 생성자로 주입한다.
    @InjectMocks
    private ArticleService articleService;

    @Test
    @DisplayName("게시글을 제목과 작성자 이름이 담긴 응답 DTO로 변환한다")
    void testFindArticleById() {
        // DB에 저장하지 않고 객체만 준비한다. ID도 테스트에서 직접 정한다.
        Member member = Member.builder()
                .id(1L).name("정혁").email("JungHyuk@campus.com").age(10).build();
        Article article = Article.builder()
                .id(1L).title("주말에 한강에서 자전거를 탔어요").member(member).build();

        // findById(1L)를 호출하면 DB 조회 대신 준비한 article을 반환한다.
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // 실제 Service가 Entity를 응답 DTO로 바꾸는지 확인한다.
        ArticleResponse response = articleService.findById(1L);

        assertThat(response.getTitle()).isEqualTo("주말에 한강에서 자전거를 탔어요");
        assertThat(response.getWriterName()).isEqualTo("정혁");
    }
}
