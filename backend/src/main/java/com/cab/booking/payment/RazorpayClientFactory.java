package com.cab.booking.payment;

import com.cab.booking.common.BadRequestException;
import com.cab.booking.config.AppProperties;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.stereotype.Component;

@Component
public class RazorpayClientFactory {

    private final AppProperties appProperties;

    public RazorpayClientFactory(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public boolean hasValidCredentials() {
        String keyId = appProperties.getRazorpay().getKeyId();
        String keySecret = appProperties.getRazorpay().getKeySecret();
        return keyId != null && !keyId.isBlank() && keySecret != null && !keySecret.isBlank();
    }

    public RazorpayClient createClient() throws RazorpayException {
        String keyId = appProperties.getRazorpay().getKeyId();
        String keySecret = appProperties.getRazorpay().getKeySecret();

        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new BadRequestException("Razorpay API credentials (RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET) are not configured.");
        }

        return new RazorpayClient(keyId, keySecret);
    }
}
