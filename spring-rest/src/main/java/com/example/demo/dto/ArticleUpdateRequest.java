package com.example.demo.dto;

import lombok.Data;

// 수정할 때는 제목과 본문만 받는다. 기존 작성자는 바꾸지 않는다.
@Data
public class ArticleUpdateRequest {
    private String title;
    private String description;
}
