package com.fayupable.test.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupportTicketResponse {
    private String message;
    private Object data;
}
