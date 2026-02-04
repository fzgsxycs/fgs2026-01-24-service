package com.example.computerassociation.controller;

import com.example.computerassociation.dto.UserDTO;
import com.example.computerassociation.entity.User;
import com.example.computerassociation.service.UserService;
import com.example.computerassociation.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api/users") // 统一的API前缀
@CrossOrigin(origins = "*") // 允许跨域请求
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ... existing code ...
}
