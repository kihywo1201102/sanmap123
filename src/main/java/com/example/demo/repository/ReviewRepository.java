package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 특정 산(Id)에 해당하는 리얼 리뷰 목록을 번호 역순(최신순)으로 긁어오기
    List<Review> findByMountainIdOrderByIdDesc(Long mountainId);
    // 닉네임 변경 시 기존 리뷰들의 작성자명을 한꺼번에 변경하기 위한 조회 쿼리
    List<Review> findByAuthor(String author);
}