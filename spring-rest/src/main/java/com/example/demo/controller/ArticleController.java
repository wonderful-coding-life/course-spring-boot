package com.example.demo.controller;

import com.example.demo.dto.ArticleResponse;
import com.example.demo.dto.ArticleRequest;
import com.example.demo.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse create(@RequestParam("memberId") Long memberId, @RequestBody ArticleRequest request) {
        return articleService.create(memberId, request);
    }

    @GetMapping("/{id}")
    public ArticleResponse findById(@PathVariable("id") Long id) {
        return articleService.findById(id);
    }

    @GetMapping
    public Page<ArticleResponse> findAll(
            @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return articleService.findAll(pageable);
    }

    @PutMapping("/{id}")
    public ArticleResponse update(@PathVariable("id") Long id, @RequestBody ArticleRequest request) {
        return articleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        articleService.delete(id);
    }
}
