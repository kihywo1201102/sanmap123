package com.example.demo.repository;

import com.example.demo.entity.Mountain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MountainRepository extends JpaRepository<Mountain, Long> {
}