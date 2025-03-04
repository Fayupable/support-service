package com.fayupable.test.kafka;

import com.fayupable.test.enums.SupportStatus;
import lombok.*;

import java.util.UUID;
import java.time.LocalDateTime;

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