package com.kkumteul.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "google_id", nullable = false, unique = true)
    private String googleId;

    @Column(name = "email", length = 255, unique = true) // 이메일도 중복 가입 방지를 위해 unique 추가!
    private String email;

    @Column(name = "name", length = 100)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mandalart> mandalarts = new ArrayList<>();

    // 1. 생성자 수정: 서비스 코드(name, email, googleId)와 순서를 맞췄어!
    public User(String name, String email, String googleId) {
        this.name = name;
        this.email = email;
        this.googleId = googleId;
    }

    // 2. 비즈니스 로직: 이름만 업데이트하는 기능 (서비스에서 사용함)
    public User updateName(String name) {
        this.name = name;
        return this; // 서비스의 .map() 안에서 자연스럽게 연결되도록 자기 자신을 반환해!
    }

    // 3. 기존 프로필 전체 수정 메서드도 유지
    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
    }
}