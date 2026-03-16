package com.example.computerassociation.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.example.computerassociation.common.Result;
import com.example.computerassociation.dto.LoginDTO;
import com.example.computerassociation.dto.RegisterDTO;
import com.example.computerassociation.dto.ResetPasswordDTO;
import com.example.computerassociation.dto.SendCodeDTO;
import com.example.computerassociation.entity.User;
import com.example.computerassociation.service.UserService;
import com.example.computerassociation.util.JwtUtil;
import com.example.computerassociation.util.RedisUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 150);
        String captchaCode = lineCaptcha.getCode();
        String captchaImage = lineCaptcha.getImageBase64();
        String captchaKey = UUID.randomUUID().toString();

        redisUtil.set(captchaKey, captchaCode, 5, TimeUnit.MINUTES);

        Map<String, String> data = new HashMap<>();
        data.put("captchaKey", captchaKey);
        data.put("captchaImage", "data:image/png;base64," + captchaImage);

        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO) {
        boolean success = userService.register(registerDTO);
        return success ? Result.success("注册成功") : Result.fail("注册失败");
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        if (user != null) {
            String token = jwtUtil.generateToken(user.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success(data, "登录成功");
        }
        return Result.fail("用户名或密码错误");
    }

    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeDTO sendCodeDTO) {
        boolean success = userService.sendVerificationCode(sendCodeDTO.getEmail());
        return success ? Result.success("验证码已发送至您的邮箱") : Result.fail("验证码发送失败，请稍后再试");
    }

    @PostMapping("/send-reset-code")
    public Result<String> sendResetCode(@Valid @RequestBody SendCodeDTO sendCodeDTO) {
        boolean success = userService.sendResetPasswordEmail(sendCodeDTO.getEmail());
        return success ? Result.success("验证码已发送至您的邮箱") : Result.fail("验证码发送失败，请稍后再试");
    }

    @PutMapping("/reset-password")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        boolean success = userService.resetPassword(
                resetPasswordDTO.getEmail(),
                resetPasswordDTO.getNewPassword(),
                resetPasswordDTO.getVerificationCode()
        );
        return success ? Result.success("密码重置成功") : Result.fail("密码重置失败");
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username != null && jwtUtil.validateToken(token, username)) {
            User user = userService.getByUsername(username);
            if (user != null) {
                user.setPassword(null);
                return Result.success(user);
            }
            return Result.fail("用户不存在");
        }
        return Result.fail(401, "令牌无效或已过期");
    }
}
