package com.example.demo.dto;

import lombok.Data;

// 등록 요청에는 작성자 ID와 제목, 본문만 받는다. 게시글 ID와 날짜는 서버에서 만든다.
@Data
public class ArticleCreateRequest {
    private Long memberId;
    private String title;
    private String description;
}
