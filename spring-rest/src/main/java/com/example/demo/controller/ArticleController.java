package com.example.demo.controller;

import com.example.demo.dto.ArticleCreateRequest;
import com.example.demo.dto.ArticleResponse;
import com.example.demo.dto.ArticleUpdateRequest;
import com.example.demo.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Controller는 요청을 받아 Service에 전달하고 DTO를 JSON으로 응답한다.
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    // POST /articles: 요청 DTO로 게시글을 등록하고 201로 응답한다.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse create(@RequestBody ArticleCreateRequest request) {
        return articleService.create(request);
    }

    // GET /articles/게시글ID: 작성자 이름을 포함한 게시글을 조회한다.
    @GetMapping("/{id}")
    public ArticleResponse findById(@PathVariable("id") Long id) {
        return articleService.findById(id);
    }

    // GET /articles?page=0&size=5&sort=id,desc: 페이지 번호, 크기, 정렬 조건을 받는다.
    // PageableDefault는 요청에서 생략한 조건의 기본값이다. 페이지 번호는 0부터 시작한다.
    @GetMapping
    public Page<ArticleResponse> findAll(
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return articleService.findAll(pageable);
    }

    // PUT /articles/게시글ID: 제목과 본문을 수정한다.
    @PutMapping("/{id}")
    public ArticleResponse update(@PathVariable("id") Long id, @RequestBody ArticleUpdateRequest request) {
        return articleService.update(id, request);
    }

    // DELETE /articles/게시글ID: 삭제 후 본문 없이 204로 응답한다.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        articleService.delete(id);
    }
}
