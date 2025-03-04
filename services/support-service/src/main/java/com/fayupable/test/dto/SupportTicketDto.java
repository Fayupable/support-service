package com.fayupable.test.dto;

import com.fayupable.test.enums.SupportPriority;
import com.fayupable.test.enums.SupportStatus;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class SupportTicketDto {
    private UUID supportTicketId;
    private UUID userId;
    private UUID assignedWorkerId;
    private SupportStatus status;
    private SupportPriority priority;
    private Set<TicketDetailsDto> ticketDetails;
    private Set<TicketResolutionDto> resolutions;

}
