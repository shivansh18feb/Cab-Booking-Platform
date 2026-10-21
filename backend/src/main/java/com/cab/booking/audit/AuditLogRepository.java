package com.cab.booking.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByAction(String action, Pageable pageable);
    Page<AuditLog> findByResourceType(String resourceType, Pageable pageable);
    Page<AuditLog> findByActorEmail(String actorEmail, Pageable pageable);
    Page<AuditLog> findByActionAndResourceType(String action, String resourceType, Pageable pageable);
}
