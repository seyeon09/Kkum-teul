package com.kkumteul.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "mandalarts") //  DB 테이블 이름
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA의 기본 생성자
public class Mandalart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mandalart_id") // PK 이름
    private Long id;

    // 여러 개의 만다라트가 한 명의 유저에게 속함
    // FetchType.LAZY를 써서 유저 정보가 당장 필요 없을 때의 성능 최적화
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title; // 만다라트의 전체 주제를 담는 필드

    @CreationTimestamp // 서버 시간을 직접 안 넣어도 DB에 들어갈 때 자동 생성일
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 만다라트 한 판에 9개의 카테고리 블록
    // CascadeType.ALL -> 만다라트만 저장/삭제해도 카테고리가 알아서 따라옴
    @OneToMany(mappedBy = "mandalart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories = new ArrayList<>();

    // 생성자-> 유저와 제목을 필수로 받아서 '누구의 어떤 목표'인지 확실히
    public Mandalart(User user, String title) {
        this.user = user;
        this.title = title;
        // 유저 객체 리스트에도 나를 추가해주는 센스! [cite: 2025-09-29]
        if (user != null) {
            user.getMandalarts().add(this);
        }
    }
}