package com.fayupable.test.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserInfoRequest {
    @Size(min = 4, max = 30, message = "Username must be between 4 and 30 characters")
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String password;

    @Size(max = 30, message = "First name must be less than 30 characters")
    private String firstName;

    @Size(max = 30, message = "Last name must be less than 30 characters")
    private String lastName;

    private List<UpdateUserProfileRequest> profiles;

    private List<UpdateUserContactRequest> contacts;
}