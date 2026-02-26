package com.kkumteul.controller;
import com.kkumteul.domain.Category;
import com.kkumteul.domain.Mandalart;
import com.kkumteul.domain.User;
import com.kkumteul.repository.UserRepository;
import com.kkumteul.service.MandalartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // [필수 추가]
import java.time.LocalDate;
import java.util.List;
import com.kkumteul.domain.Cell;
import java.util.Comparator;

import java.util.Map; // [필수 추가]


@Controller
@RequiredArgsConstructor
public class MandalartController {

    private final MandalartService mandalartService;
    private final UserRepository userRepository;

    /**
     * 1. 만다라트 입력 폼 화면 띄우기
     */
    @GetMapping("/mandalart/new")
    public String createForm(Model model, @AuthenticationPrincipal OAuth2User principal) {
        // 로그인한 사용자의 정보를 principal 이라는 이름으로 받음
        // principal이 널값이 아니라면
        if (principal != null) {
            String name = principal.getAttribute("name");
            // name 변수에 사용자 정보중 name에 해당하는 값을 가져옴
            model.addAttribute("userName", name);
            // html에서 userName이라는 값으로 쓰게 모델에 담아줌
        }
        // createForm html 화면에 띄워줌
        return "createForm";
    }

    /**
     * 2. 만다라트 저장 (81개 파라미터 수집 방식)
     * 우리가 만든 createMandalartFromMap을 사용하는 핵심 로직이야!
     */
    @PostMapping("/mandalart/save")
    public String save(@RequestParam Map<String, String> allParams,
                       @AuthenticationPrincipal OAuth2User oAuth2User,
                       RedirectAttributes redirectAttributes) {

        allParams.forEach((key, value) -> System.out.println(key + " : " + value));

        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보를 찾을 수 없습니다."));
        Long userId = user.getId();

        Long savedId = mandalartService.createMandalartFromMap(allParams, userId);
        // 81개 칸의 데이터를 key-value로 묶어서 Map에 몽땅 저장
        // Map에 담긴 데이터를 읽어서 Mysql에 만다르트->카테고리->셀에 맞춰 저장시킴
        // redirect:/.../{id} 구문의 {id} 자리에 savedId가 쏙 들어감
        redirectAttributes.addAttribute("id", savedId);

        return "redirect:/mandalart/main/{id}";
    }

    /**
     * 3. 메인 결과 화면 (main.html) 보여주기
     */
    @GetMapping("/mandalart/main/{id}") // user id 번호 인식하여 입장
    public String main(@PathVariable("id") Long id, @AuthenticationPrincipal OAuth2User principal, Model model) { // {id}를 자바 변수 'id'로 변환

        // 세션에서 구글 사용자 이름 꺼내기
        if (principal != null) {
            String name = principal.getAttribute("name");
            model.addAttribute("userName", name);
        }

        Mandalart mandalart = mandalartService.findMandalart(id);
        // 서비스에게 DB에서 해당 ID의 만다라트 덩어리를 통째로 가져오라는 메소드 실행

        // 1. D-Day 계산 (2026년 12월 31일 기준)
        LocalDate today = LocalDate.now(); // 오늘 날짜 확인
        LocalDate endOfYear = LocalDate.of(2026, 12, 31);
        //목표 마감일 설정
        long dDay = java.time.temporal.ChronoUnit.DAYS.between(today, endOfYear);
        // 두 날짜 사이의 일수 차이 계산

        // 2. 전체 달성률 계산
        // 모든 카테고리의 모든 세부 목표를 합쳐서 계산해!
        List<Cell> allcells = mandalart.getCategories().stream()
                // stream으로 카테고리들을 한줄로 나열
                .flatMap(category -> category.getCells().stream())
                //세부목표들도 다 꺼냄
                .toList(); // 전체를 리스트로 묶음

        long totalCount = allcells.size(); // 전체 목표 수 계산
        long completedCount = allcells.stream().filter(Cell::getIsCompleted).count();
        // 완료 상태만 골라서 세기
        int progressPercent = totalCount == 0 ? 0 : (int) ((double) completedCount / totalCount * 100);
        // 0으로 나누는 에러를 방지하면서 퍼센트 계산

        // 3. 최근 활동 (완료된 목표 중 최신 3개만 추출)
        List<Cell> recentActivities = allcells.stream()
                .filter(Cell::getIsCompleted) // 완료된 것만 필터
                .sorted(Comparator.comparing(Cell::getCreatedAt).reversed())
                // 생성일 기준으로 내림차순 정렬
                .limit(3) // 3개 뽑기
                .toList();

        // 모델에 담아서 메인으로 이동
        model.addAttribute("mandalart", mandalart);
        model.addAttribute("dDay", dDay);
        model.addAttribute("progress", progressPercent);
        model.addAttribute("recentActivities", recentActivities);

        return "main";
    }

    @GetMapping("/mandalart/category/{id}")
    public String categoryDetailPage(@PathVariable("id") Long id, Model model) {
        // JS에서 쓸 수 있게 ID만 넘겨주면 돼!
        model.addAttribute("categoryId", id);

        // 만약 상단 헤더에 이름을 띄우고 싶다면 여기서 유저 정보를 추가해도 좋아.
        // model.addAttribute("userName", ...);

        return "Detail"; // templates/categoryDetail.html을 찾아가게 함
    }

    // 만다라트 수정
    // MandalartController.java

    @GetMapping("/mandalart/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            model.addAttribute("userName", principal.getAttribute("name"));
        }

        // Cell 데이터를 Map으로 가져오기
        Map<String, String> cellData = mandalartService.getCellMap(id);

        model.addAttribute("goals", cellData); // HTML에서 쓰던 이름 'goals'는 유지해도 돼!
        model.addAttribute("mandalartId", id);
        model.addAttribute("isEdit", true);

        return "createForm";
    }



}