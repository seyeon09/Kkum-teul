package com.kkumteul.controller;

import com.kkumteul.domain.User;
import com.kkumteul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor // userRepository 연결을 위해 필요해!
public class HomeController {

    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal) {
        // 1. 로그인을 안 한 상태라면?
        if (principal == null) {
            return "landing"; // 우리가 만든 랜딩 페이지(index 아님)를 보여줘.
        }

        // 2. 로그인을 한 상태라면 DB에서 유저 정보를 가져와.
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음"));

        // 3. 만다라트가 이미 있는지 확인해.
        if (user.getMandalarts().isEmpty()) {
            // 만다라트가 없으면 새로 만드는 화면으로 보내!
            return "redirect:/mandalart/new";
        } else {
            // 이미 있다면 저장된 첫 번째 만다라트의 상세 화면으로 보내!
            Long mandalartId = user.getMandalarts().get(0).getId();
            return "redirect:/mandalart/main/" + mandalartId;
        }
    }
}