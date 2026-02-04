package com.example.computerassociation.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于生成和验证JWT令牌
 * JWT包含三部分：头部、载荷、签名
 */
@Component
public class JwtUtil {

    // 从配置文件中读取密钥
    @Value("${jwt.secret:computer_association_secret_key}")
    private String secret;

    // 从配置文件中读取过期时间，默认为24小时
    @Value("${jwt.expiration:86400}")
    private Long expiration;

    /**
     * 根据用户名生成JWT令牌
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username); // 将用户名存储在JWT的载荷中
        claims.put("created", new Date()); // 记录创建时间

        return Jwts.builder()
                .setClaims(claims) // 设置载荷信息
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000)) // 设置过期时间
                .signWith(SignatureAlgorithm.HS512, secret) // 使用HS512算法和密钥进行签名
                .compact(); // 生成最终的JWT字符串
    }

    /**
     * 从JWT令牌中解析用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从JWT令牌中获取载荷信息
     * @param token JWT令牌
     * @return 载荷信息
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret) // 设置签名密钥
                    .parseClaimsJws(token) // 解析JWT
                    .getBody();
        } catch (Exception e) {
            // 如果解析失败，返回null
            claims = null;
        }
        return claims;
    }

    /**
     * 验证JWT令牌是否有效
     * @param token JWT令牌
     * @param username 用户名
     * @return 是否有效
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return tokenUsername.equals(username) && !isTokenExpired(token); // 检查用户名匹配且未过期
    }

    /**
     * 检查JWT令牌是否已过期
     * @param token JWT令牌
     * @return 是否已过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date()); // 如果过期时间早于当前时间，则已过期
    }

    /**
     * 从JWT令牌中获取过期时间
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
}
