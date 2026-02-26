package com.kkumteul.service;

import com.kkumteul.domain.User;
import com.kkumteul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글에서 가져온 기본 사용자 정보 생성
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 구글 로그인 시 제공받는 모든 속성(이름, 이메일, 고유ID 등) 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 3. 필요한 정보만 쏙쏙 뽑아내기
        String googleId = (String) attributes.get("sub"); // 구글의 고유 식별자
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 4. DB에 저장하거나 이미 있다면 정보를 업데이트
        User user = saveOrUpdate(googleId, email, name);

        // 5. 시큐리티 세션에 저장할 유저 정보 반환
        return new DefaultOAuth2User(
                Collections.emptyList(), // 권한 목록 (필요시 추가)
                attributes,
                "name" // 구글 로그인 식별자 키값
        );
    }

    private User saveOrUpdate(String googleId, String email, String name) {
        // 이메일로 기존 유저인지 확인하고, 이름이 바뀌었다면 업데이트!
        User user = userRepository.findByEmail(email)
                .map(entity -> entity.updateName(name)) // 이름 변경 시 업데이트 로직 (User 엔티티에 구현 필요)
                .orElse(new User(name, email, googleId)); // 없다면 새로 생성

        return userRepository.save(user);
    }
}