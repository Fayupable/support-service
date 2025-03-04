package com.fayupable.test.mapper;

import com.fayupable.test.dto.TicketDetailsDto;
import com.fayupable.test.entity.TicketDetails;
import org.springframework.stereotype.Service;

@Service
public class TicketDetailsMapper {

    public TicketDetailsDto fromTicketDetails(TicketDetails ticketDetails) {
        if (ticketDetails == null) {
            return null;
        }
        TicketDetailsDto dto = new TicketDetailsDto();
        dto.setDescription(ticketDetails.getDescription());
        dto.setCreatedAt(ticketDetails.getCreatedAt());

        return dto;
    }
}
