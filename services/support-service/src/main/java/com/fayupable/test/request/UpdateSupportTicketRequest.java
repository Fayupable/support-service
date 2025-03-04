package com.fayupable.test.request;

import com.fayupable.test.enums.SupportPriority;
import com.fayupable.test.enums.SupportStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateSupportTicketRequest {
    private UUID userId;
    private UUID attachmentId;
    private SupportStatus status;
    private SupportPriority priority;
    private List<UpdateTicketDetailsRequest> ticketDetails;
    private List<UpdateTicketResolutionRequest> resolutions;
}