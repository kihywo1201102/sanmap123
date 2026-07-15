package com.example.demo.repository;

import com.example.demo.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {

    // ⭐️ [수리 완료] 없는 'email' 관련 코드를 완전히 삭제하여 부팅 에러를 박멸합니다!
    Optional<SiteUser> findByUsername(String username);
}