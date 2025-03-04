package com.fayupable.test.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateTicketDetailsRequest {
    private UUID ticketDetailsId;
    private String description;
}