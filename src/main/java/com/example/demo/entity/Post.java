package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // 고유 식별자 (PK)

    private String title;       // 후기 제목

    @Column(columnDefinition = "TEXT")
    private String content;     // 후기 내용

    private String imageUrl;    // 사진 저장 경로

    private String author;      // [필수 추가] 작성자 닉네임

    private LocalDateTime createdAt = LocalDateTime.now(); // 작성 시간 기본값 설정

    // 어느 산에 대한 후기인지 연결
    @ManyToOne
    @JoinColumn(name = "mountain_id")
    private Mountain mountain;

    // --- ⭐️ [에러 영원히 박멸] 직접 만든 Getter & Setter 메서드 ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Mountain getMountain() {
        return mountain;
    }

    public void setMountain(Mountain mountain) {
        this.mountain = mountain;
    }
}