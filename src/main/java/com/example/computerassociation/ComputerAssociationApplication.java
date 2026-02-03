package com.example.computerassociation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ComputerAssociationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComputerAssociationApplication.class, args);
    }

    /**
     * 配置密码编码器
     * 用于对用户密码进行加密存储
     * @return 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 使用BCrypt算法进行密码加密
    }
}
