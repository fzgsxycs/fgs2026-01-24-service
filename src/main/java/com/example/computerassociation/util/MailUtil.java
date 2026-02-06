package com.example.computerassociation.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

 /**
 * 邮件发送工具类
 * <p>
 * 提供邮件发送和Thymeleaf模板渲染的功能。
 * 这个类使用了静态注入的方式来获取 {@link TemplateEngine} 和 {@link JavaMailSender} 的实例，
 * 以便在静态方法中调用它们。
 * </p>
 */
@Component
public class MailUtil {

    private static TemplateEngine templateEngine;
    private static JavaMailSender mailSender;

    /**
     * 静态注入TemplateEngine
     * @param templateEngine Thymeleaf模板引擎
     */
    @Autowired
    public void setTemplateEngine(TemplateEngine templateEngine) {
        MailUtil.templateEngine = templateEngine;
    }

    /**
     * 静态注入JavaMailSender
     * @param mailSender Spring的邮件发送器
     */
    @Autowired
    public void setMailSender(JavaMailSender mailSender) {
        MailUtil.mailSender = mailSender;
    }

    /**
     * 渲染Thymeleaf邮件模板
     * @param templateName 模板文件的名称 (例如 "verificationCode.html")
     * @param context 包含模板所需变量的上下文对象
     * @return 渲染后的HTML字符串
     */
    public static String renderTemplate(String templateName, Context context) {
        return templateEngine.process(templateName, context);
    }

    /**
     * 发送包含验证码的邮件
     * <p>
     * 该方法会使用 "verificationCode.html" 模板来生成邮件内容。
     * </p>
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param verificationCode 验证码字符串
     * @throws MessagingException 如果邮件发送失败
     */
    public static void sendVerificationEmail(String to, String subject, String verificationCode) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        // 显式设置发件人地址，确保与SMTP认证用户一致
        helper.setFrom("18065143863@163.com");

        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        context.setVariable("expireTime", 5); // 设置默认过期时间为5分钟
        String emailContent = renderTemplate("verification-code-email.html", context);

        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }
}