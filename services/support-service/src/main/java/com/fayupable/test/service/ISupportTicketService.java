package com.fayupable.test.service;

import com.fayupable.test.dto.SupportTicketDto;
import com.fayupable.test.dto.TicketResolutionDto;
import com.fayupable.test.request.AddSupportTicketRequest;
import com.fayupable.test.request.UpdateSupportTicketRequest;
import com.fayupable.test.request.UpdateTicketResolutionRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ISupportTicketService {
    List<SupportTicketDto> getAllSupportTickets();

    List<SupportTicketDto> getAllSupportTicketsWithRedis();

    SupportTicketDto addSupportTicket(AddSupportTicketRequest addSupportTicketRequest, MultipartFile attachment);

    SupportTicketDto updateSupportTicketForUser(UUID supportTicketId, UpdateSupportTicketRequest updateRequest);

    SupportTicketDto updateSupportTicketForStaff(UUID supportTicketId, UpdateSupportTicketRequest updateRequest);

    TicketResolutionDto updateTicketResolution(UUID supportTicketId, UpdateTicketResolutionRequest updateRequest);
}
