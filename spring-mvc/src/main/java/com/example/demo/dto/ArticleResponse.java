package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String description;
    private String writerName;
    private LocalDateTime created;
    private LocalDateTime updated;
}
