package com.example.demo.repository;

import com.example.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 자랑글(postId)에 달린 댓글들을 등록 순서대로 조회
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}