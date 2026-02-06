package com.example.computerassociation.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String email;
    private String password;
    private String captchaKey;
    private String captchaCode;
}