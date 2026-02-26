package com.kkumteul.repository;

import com.kkumteul.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 특정 만다르트에 속한 카테고리만 찾아올 때 사용할 수 있어
    List<Category> findByMandalartId(Long mandalartId);
}