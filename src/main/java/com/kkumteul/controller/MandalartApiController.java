package com.kkumteul.controller;

import com.kkumteul.domain.Category;
import com.kkumteul.domain.Cell;
import com.kkumteul.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mandalart")
@RequiredArgsConstructor
public class MandalartApiController {

    private final CategoryRepository categoryRepository;

    /**
     * 특정 카테고리의 상세 데이터(이름, 8개 세부 목표, 달성률)를 반환
     */
    @GetMapping("/category/{id}")
    public Map<String, Object> getCategoryDetail(@PathVariable("id") Long id) {
        Category category = categoryRepository.findById(id).orElseThrow();
        Map<String, Object> response = new HashMap<>();

        String name = (category.getCategoryName() != null) ? category.getCategoryName() : "목표";
        response.put("title", name);
        response.put("imgKey", getImgKey(name));

        // 1. 8개 세부 목표 텍스트 리스트
        List<String> items = category.getCells().stream()
                .sorted(Comparator.comparing(Cell::getIndex))
                .filter(cell -> cell.getIndex() % 9 != 4)
                .map(cell -> (cell.getContent() == null || cell.getContent().isEmpty()) ? "○" : cell.getContent())
                .collect(Collectors.toList());
        response.put("items", items);

        // 2. [추가] 8개 세부 목표 완료 여부 리스트 (새로고침 시 유지용)
        List<Boolean> completedList = category.getCells().stream()
                .sorted(Comparator.comparing(Cell::getIndex))
                .filter(cell -> cell.getIndex() % 9 != 4)
                .map(Cell::getIsCompleted)
                .collect(Collectors.toList());
        response.put("completedList", completedList);

        // 3. 달성 개수 계산
        long achievedCount = category.getCells().stream()
                .filter(cell -> cell.getIndex() % 9 != 4)
                .filter(Cell::getIsCompleted)
                .count();
        response.put("achieved", achievedCount);

        return response;
    }

    /**
     * 카테고리 위치에 따른 기본 이름 반환
     */
    private String getDefaultCategoryName(Category category) {
        // 만다라트 내에서 몇 번째 카테고리인지 확인
        List<Category> allCategories = category.getMandalart().getCategories();
        int catIdx = allCategories.indexOf(category);

        String[] defaultNames = {"독서", "운동", "공부", "경제", "여행", "취미", "가족", "자기관리"};
        return (catIdx >= 0 && catIdx < defaultNames.length) ? defaultNames[catIdx] : "목표";
    }

    /**
     * 카테고리명별 이미지 폴더 키값 매핑
     */
    private String getImgKey(String name) {
        if (name.contains("독서")) return "book";
        if (name.contains("운동")) return "gym";
        if (name.contains("공부")) return "study";
        if (name.contains("경제")) return "economy";
        if (name.contains("여행")) return "travel";
        if (name.contains("취미")) return "hobby";
        if (name.contains("가족")) return "family";
        if (name.contains("자기관리")) return "selfcare";
        return "default";
    }

    @PostMapping("/category/{categoryId}/cell/{cellIndex}/toggle")
    public Map<String, Object> toggleCellCompletion(
            @PathVariable("categoryId") Long categoryId,
            @PathVariable("cellIndex") Integer cellIndex) {

        Category category = categoryRepository.findById(categoryId).orElseThrow();

        // HTML의 0~7 인덱스를 DB의 0~8 인덱스(중앙 4번 제외)로 매핑
        int dbIndex = (cellIndex < 4) ? cellIndex : cellIndex + 1;

        // 해당 인덱스의 셀 찾기
        Cell targetCell = category.getCells().stream()
                .filter(c -> c.getIndex().equals(dbIndex))
                .findFirst()
                .orElseThrow();

        // 상태 반전 (T -> F, F -> T)
        targetCell.toggleCompletion();
        categoryRepository.save(category); // 변경 사항 저장

        // 갱신된 성취도 계산
        long achievedCount = category.getCells().stream()
                .filter(c -> c.getIndex() % 9 != 4)
                .filter(Cell::getIsCompleted)
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isCompleted", targetCell.getIsCompleted());
        response.put("achieved", achievedCount);
        return response;
    }
}