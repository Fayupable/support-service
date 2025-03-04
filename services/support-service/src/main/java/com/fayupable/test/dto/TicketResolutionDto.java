package com.fayupable.test.dto;

import lombok.Data;
import org.apache.kafka.common.protocol.types.Field;

import java.time.LocalDateTime;

@Data
public class TicketResolutionDto {
    private String resolutionMessage;
    private LocalDateTime closedAt;
}
