package com.example.demo.repository;

import com.example.demo.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository를 상속받으면 내부에 자동으로 MySQL용 무기(save, find 등을 처리하는 SQL)가 탑재돼!
public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByUsername(String username); // 나중에 로그인할 때 아이디로 유저 찾는 SQL로 변환됨
}