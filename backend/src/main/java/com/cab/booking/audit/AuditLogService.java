package com.cab.booking.audit;

import com.cab.booking.security.UserPrincipal;
import com.cab.booking.user.User;
import com.cab.booking.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Non-blocking async audit logging.
     */
    @Async
    public void record(UserPrincipal principal, String action, String resourceType, String resourceId, String details, String ipAddress) {
        try {
            User actor = null;
            String email = null;
            String role = null;

            if (principal != null) {
                email = principal.getEmail();
                role = principal.getRole() != null ? principal.getRole().name() : null;
                actor = userRepository.findById(principal.getId()).orElse(null);
            }

            AuditLog entry = new AuditLog(actor, email, role, action, resourceType, resourceId, details, ipAddress);
            auditLogRepository.save(entry);
            log.info("AUDIT: [{}] {} on {}/{} by {}", action, details, resourceType, resourceId, email);
        } catch (Exception e) {
            log.error("Failed to record audit log: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogs(String action, String resourceType, Pageable pageable) {
        if (action != null && !action.isBlank() && resourceType != null && !resourceType.isBlank()) {
            return auditLogRepository.findByActionAndResourceType(action.trim(), resourceType.trim(), pageable)
                    .map(AuditLogDto::fromEntity);
        } else if (action != null && !action.isBlank()) {
            return auditLogRepository.findByAction(action.trim(), pageable).map(AuditLogDto::fromEntity);
        } else if (resourceType != null && !resourceType.isBlank()) {
            return auditLogRepository.findByResourceType(resourceType.trim(), pageable).map(AuditLogDto::fromEntity);
        }
        return auditLogRepository.findAll(pageable).map(AuditLogDto::fromEntity);
    }
}
