package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 화면에서 사용할 게시글 정보만 담는다. 작성자는 이름으로 제공한다.
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
