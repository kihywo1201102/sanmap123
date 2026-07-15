package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String difficulty; // 난이도
    private int viewRating;    // 별점 평점
    private String transport;  // 교통 편의성

    @Column(columnDefinition = "TEXT")
    private String content;    // 한줄 평

    private String author;     // 작성자 닉네임
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "mountain_id")
    private Mountain mountain; // 대상 산 정보 매핑

    // --- Getter & Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public int getViewRating() { return viewRating; }
    public void setViewRating(int viewRating) { this.viewRating = viewRating; }
    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Mountain getMountain() { return mountain; }
    public void setMountain(Mountain mountain) { this.mountain = mountain; }
}