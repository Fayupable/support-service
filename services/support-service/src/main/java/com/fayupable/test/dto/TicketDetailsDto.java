package com.fayupable.test.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketDetailsDto {
    private String description;
    private LocalDateTime createdAt;
}
