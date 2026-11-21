package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Entity를 그대로 반환하지 않고, 응답에 필요한 정보만 담는다.
@Getter
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String description;
    private Long memberId;
    // Member 객체 전체 대신 작성자 이름을 제공한다.
    private String writerName;
    private LocalDateTime created;
    private LocalDateTime updated;
}
