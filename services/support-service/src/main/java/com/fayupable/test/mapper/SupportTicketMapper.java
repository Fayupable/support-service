package com.fayupable.test.mapper;

import com.fayupable.test.dto.SupportTicketDto;
import com.fayupable.test.entity.SupportTicket;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class SupportTicketMapper {

    private final TicketDetailsMapper ticketDetailsMapper;
    private final TicketResolutionMapper ticketResolutionMapper;

    public SupportTicketMapper(TicketDetailsMapper ticketDetailsMapper, TicketResolutionMapper ticketResolutionMapper) {
        this.ticketDetailsMapper = ticketDetailsMapper;
        this.ticketResolutionMapper = ticketResolutionMapper;
    }

    public SupportTicketDto fromSupportTicket(SupportTicket supportTicket) {
        if (supportTicket == null) {
            return null;
        }
        SupportTicketDto dto = new SupportTicketDto();
        dto.setSupportTicketId(supportTicket.getSupportTicketId());
        dto.setUserId(supportTicket.getUserId());
        dto.setAssignedWorkerId(supportTicket.getAssignedWorkerId());
        dto.setStatus(supportTicket.getStatus());
        dto.setPriority(supportTicket.getPriority());
        dto.setTicketDetails(supportTicket.getTicketDetails() != null ?
                supportTicket.getTicketDetails().stream()
                        .map(ticketDetailsMapper::fromTicketDetails)
                        .collect(Collectors.toSet())
                : new HashSet<>());

        dto.setResolutions(supportTicket.getResolutions() != null ?
                supportTicket.getResolutions().stream()
                        .map(ticketResolutionMapper::fromTicketResolution)
                        .collect(Collectors.toSet())
                : new HashSet<>());

        return dto;
    }
}
