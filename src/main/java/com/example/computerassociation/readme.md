# 邮件模块技术文档

## 1. 模块概述

邮件模块是计算机协会系统中的一个核心组件，主要负责发送验证码邮件，用于用户注册、密码重置等场景的身份验证。该模块基于 Spring Boot Mail 技术实现，支持 HTML 格式邮件发送，并使用 Thymeleaf 模板引擎渲染邮件内容。

## 2. 核心实现

### 2.1 邮件工具类

邮件模块的核心实现位于 `MailUtil.java` 文件中，主要提供以下功能：

- **模板渲染**：使用 Thymeleaf 模板引擎渲染邮件内容
- **验证码邮件发送**：发送包含验证码的 HTML 格式邮件

```java
public static void sendVerificationEmail(String to, String subject, String verificationCode) throws MessagingException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

    helper.setTo(to);
    helper.setSubject(subject);
    helper.setFrom(mailFrom);

    Context context = new Context();
    context.setVariable("verificationCode", verificationCode);
    context.setVariable("expireTime", 5);
    String emailContent = renderTemplate("verification-code-email.html", context);

    helper.setText(emailContent, true);
    mailSender.send(mimeMessage);
}
```

### 2.2 邮件模板

邮件模板使用 Thymeleaf 语法编写，位于 `templates/verification-code-email.html` 文件中，主要特点：

- **响应式设计**：适配不同设备显示
- **美观布局**：深色主题，简洁明了
- **变量替换**：支持动态插入验证码和过期时间

### 2.3 配置信息

邮件服务的配置位于 `application.yml` 文件中，支持通过环境变量进行配置：

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|----------|--------|------|
| host | MAIL_HOST | smtp.163.com | SMTP 服务器地址 |
| port | MAIL_PORT | 465 | SMTP 服务器端口 |
| username | MAIL_USERNAME | - | 邮箱用户名 |
| password | MAIL_PASSWORD | - | 邮箱密码/授权码 |
| from | MAIL_FROM | ${MAIL_USERNAME} | 发件人邮箱 |

## 3. 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.x | 基础框架 |
| Spring Mail | - | 邮件发送功能 |
| Thymeleaf | - | 邮件模板渲染 |
| JavaMail | - | 邮件协议实现 |

## 4. 使用流程

### 4.1 依赖注入

邮件工具类通过 Spring 依赖注入获取必要的组件：

- `JavaMailSender`：邮件发送核心组件
- `TemplateEngine`：模板引擎，用于渲染邮件内容
- `mailFrom`：发件人邮箱地址

### 4.2 发送验证码邮件

1. 生成验证码
2. 调用 `MailUtil.sendVerificationEmail()` 方法发送邮件
3. 邮件发送成功后，将验证码存储到 Redis 中，设置过期时间
4. 用户输入验证码后，进行验证

## 5. 安全考虑

1. **验证码安全**：验证码通过邮件发送，避免了短信验证码的成本和限制
2. **有效期控制**：验证码设置了 5 分钟的有效期，提高安全性
3. **模板安全**：邮件模板使用 Thymeleaf 语法，避免了 XSS 攻击风险
4. **配置安全**：邮箱密码通过环境变量配置，避免硬编码到代码中

## 6. 部署说明

### 6.1 环境变量配置

在部署时，需要设置以下环境变量：

- `MAIL_HOST`：SMTP 服务器地址
- `MAIL_PORT`：SMTP 服务器端口
- `MAIL_USERNAME`：邮箱用户名
- `MAIL_PASSWORD`：邮箱密码/授权码
- `MAIL_FROM`：发件人邮箱（可选，默认使用 MAIL_USERNAME）

### 6.2 邮箱设置

使用 163 邮箱时，需要：
1. 开启 SMTP 服务
2. 获取授权码（不是登录密码）
3. 配置邮箱客户端授权

## 7. 依赖关系

| 模块 | 依赖关系 | 说明 |
|------|----------|------|
| UserService | 依赖 | 调用 MailUtil 发送验证码邮件 |
| RedisUtil | 依赖 | 存储验证码，设置过期时间 |


## 8. 接口文档

### 8.1 用户管理接口

#### 8.1.1 用户注册
- **接口路径**：`POST /api/user/register`
- **功能描述**：使用用户名、邮箱和验证码注册新用户
- **请求参数**：
  ```json
  {
    "username": "用户名",
    "password": "密码",
    "email": "邮箱",
    "verificationCode": "验证码",
    "captchaKey": "验证码key",
    "captchaValue": "图形验证码"
  }
  ```
- **响应格式**：
  ```json
  {
    "code": 200,
    "msg": "注册成功",
    "data": "注册成功"
  }
  ```
- **状态码**：200（注册成功），400（参数校验失败或验证码错误）


#### 8.1.2 发送注册验证码
- **接口路径**：`POST /api/user/send-code`
- **功能描述**：向邮箱发送注册验证码
- **请求参数**：
  ```json
  {
    "email": "邮箱"
  }
  ```
- **响应格式**：
  ```json
  {
    "code": 200,
    "msg": "验证码已发送至您的邮箱",
    "data": "验证码已发送至您的邮箱"
  }
  ```
- **状态码**：200（发送成功），400（邮箱已被注册）

#### 8.1.3 发送重置密码验证码
- **接口路径**：`POST /api/user/send-reset-code`
- **功能描述**：向邮箱发送重置密码验证码
- **请求参数**：
  ```json
  {
    "email": "邮箱"
  }
  ```
- **响应格式**：
  ```json
  {
    "code": 200,
    "msg": "验证码已发送至您的邮箱",
    "data": "验证码已发送至您的邮箱"
  }
  ```
- **状态码**：200（发送成功），400（邮箱不存在）


### 8.2 接口设计说明

#### 8.2.1 认证机制
- 使用JWT（JSON Web Token）进行身份认证
- 登录成功后返回JWT令牌
- 需要认证的接口通过请求头中的Authorization字段传递令牌

#### 8.2.2 验证码机制
- 图形验证码：用于防止恶意注册和登录
- 邮箱验证码：用于注册和密码重置时的身份验证
- 验证码存储在Redis中，设置5分钟过期时间

#### 8.2.3 错误处理
- 统一使用Result对象返回响应
- 包含code（状态码）、msg（消息）和data（数据）三个字段
- 错误信息清晰明确，便于前端处理

#### 8.2.4 安全措施
- 密码加密存储
- JWT令牌验证
- 验证码防刷
- 邮箱唯一性校验

### 8.3 依赖关系

| 接口 | 依赖服务 | 说明 |
|------|----------|------|
| 所有接口 | UserService | 业务逻辑处理 |
| 登录、获取用户信息 | JwtUtil | JWT令牌生成和验证 |
| 获取验证码、发送验证码 | RedisUtil | 存储验证码 |
| 发送验证码 | MailUtil | 发送邮件 |

### 8.4 使用示例

#### 8.4.1 注册示例
```bash
POST /api/user/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "123456",
  "email": "newuser@example.com",
  "verificationCode": "123456",
  "captchaKey": "UUID",
  "captchaValue": "ABCD"
}
```

### 8.5 注意事项

1. **验证码有效期**：所有验证码有效期为5分钟
2. **密码要求**：密码长度至少6位
3. **邮箱格式**：必须符合标准邮箱格式
4. **令牌有效期**：JWT令牌有效期为24小时
5. **请求频率限制**：发送验证码接口有频率限制，防止滥用