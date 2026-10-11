package com.cab.booking.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateSupportTicketRequest {

    private Long rideId;

    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 150)
    private String subject;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000)
    private String description;

    private TicketPriority priority = TicketPriority.MEDIUM;

    public CreateSupportTicketRequest() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
}
