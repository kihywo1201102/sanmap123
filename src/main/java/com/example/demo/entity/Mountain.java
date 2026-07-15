package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mountain")
public class Mountain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String region;
    private String height;

    @Column(name = "difficulty_level")
    private int difficultyLevel;

    @Column(name = "view_score")
    private int viewScore;

    @Column(name = "traffic_score")
    private int trafficScore;

    private String img;

    // --- ⭐️ [롬복 에러 원천 차단] 표준 Getter & Setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getViewScore() {
        return viewScore;
    }

    public void setViewScore(int viewScore) {
        this.viewScore = viewScore;
    }

    public int getTrafficScore() {
        return trafficScore;
    }

    public void setTrafficScore(int trafficScore) {
        this.trafficScore = trafficScore;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}