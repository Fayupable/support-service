package com.fayupable.test.kafka.support;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SupportTicketConfirmation {
    private UUID supportTicketId;
    private String email;
    private SupportStatus status;
}
