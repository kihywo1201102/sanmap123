package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();

    // ⭐️ [핵심 추가] 특정 산의 ID에 매핑된 자랑글만 최신순으로 쏙쏙 가져오는 쿼리!
    List<Post> findByMountainIdOrderByCreatedAtDesc(Long mountainId);

    // 내 닉네임으로 작성된 모든 자랑글을 최신순으로 가져오는 쿼리
    List<Post> findByAuthorOrderByCreatedAtDesc(String author);

    // 닉네임 변경 시 기존 글들의 작성자명을 한꺼번에 변경하기 위한 조회 쿼리
    List<Post> findByAuthor(String author);

}