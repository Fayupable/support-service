package com.fayupable.test.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateTicketResolutionRequest {
    private UUID ticketResolutionId;
    private String resolutionMessage;
}