package com.kkumteul.service;

import com.kkumteul.domain.User;
import com.kkumteul.domain.Category;
import com.kkumteul.domain.Mandalart;
import com.kkumteul.domain.Cell; // 클래스명은 대문자로 시작!
import com.kkumteul.repository.UserRepository;
import com.kkumteul.repository.CategoryRepository;
import com.kkumteul.repository.MandalartRepository;
import com.kkumteul.repository.CellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MandalartService {

    private final MandalartRepository mandalartRepository;
    private final CategoryRepository categoryRepository;
    private final CellRepository cellRepository;
    private final UserRepository userRepository;

    /**
     * 1. 81개 평면 데이터를 계층 구조로 변환하여 전체 저장
     */
    @Transactional
    public Long createMandalartFromMap(Map<String, String> goalParams, Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        // [핵심!] 이미 만다라트가 있다면 새로 만들지 않고 기존 ID를 그냥 돌려줘.
        if (!user.getMandalarts().isEmpty()) {
            return user.getMandalarts().get(0).getId();
        }

        // 중앙 핵심 목표 (인덱스 40)
        String mainGoalTitle = goalParams.get("goal[40]");
        Mandalart mandalart = new Mandalart(user, mainGoalTitle);

        // 9개의 블록 순회
        for (int i = 0; i < 9; i++) {
            if (i == 4) continue;

            int categoryIndex = calculateFlatIndex(i, 4);
            String categoryName = goalParams.get("goal[" + categoryIndex + "]");

            // Category 생성자: (mandalart, position, categoryName) 순서 준수!
            Category category = new Category(mandalart, i, categoryName);

            // 해당 블록 내 나머지 8개 칸 순회
            for (int j = 0; j < 9; j++) {
                if (j == 4) continue;

                int flatIndex = calculateFlatIndex(i, j);
                String cellContent = goalParams.get("goal[" + flatIndex + "]");

                if (cellContent != null && !cellContent.isEmpty()) {
                    // Cell 생성자: (category, index, content) 순서 준수!
                    new Cell(category, j, cellContent);
                }
            }
        }
        // CascadeType.ALL로 인해 하위 엔티티까지 한 번에 저장됨
        return mandalartRepository.save(mandalart).getId();
    }

    /**
     * 2. 세부 목표 달성 상태 토글
     */
    @Transactional
    public void toggleCell(Long cellId) {
        Cell cell = cellRepository.findById(cellId)
                .orElseThrow(() -> new IllegalArgumentException("해당 셀이 없습니다. id=" + cellId));

        // 도메인 내 정의된 메서드 호출 (toggleCompletion)
        cell.toggleCompletion();
    }

    /**
     * 3. 특정 카테고리 내 완료된 목표 개수 조회
     */
    public int getCompletedCount(Long categoryId) {
        List<Cell> cells = cellRepository.findByCategoryId(categoryId);
        return (int) cells.stream()
                .filter(Cell::getIsCompleted) // Getter 활용
                .count();
    }

    /**
     * 4. 만다라트 단건 조회
     */
    public Mandalart findMandalart(Long mandalartId) {
        return mandalartRepository.findById(mandalartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 만다르트가 없습니다."));
    }

    /**
     * [Helper] 2차원 좌표 -> 1차원 인덱스 변환 로직
     */
    private int calculateFlatIndex(int blockIdx, int cellIdx) {
        return (blockIdx / 3) * 27 + (blockIdx % 3) * 3 + (cellIdx / 3) * 9 + (cellIdx % 3);
    }

    public Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 없습니다. id=" + id));
    }

    // 만다라트 수정을 위해 맵에 데이터 다시 담기
    @Transactional(readOnly = true)
    public Map<String, String> getCellMap(Long mandalartId) {
        Mandalart mandalart = findMandalart(mandalartId);
        Map<String, String> cellMap = new HashMap<>();

        int globalIdx = 0;
        // 9개의 카테고리와 각 9개의 셀을 순회해
        for (Category category : mandalart.getCategories()) {
            for (Cell cell : category.getCells()) {
                // [핵심!] cell.getContent()를 호출해서 Map에 담아줘
                cellMap.put("goal[" + globalIdx + "]", cell.getContent());
                globalIdx++;
            }
        }
        return cellMap;
    }

}