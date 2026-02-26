package com.kkumteul.repository;

import com.kkumteul.domain.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CellRepository extends JpaRepository<Cell, Long> {
    // 특정 카테고리에 속한 세부 목표들만 찾아오는 기능이야
    List<Cell> findByCategoryId(Long categoryId);
}