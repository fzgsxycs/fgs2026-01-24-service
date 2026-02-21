package com.example.computerassociation.controller;

import com.example.computerassociation.common.Result;
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
@RequestMapping("/api/user") // 统一的API前缀
@CrossOrigin(origins = "*") // 允许跨域请求
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册接口
     * @param userDTO 用户注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO) {
        try {
            boolean result = userService.register(userDTO);
            if (result) {
                return Result.success("注册成功");
            } else {
                return Result.fail("注册失败");
            }
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 用户登录接口
     * @param userDTO 登录信息
     * @return 登录结果和JWT令牌
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserDTO userDTO) {
        try {
            User user = userService.login(userDTO.getUsername(), userDTO.getPassword());
            if (user != null) {
                // 生成JWT令牌
                String token = jwtUtil.generateToken(user.getUsername());
                
                // 构造返回数据
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userId", user.getId());
                data.put("username", user.getUsername());
                data.put("email", user.getEmail());
                
                return Result.success(data, "登录成功");
            } else {
                return Result.fail("用户名或密码错误");
            }
        } catch (Exception e) {
            return Result.fail("登录异常: " + e.getMessage());
        }
    }

    /**
     * 发送重置密码验证码
     * @param email 邮箱地址
     * @return 发送结果
     */
    @PostMapping("/send-reset-code")
    public Result<String> sendResetCode(@RequestParam String email) {
        try {
            boolean result = userService.sendResetPasswordEmail(email);
            if (result) {
                return Result.success("验证码已发送到您的邮箱");
            } else {
                return Result.fail("验证码发送失败");
            }
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 重置密码
     * @param resetRequest 重置密码请求参数
     * @return 重置结果
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> resetRequest) {
        try {
            String email = resetRequest.get("email");
            String newPassword = resetRequest.get("newPassword");
            String verificationCode = resetRequest.get("verificationCode");
            
            boolean result = userService.resetPassword(email, newPassword, verificationCode);
            if (result) {
                return Result.success("密码重置成功");
            } else {
                return Result.fail("密码重置失败");
            }
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}

