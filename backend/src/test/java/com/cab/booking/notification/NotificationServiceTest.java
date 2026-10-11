package com.cab.booking.notification;

import com.cab.booking.security.UserPrincipal;
import com.cab.booking.user.Role;
import com.cab.booking.user.User;
import com.cab.booking.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@cabflow.app", "+14155551111", "pass", Role.RIDER);
        user.setId(55L);
        userPrincipal = new UserPrincipal(55L, "Alice", "alice@cabflow.app", "pass", Role.RIDER, true, Collections.emptyList());
    }

    @Test
    @DisplayName("Should create persistent notification for user")
    void shouldCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification notif = notificationService.createNotification(user, "Ride Confirmed", "Driver is arriving", "RIDE", null);

        assertNotNull(notif);
        assertEquals("Ride Confirmed", notif.getTitle());
        assertEquals("Driver is arriving", notif.getMessage());
        assertEquals("RIDE", notif.getNotificationType());
        assertFalse(notif.isRead());
    }

    @Test
    @DisplayName("Should retrieve paginated user notifications")
    void shouldGetNotifications() {
        Notification n1 = new Notification(user, "Alert 1", "Body 1", "SYSTEM", null);
        Page<Notification> page = new PageImpl<>(List.of(n1));

        when(userRepository.findById(55L)).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserOrderByCreatedAtDesc(eq(user), any(PageRequest.class))).thenReturn(page);

        Page<NotificationDto> result = notificationService.getNotifications(userPrincipal, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Alert 1", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Should mark all notifications as read for user")
    void shouldMarkAllRead() {
        when(userRepository.findById(55L)).thenReturn(Optional.of(user));
        when(notificationRepository.markAllAsReadForUser(user)).thenReturn(3);

        int updated = notificationService.markAllRead(userPrincipal);

        assertEquals(3, updated);
        verify(notificationRepository).markAllAsReadForUser(user);
    }

    @Test
    @DisplayName("Should return accurate unread notification count")
    void shouldGetUnreadCount() {
        when(userRepository.findById(55L)).thenReturn(Optional.of(user));
        when(notificationRepository.countByUserAndReadFalse(user)).thenReturn(5L);

        long count = notificationService.getUnreadCount(userPrincipal);

        assertEquals(5L, count);
    }
}
