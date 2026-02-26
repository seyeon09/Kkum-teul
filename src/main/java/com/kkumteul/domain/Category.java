package com.kkumteul.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "categorys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mandalart_id", nullable = false)
    private  Mandalart mandalart;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cell> cells = new ArrayList<>();

    public Category(Mandalart mandalart, Integer position, String categoryName) {
        this.mandalart = mandalart;
        this.position = position;
        this.categoryName = categoryName;
        if (mandalart != null) {
            mandalart.getCategories().add(this);
        }
    }

}