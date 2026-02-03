package com.example.computerassociation.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.example.computerassociation.common.Result;
import com.example.computerassociation.dto.UserDTO;
import com.example.computerassociation.entity.User;
import com.example.computerassociation.service.UserService;
import com.example.computerassociation.util.JwtUtil;
import com.example.computerassociation.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户控制器
 * 提供用户注册、登录、密码重置等REST API接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册接口
     * @param userDTO 用户注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO) {
        try {
            // 参数验证
            if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
                return Result.fail("用户名不能为空");
            }
            if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
                return Result.fail("邮箱不能为空");
            }
            if (userDTO.getPassword() == null || userDTO.getPassword().length() < 6) {
                return Result.fail("密码长度不能少于6位");
            }

            // 执行注册
            boolean success = userService.register(userDTO);
            if (success) {
                return Result.success("注册成功");
            } else {
                return Result.fail("注册失败");
            }
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("系统错误");
        }
    }

    /**
     * 用户登录接口
     * @param loginInfo 登录信息（用户名/邮箱和密码）
     * @return 登录结果和JWT令牌
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");

        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            return Result.fail("用户名或邮箱不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.fail("密码不能为空");
        }

        // 执行登录
        User user = userService.login(username, password);
        if (user != null) {
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUsername());

            // 返回用户信息和令牌
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return Result.success(data, "登录成功");
        } else {
            return Result.fail("用户名或密码错误");
        }
    }

    /**
     * 发送重置密码验证码接口
     * @param email 邮箱地址
     * @return 发送结果
     */
    @PostMapping("/send-reset-code")
    public Result<String> sendResetCode(@RequestParam String email) {
        try {
            boolean success = userService.sendResetPasswordEmail(email);
            if (success) {
                return Result.success("验证码已发送至您的邮箱");
            } else {
                return Result.fail("验证码发送失败，请稍后再试");
            }
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("系统错误");
        }
    }

    /**
     * 重置密码接口
     * @param resetInfo 重置密码信息（邮箱、新密码、验证码）
     * @return 重置结果
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> resetInfo) {
        String email = resetInfo.get("email");
        String newPassword = resetInfo.get("newPassword");
        String verificationCode = resetInfo.get("verificationCode");

        // 参数验证
        if (email == null || email.trim().isEmpty()) {
            return Result.fail("邮箱不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail("新密码长度不能少于6位");
        }
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            return Result.fail("验证码不能为空");
        }

        try {
            boolean success = userService.resetPassword(email, newPassword, verificationCode);
            if (success) {
                return Result.success("密码重置成功");
            } else {
                return Result.fail("密码重置失败");
            }
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("系统错误");
        }
    }

    /**
     * 获取用户信息接口（需要JWT验证）
     * @param token JWT令牌
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 验证令牌并获取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null && jwtUtil.validateToken(token, username)) {
                // 根据用户名查询用户信息
                User user = userService.login(username, ""); // 只查询用户信息，不需要密码验证
                if (user != null) {
                    // 清理敏感信息
                    user.setPassword(null);
                    return Result.success(user);
                } else {
                    return Result.fail("用户不存在");
                }
            } else {
                return Result.fail(401, "令牌无效或已过期");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(401, "令牌验证失败");
        }
    }
}
