package com.iot.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phone;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;

    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;


    private String password;
    private String emailVerificationToken;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpires;


    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private String profileImageUrl;
    private String bio;
    private String website;
    private String linkedinUrl;
    private String preferences;
}
