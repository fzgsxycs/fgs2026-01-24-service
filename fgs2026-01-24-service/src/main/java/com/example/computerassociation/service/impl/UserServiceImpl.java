package com.example.computerassociation.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.computerassociation.entity.User;
import com.example.computerassociation.mapper.UserMapper;
import com.example.computerassociation.dto.UserDTO;
import com.example.computerassociation.service.UserService;
import com.example.computerassociation.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * 实现用户注册、登录、密码重置等业务逻辑
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // 密码加密器

    @Autowired
    private RedisUtil redisUtil; // Redis工具类

    @Autowired
    private JavaMailSender mailSender; // 邮件发送器

    @Autowired
    private TemplateEngine templateEngine; // 模板引擎

    // 验证码过期时间（分钟）
    private static final int VERIFICATION_CODE_EXPIRE_TIME = 5;

    /**
     * 用户注册实现
     * @param userDTO 用户注册信息
     * @return 注册结果
     */
    @Override
    public boolean register(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        // 对密码进行加密处理
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setStatus(1); // 默认启用状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        return save(user);
    }

    /**
     * 用户登录实现
     * @param username 用户名或邮箱
     * @param password 密码
     * @return 用户对象，如果登录失败则返回null
     */
    @Override
    public User login(String username, String password) {
        // 构建查询条件：根据用户名或邮箱查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).or().eq("email", username);

        User user = userMapper.selectOne(queryWrapper);

        // 如果用户不存在或密码验证失败，返回null
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return user;
    }

    /**
     * 发送重置密码邮件
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    @Override
    public boolean sendResetPasswordEmail(String email) {
        // 检查邮箱是否存在于系统中
        if (!existsByEmail(email)) {
            throw new RuntimeException("邮箱不存在");
        }

        // 生成6位数字验证码
        String verificationCode = RandomUtil.randomNumbers(6);

        // 将验证码存储到Redis中，设置过期时间为5分钟
        redisUtil.setString("reset_password_code:" + email, verificationCode,
                VERIFICATION_CODE_EXPIRE_TIME, TimeUnit.MINUTES);

        // 准备邮件内容
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("重置密码验证码");

            // 邮件正文
            String content = "您的重置密码验证码是：" + verificationCode + "，有效期为" +
                    VERIFICATION_CODE_EXPIRE_TIME + "分钟，请及时使用。";
            helper.setText(content, true);

            // 发送邮件
            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            // 发送邮件失败时记录错误日志
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 重置密码实现
     * @param email 邮箱
     * @param newPassword 新密码
     * @param verificationCode 验证码
     * @return 是否重置成功
     */
    @Override
    public boolean resetPassword(String email, String newPassword, String verificationCode) {
        // 验证邮箱是否存在
        if (!existsByEmail(email)) {
            throw new RuntimeException("邮箱不存在");
        }

        // 从Redis中获取存储的验证码
        String storedCode = redisUtil.getString("reset_password_code:" + email);

        // 验证验证码是否正确
        if (storedCode == null || !storedCode.equals(verificationCode)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 更新用户密码
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);

        User user = new User();
        user.setPassword(passwordEncoder.encode(newPassword)); // 对新密码进行加密
        user.setUpdateTime(LocalDateTime.now()); // 更新更新时间

        int result = userMapper.update(user, queryWrapper);

        // 验证码使用后立即删除
        redisUtil.deleteKey("reset_password_code:" + email);

        return result > 0;
    }

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    @Override
    public boolean existsByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userMapper.selectCount(queryWrapper) > 0;
    }
}
