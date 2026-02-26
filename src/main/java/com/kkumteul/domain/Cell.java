package com.kkumteul.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cells")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cell_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "cell_index", nullable = false)
    private Integer index;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Cell(Category category, Integer index, String content) {
        this.category = category;
        this.index = index;
        this.content = content;
        if (category != null) {
            category.getCells().add(this);
        }
    }

    // 비즈니스 로직: 완료 상태 변경
    public void updateContent(String content) {
        this.content = content;
    }

    public void toggleCompletion() {
        this.isCompleted = !this.isCompleted;
    }
}