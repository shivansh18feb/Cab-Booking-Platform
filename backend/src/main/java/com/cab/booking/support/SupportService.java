package com.cab.booking.support;

import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.ride.Ride;
import com.cab.booking.ride.RideRepository;
import com.cab.booking.security.UserPrincipal;
import com.cab.booking.user.User;
import com.cab.booking.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final RideRepository rideRepository;

    public SupportService(SupportTicketRepository supportTicketRepository,
                          UserRepository userRepository,
                          RideRepository rideRepository) {
        this.supportTicketRepository = supportTicketRepository;
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
    }

    @Transactional
    public SupportTicketDto createTicket(CreateSupportTicketRequest request, UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ride ride = null;
        if (request.getRideId() != null) {
            ride = rideRepository.findById(request.getRideId()).orElse(null);
        }

        SupportTicket ticket = new SupportTicket(
                user,
                ride,
                request.getSubject().trim(),
                request.getDescription().trim(),
                request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM
        );
        ticket = supportTicketRepository.save(ticket);
        return SupportTicketDto.fromEntity(ticket);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketDto> getMyTickets(UserPrincipal principal, Pageable pageable) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return supportTicketRepository.findByCreatedByOrderByCreatedAtDesc(user, pageable)
                .map(SupportTicketDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketDto> getAllTickets(TicketStatus status, Pageable pageable) {
        if (status != null) {
            return supportTicketRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(SupportTicketDto::fromEntity);
        }
        return supportTicketRepository.findAll(pageable).map(SupportTicketDto::fromEntity);
    }

    @Transactional
    public SupportTicketDto updateTicket(Long ticketId, UpdateTicketStatusRequest request, UserPrincipal adminPrincipal) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found: " + ticketId));

        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
        }
        if (request.getResolutionNotes() != null && !request.getResolutionNotes().isBlank()) {
            ticket.setResolutionNotes(request.getResolutionNotes().trim());
        }
        if (request.getAssignedAdminId() != null) {
            User admin = userRepository.findById(request.getAssignedAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + request.getAssignedAdminId()));
            ticket.setAssignedAdmin(admin);
        } else if (ticket.getAssignedAdmin() == null && adminPrincipal != null) {
            userRepository.findById(adminPrincipal.getId()).ifPresent(ticket::setAssignedAdmin);
        }

        ticket = supportTicketRepository.save(ticket);
        return SupportTicketDto.fromEntity(ticket);
    }
}
