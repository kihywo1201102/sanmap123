package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();

    // ⭐️ [핵심 추가] 특정 산의 ID에 매핑된 자랑글만 최신순으로 쏙쏙 가져오는 쿼리!
    List<Post> findByMountainIdOrderByCreatedAtDesc(Long mountainId);
}