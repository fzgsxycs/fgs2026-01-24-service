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
                // 1. 关闭 CSRF（新版本 Lambda 写法，消除废弃警告）
                .csrf(csrf -> csrf.disable())
                // 2. 启用跨域（与你的 CorsConfig 配合）
                .cors(cors -> cors.disable()) // 若已通过 CorsConfig 配置跨域，此处可 disable 或保留
                // 3. 配置接口权限
                .authorizeHttpRequests(auth -> auth
                        // 放行 Redis 所有接口，解决 401 问题
                        .requestMatchers("/redis/**").permitAll()
                        // 其他接口需认证（正式环境可保留）
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
