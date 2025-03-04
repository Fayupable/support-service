package com.fayupable.test.enums;

import lombok.Getter;

@Getter
public enum EmailTemplate {
    USER_VERIFICATION("UserVerification.html", "User Verification Successful"),
    SUPPORT_TICKET_CONFIRMATION("SupportTicketConfirmation.html", "Support Ticket Confirmation"),
    SUPPORT_TICKET_RESOLUTION_CONFIRMATION("SupportTicketResolutionConfirmation.html", "Support Ticket Resolution Confirmation");
    private final String template;
    private final String subject;

    EmailTemplate(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}
