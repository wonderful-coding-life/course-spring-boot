package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 클래스명과 테이블명이 다르면 @Table(name), 필드명과 컬럼명이 다르면 @Column(name)으로 매핑한다.
// 회원을 참조하는 외래 키 컬럼명은 @JoinColumn(name)으로 매핑한다.
@Entity
@Table(name = "article")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "title")
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name = "created")
    private LocalDateTime created;
    @Column(name = "updated")
    private LocalDateTime updated;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    // 처음 저장하기 전에 생성일과 수정일을 현재 시간으로 설정한다.
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        created = now;
        updated = now;
    }

    // 변경 내용을 DB에 반영하기 전에 수정일만 현재 시간으로 갱신한다.
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}
