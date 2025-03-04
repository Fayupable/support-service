package com.fayupable.test.request.user;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateUserContactRequest {
    private UUID contactId;
    private String phoneNumber;
}