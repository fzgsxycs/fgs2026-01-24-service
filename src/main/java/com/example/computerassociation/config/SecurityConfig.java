package com.example.computerassociation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用CSRF保护
                .cors(cors -> cors.disable()) // 禁用CORS（与CorsConfig配合使用）
                .authorizeHttpRequests(authorize -> authorize
                        // 允许所有路径的POST请求匿名访问（用户注册和登录相关）
                        .requestMatchers("/api/users/register", "/api/users/login",
                                "/api/users/send-reset-code", "/api/users/reset-password").permitAll()
                        // 允许Swagger UI相关路径
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 允许所有OPTIONS请求
                        .requestMatchers("/", "/**").permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
