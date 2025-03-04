package com.fayupable.test.mapper;

import com.fayupable.test.dto.TicketResolutionDto;
import com.fayupable.test.entity.TicketResolution;
import org.springframework.stereotype.Service;

@Service
public class TicketResolutionMapper {

    public TicketResolutionDto fromTicketResolution(TicketResolution ticketResolution) {
        if (ticketResolution == null) {
            return null;
        }
        TicketResolutionDto dto = new TicketResolutionDto();
        dto.setResolutionMessage(ticketResolution.getResolutionMessage());
        dto.setClosedAt(ticketResolution.getClosedAt());
        return dto;
    }
}
