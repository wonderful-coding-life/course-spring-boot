package com.example.demo.service;

import com.example.demo.dto.ArticleResponse;
import com.example.demo.entity.Article;
import com.example.demo.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public Page<ArticleResponse> findAll(Pageable pageable) {
        Page<Article> page = articleRepository.findAll(pageable);
        // 각 게시글을 DTO로 바꾸고, 전체 개수와 페이지 정보는 유지한다.
        return page.map(this::toResponse);
    }

    public ArticleResponse findById(Long id) {
        // 조회 결과가 없으면 공통 예외 처리에서 404 화면을 표시한다.
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
        return toResponse(article);
    }

    private ArticleResponse toResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .writerName(article.getMember().getName())
                .created(article.getCreated())
                .updated(article.getUpdated())
                .build();
    }
}
