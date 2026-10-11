package com.cab.booking.realtime;

import com.cab.booking.ride.Ride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RealtimeNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyRideStatusUpdate(Ride ride) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "RIDE_STATUS_UPDATE");
        payload.put("rideId", ride.getId());
        payload.put("status", ride.getRideStatus().name());
        payload.put("driverId", ride.getDriver() != null ? ride.getDriver().getId() : null);
        payload.put("driverName", ride.getDriver() != null ? ride.getDriver().getUser().getFullName() : null);
        payload.put("driverPhone", ride.getDriver() != null ? ride.getDriver().getUser().getPhoneNumber() : null);
        payload.put("pickupAddress", ride.getPickupAddress());
        payload.put("dropoffAddress", ride.getDropoffAddress());
        payload.put("fare", ride.getFinalFare() != null ? ride.getFinalFare() : ride.getEstimatedFare());

        log.info("Broadcasting status {} for ride {} to /topic/rides/{}", ride.getRideStatus(), ride.getId(), ride.getId());
        messagingTemplate.convertAndSend("/topic/rides/" + ride.getId(), payload);
        messagingTemplate.convertAndSend("/topic/admin/rides", payload);
    }

    public void sendDriverRideRequest(Long driverId, Ride ride) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_RIDE_REQUEST");
        payload.put("rideId", ride.getId());
        payload.put("pickupAddress", ride.getPickupAddress());
        payload.put("pickupLatitude", ride.getPickupLatitude());
        payload.put("pickupLongitude", ride.getPickupLongitude());
        payload.put("dropoffAddress", ride.getDropoffAddress());
        payload.put("dropoffLatitude", ride.getDropoffLatitude());
        payload.put("dropoffLongitude", ride.getDropoffLongitude());
        payload.put("estimatedFare", ride.getEstimatedFare());
        payload.put("distanceKm", ride.getDistanceKm());
        payload.put("durationMinutes", ride.getEstimatedDurationMinutes());
        payload.put("riderName", ride.getRider().getFullName());

        log.info("Sending ride request {} to driver {}", ride.getId(), driverId);
        messagingTemplate.convertAndSend("/topic/drivers/" + driverId + "/requests", payload);
    }

    public void broadcastDriverLocation(Long rideId, double latitude, double longitude, double heading) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "DRIVER_LOCATION_UPDATE");
        payload.put("rideId", rideId);
        payload.put("latitude", latitude);
        payload.put("longitude", longitude);
        payload.put("heading", heading);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/rides/" + rideId + "/location", payload);
    }

    public void notifyRiderDirect(Long riderId, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "DIRECT_NOTIFICATION");
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/riders/" + riderId, payload);
    }

    public void broadcastSosAlert(Long rideId, Long userId, String userName, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SOS_ALERT");
        payload.put("rideId", rideId);
        payload.put("userId", userId);
        payload.put("userName", userName);
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());

        log.warn("SOS ALERT triggered for ride {} by user {}: {}", rideId, userId, message);
        messagingTemplate.convertAndSend("/topic/rides/" + rideId, payload);
        messagingTemplate.convertAndSend("/topic/admin/sos", payload);
    }
}
