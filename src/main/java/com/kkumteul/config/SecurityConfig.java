package com.kkumteul.config;

import com.kkumteul.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 설정을 활성화!
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 테스트를 위해 CSRF 보안 잠시 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/landing","/css/**", "/images/**", "/js/**").permitAll() // 누구나 접근 가능
                        .anyRequest().authenticated() // 그 외 모든 페이지는 로그인 필수!
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // [핵심!] 우리가 만든 서비스 연결
                        )
                        .defaultSuccessUrl("/", true) // 로그인 성공하면 메인으로 이동
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // 로그아웃 성공 시 이동할 페이지
                );

        return http.build();
    }
}