package com.example.demo.service;

import com.example.demo.dto.ArticleCreateRequest;
import com.example.demo.dto.ArticleResponse;
import com.example.demo.dto.ArticleUpdateRequest;
import com.example.demo.entity.Article;
import com.example.demo.entity.Member;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// 회원 조회, 게시글 처리, DTO 변환을 담당한다.
@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    public ArticleResponse create(ArticleCreateRequest request) {
        // 요청의 회원 ID로 실제 작성자를 찾아 게시글과 연결한다.
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없습니다."));

        Article article = Article.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .member(member)
                .build();
        Article saved = articleRepository.save(article);
        return toResponse(saved);
    }

    public ArticleResponse findById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return toResponse(article);
    }

    public Page<ArticleResponse> findAll(Pageable pageable) {
        Page<Article> page = articleRepository.findAll(pageable);

        // map은 현재 페이지의 게시글마다 toResponse를 호출해 응답 DTO로 바꾼다.
        // 페이지 번호, 전체 게시글 수, 전체 페이지 수는 그대로 유지한다.
        return page.map(article -> toResponse(article));
    }

    public ArticleResponse update(Long id, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        article.setTitle(request.getTitle());
        article.setDescription(request.getDescription());
        // 변경 내용을 저장하고, save가 반환한 객체로 응답을 만든다.
        Article saved = articleRepository.save(article);
        return toResponse(saved);
    }

    public void delete(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        articleRepository.delete(article);
    }

    private ArticleResponse toResponse(Article article) {
        // 관계 매핑된 회원에서 ID와 이름을 꺼내 응답에 담는다.
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .memberId(article.getMember().getId())
                .writerName(article.getMember().getName())
                .created(article.getCreated())
                .updated(article.getUpdated())
                .build();
    }
}
