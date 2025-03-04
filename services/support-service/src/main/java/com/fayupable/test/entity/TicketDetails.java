package com.fayupable.test.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticket_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID ticketDetailsId;

    @Column(length = 2000)
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_ticket_id")
    private SupportTicket ticket;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }


}