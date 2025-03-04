package com.fayupable.test.controller;

import com.fayupable.test.request.AddSupportTicketRequest;
import com.fayupable.test.request.UpdateSupportTicketRequest;
import com.fayupable.test.request.UpdateTicketResolutionRequest;
import com.fayupable.test.response.SupportTicketResponse;
import com.fayupable.test.service.ISupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/support-tickets")
@RequiredArgsConstructor
public class SupportTicketController {
    private final ISupportTicketService supportTicketService;

    @GetMapping("/all")
    public ResponseEntity<SupportTicketResponse> getAllSupportTickets() {
        return ResponseEntity.ok(new SupportTicketResponse("All support tickets", supportTicketService.getAllSupportTickets()));
    }

    @GetMapping("/all/redis")
    public ResponseEntity<SupportTicketResponse> getAllSupportTicketsWithRedis() {
        return ResponseEntity.ok(new SupportTicketResponse("All support tickets with Redis", supportTicketService.getAllSupportTicketsWithRedis()));
    }

    @PostMapping(value = "/add", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<SupportTicketResponse> addSupportTicket(@RequestPart("addSupportTicketRequest") AddSupportTicketRequest addSupportTicketRequest, @RequestPart(value = "attachment", required = false) MultipartFile attachment) {
        return ResponseEntity.ok(new SupportTicketResponse("Support ticket added successfully", supportTicketService.addSupportTicket(addSupportTicketRequest, attachment)));
    }


    @PutMapping("/update/{supportTicketId}")
    public ResponseEntity<SupportTicketResponse> updateSupportTicket(@PathVariable UUID supportTicketId, @RequestBody UpdateSupportTicketRequest addSupportTicketRequest) {
        return ResponseEntity.ok(new SupportTicketResponse("Support ticket updated successfully", supportTicketService.updateSupportTicketForUser(supportTicketId, addSupportTicketRequest)));
    }

    @PutMapping("/update/staff/{supportTicketId}")
    public ResponseEntity<SupportTicketResponse> updateSupportTicketForStaff(@PathVariable UUID supportTicketId, @RequestBody UpdateSupportTicketRequest addSupportTicketRequest) {
        return ResponseEntity.ok(new SupportTicketResponse("Support ticket updated successfully", supportTicketService.updateSupportTicketForStaff(supportTicketId, addSupportTicketRequest)));
    }

    @PutMapping("/update/staff/resolution/{supportTicketId}")
    public ResponseEntity<SupportTicketResponse> updateTicketResolution(@PathVariable UUID supportTicketId, @RequestBody UpdateTicketResolutionRequest updateRequest) {
        return ResponseEntity.ok(new SupportTicketResponse("Ticket resolution updated successfully", supportTicketService.updateTicketResolution(supportTicketId, updateRequest)));
    }
}
