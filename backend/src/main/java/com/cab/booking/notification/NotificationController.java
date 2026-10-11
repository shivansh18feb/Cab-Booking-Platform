package com.cab.booking.notification;

import com.cab.booking.common.ApiResponse;
import com.cab.booking.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Persistent notification management for all user roles")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get paginated notifications for the authenticated user")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
        Page<NotificationDto> notifications = notificationService.getNotifications(principal, pageRequest);
        return ResponseEntity.ok(ApiResponse.ok(notifications));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all unread notifications as read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        int updated = notificationService.markAllRead(principal);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", Map.of("updatedCount", updated)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications for the authenticated user")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("unreadCount", count)));
    }
}
