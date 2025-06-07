package com.foodorder.backend.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String address;
}
