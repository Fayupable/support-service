package com.fayupable.test.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_resolution")
public class TicketResolution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "resolution_id")
    private UUID resolutionId;

    @Column(length = 2000)
    private String resolutionMessage;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_ticket_id")
    private SupportTicket ticket;

    @PrePersist
    public void prePersist() {
        this.closedAt = LocalDateTime.now();
    }


}
