package com.cab.booking.support;

import com.cab.booking.common.ApiResponse;
import com.cab.booking.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/support")
@Tag(name = "Customer Support", description = "Endpoints for creating and viewing customer support and dispute tickets")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping("/tickets")
    @Operation(summary = "Submit a customer support or trip dispute ticket")
    public ResponseEntity<ApiResponse<SupportTicketDto>> createTicket(
            @Valid @RequestBody CreateSupportTicketRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SupportTicketDto ticket = supportService.createTicket(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Support ticket submitted", ticket));
    }

    @GetMapping("/tickets/me")
    @Operation(summary = "List current user's support tickets")
    public ResponseEntity<ApiResponse<Page<SupportTicketDto>>> getMyTickets(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SupportTicketDto> tickets = supportService.getMyTickets(principal, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(tickets));
    }
}
