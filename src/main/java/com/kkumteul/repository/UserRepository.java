package com.kkumteul.repository;

import com.kkumteul.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Google ID로 사용자 조회 (구글 로그인 식별용)
    Optional<User> findByGoogleId(String googleId);

    // 2. 이메일로 사용자 조회 (중복 가입 방지 및 계정 식별용)
    Optional<User> findByEmail(String email);
}