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
    // 실제 Repository 대신 가짜 객체를 만든다. SQL을 실행하거나 DB에 접근하지 않는다.
    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private MemberRepository memberRepository;

    // 실제 ArticleService 객체를 만들고, 생성자에 위의 가짜 Repository들을 전달한다.
    // Service 자체는 가짜가 아니므로 실제 메서드 코드가 실행된다.
    @InjectMocks
    private ArticleService articleService;

    @Test
    // DisplayName은 실행 결과에 표시할 테스트 이름이다.
    @DisplayName("게시글을 제목과 작성자 이름이 담긴 응답 DTO로 변환한다")
    // @Disabled("사유")를 붙이면 이 테스트의 실행을 건너뛴다. 클래스에 붙이면 전체를 건너뛴다.
    void findArticleById() {
        // DB에 저장하지 않고 객체만 준비한다. ID도 테스트에서 직접 정한다.
        Member member = Member.builder()
                .id(1L).name("정혁").email("JungHyuk@campus.com").age(10).build();
        Article article = Article.builder()
                .id(1L).title("주말에 한강에서 자전거를 탔어요").member(member).build();

        // when(호출).thenReturn(결과): 가짜 객체가 특정 호출에 돌려줄 결과를 정한다.
        // 이후 findById(1L)가 호출되면 DB 조회 대신 Optional에 담은 article을 반환한다.
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // 실제 Service가 Entity를 응답 DTO로 바꾸는지 확인한다.
        ArticleResponse response = articleService.findById(1L);

        assertThat(response.getTitle()).isEqualTo("주말에 한강에서 자전거를 탔어요");
        assertThat(response.getWriterName()).isEqualTo("정혁");

        // isEqualTo(값): 예상값과 같은지 확인한다.
        // isNotNull(): null이 아닌지 확인한다.
        // isTrue() / isFalse(): 조건이 참인지 / 거짓인지 확인한다.
        // hasSize(개수): 목록 등의 크기가 예상한 개수인지 확인한다.
        // isEmpty(): 목록, 문자열 등이 비어 있는지 확인한다.
    }
}
