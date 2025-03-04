package com.fayupable.test.kafka.support;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SupportTicketResolutionConfirmation {
    private UUID supportTicketId;
    private String email;
    private String resolutionMessage;
    private LocalDateTime resolvedAt;
    private SupportStatus status;
}