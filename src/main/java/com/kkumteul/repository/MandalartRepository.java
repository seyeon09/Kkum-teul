package com.kkumteul.repository;

import com.kkumteul.domain.Mandalart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MandalartRepository extends JpaRepository<Mandalart, Long> {
    // 기본 CRUD 기능(save, findById 등)이 자동으로 포함되어 있어!
}