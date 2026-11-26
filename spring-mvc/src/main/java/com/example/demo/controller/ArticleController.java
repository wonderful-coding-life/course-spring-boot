package com.example.demo.controller;

import com.example.demo.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping("/articles")
    public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
                       Pageable pageable, Model model) {
        model.addAttribute("page", articleService.findAll(pageable));
        return "article-list";
    }

    @GetMapping("/articles/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("article", articleService.findById(id));
        return "article-detail";
    }
}
