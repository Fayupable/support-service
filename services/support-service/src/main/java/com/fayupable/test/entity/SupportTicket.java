package com.fayupable.test.entity;

import com.fayupable.test.enums.SupportPriority;
import com.fayupable.test.enums.SupportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "support_ticket")
@NamedEntityGraph(
        name = "SupportTicket.withDetails",
        attributeNodes = {
                @NamedAttributeNode("ticketDetails"),
                @NamedAttributeNode("resolutions")
        }
)
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "support_ticket_id")
    private UUID supportTicketId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "assigned_worker_id")
    private UUID assignedWorkerId;

    @Column(name = "attachment_id")
    private UUID attachmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportPriority priority;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TicketDetails> ticketDetails = new HashSet<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TicketResolution> resolutions= new HashSet<>();
}