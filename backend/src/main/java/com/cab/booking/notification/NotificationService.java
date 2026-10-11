package com.cab.booking.notification;

import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.ride.Ride;
import com.cab.booking.security.UserPrincipal;
import com.cab.booking.user.User;
import com.cab.booking.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createNotification(User user, String title, String message, String type, Ride ride) {
        Notification notification = new Notification(user, title, message, type, ride);
        Notification saved = notificationRepository.save(notification);
        log.debug("Created notification '{}' for user {}", title, user.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(UserPrincipal principal, Pageable pageable) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getId()));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(NotificationDto::fromEntity);
    }

    @Transactional
    public int markAllRead(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getId()));
        int updated = notificationRepository.markAllAsReadForUser(user);
        log.debug("Marked {} notifications as read for user {}", updated, principal.getId());
        return updated;
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getId()));
        return notificationRepository.countByUserAndReadFalse(user);
    }
}
