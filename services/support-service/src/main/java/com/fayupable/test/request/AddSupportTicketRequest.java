package com.fayupable.test.request;

import com.fayupable.test.dto.TicketDetailsDto;
import com.fayupable.test.dto.TicketResolutionDto;
import com.fayupable.test.enums.SupportPriority;
import com.fayupable.test.enums.SupportStatus;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class AddSupportTicketRequest {
    private UUID supportTicketId;
    private UUID userId;
    private UUID attachmentId;
    private SupportStatus status;
    private SupportPriority priority;
    private List<AddTicketDetailsRequest> ticketDetails;
    private List<AddTicketResolutionRequest> resolutions;
}
