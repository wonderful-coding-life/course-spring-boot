package com.example.demo.dto;

import lombok.Data;

// 등록과 수정 요청의 제목과 본문을 받는다.
@Data
public class ArticleRequest {
    private String title;
    private String description;
}
