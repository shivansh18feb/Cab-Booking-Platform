package com.cab.booking.email;

import com.cab.booking.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Transactional email service for CabFlow.
 *
 * <p>All public methods are {@code @Async} — email delivery never blocks the
 * caller's HTTP response.  If SMTP is not configured (local dev / CI), the
 * method logs a warning and returns without throwing, so the application
 * continues to function.
 *
 * <p>Required environment variables:
 * <pre>
 *   SPRING_MAIL_HOST
 *   SPRING_MAIL_PORT
 *   SPRING_MAIL_USERNAME
 *   SPRING_MAIL_PASSWORD
 *   APP_MAIL_FROM  (optional, default noreply@cabflow.app)
 * </pre>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public EmailService(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    // -----------------------------------------------------------------------
    // Password reset
    // -----------------------------------------------------------------------

    /**
     * Sends a password-reset link to the given email address.
     *
     * @param toEmail   recipient email
     * @param resetToken raw (un-hashed) reset token
     * @param frontendBaseUrl base URL of the deployed frontend (e.g. https://cab-booking-platform-seven.vercel.app)
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken, String frontendBaseUrl) {
        String resetLink = frontendBaseUrl + "/reset-password?token=" + resetToken;
        String subject = "CabFlow — Reset your password";
        String html = """
                <html><body style="font-family:sans-serif;color:#222;">
                <h2 style="color:#2563eb;">CabFlow Password Reset</h2>
                <p>We received a request to reset the password for your CabFlow account.</p>
                <p>Click the button below to choose a new password. This link expires in <strong>1 hour</strong>.</p>
                <p style="margin:24px 0;">
                  <a href="%s"
                     style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none;font-weight:bold;">
                    Reset Password
                  </a>
                </p>
                <p style="color:#666;font-size:13px;">If you didn't request this, you can safely ignore this email.</p>
                <hr style="border:none;border-top:1px solid #e5e7eb;margin:24px 0;">
                <p style="color:#999;font-size:12px;">CabFlow — Your ride, your way.</p>
                </body></html>
                """.formatted(resetLink);

        sendHtml(toEmail, subject, html);
    }

    // -----------------------------------------------------------------------
    // Email verification
    // -----------------------------------------------------------------------

    /**
     * Sends an email-verification link.
     *
     * @param toEmail   recipient email
     * @param fullName  display name for personalisation
     * @param verifyToken raw (un-hashed) verification token
     * @param frontendBaseUrl base URL of the deployed frontend
     */
    @Async
    public void sendEmailVerification(String toEmail, String fullName, String verifyToken, String frontendBaseUrl) {
        String verifyLink = frontendBaseUrl + "/verify-email?token=" + verifyToken;
        String subject = "CabFlow — Please verify your email";
        String html = """
                <html><body style="font-family:sans-serif;color:#222;">
                <h2 style="color:#2563eb;">Welcome to CabFlow, %s!</h2>
                <p>Thanks for signing up. Please verify your email address to activate your account.</p>
                <p style="margin:24px 0;">
                  <a href="%s"
                     style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none;font-weight:bold;">
                    Verify Email
                  </a>
                </p>
                <p style="color:#666;font-size:13px;">This link expires in 24 hours.</p>
                <hr style="border:none;border-top:1px solid #e5e7eb;margin:24px 0;">
                <p style="color:#999;font-size:12px;">CabFlow — Your ride, your way.</p>
                </body></html>
                """.formatted(fullName, verifyLink);

        sendHtml(toEmail, subject, html);
    }

    // -----------------------------------------------------------------------
    // Payment confirmation
    // -----------------------------------------------------------------------

    /**
     * Sends a payment-confirmation email after a successful ride payment.
     *
     * @param toEmail      rider's email
     * @param fullName     rider's display name
     * @param rideId       the ride ID
     * @param amount       amount paid in INR
     * @param paymentId    Razorpay payment ID (e.g. pay_XXXXXX)
     */
    @Async
    public void sendPaymentConfirmation(String toEmail, String fullName, Long rideId,
                                        BigDecimal amount, String paymentId) {
        String subject = "CabFlow — Payment confirmed for Ride #" + rideId;
        String html = """
                <html><body style="font-family:sans-serif;color:#222;">
                <h2 style="color:#16a34a;">Payment Successful ✓</h2>
                <p>Hi %s, your payment for Ride #%d has been confirmed.</p>
                <table style="border-collapse:collapse;width:100%%;max-width:400px;margin:16px 0;">
                  <tr><td style="padding:8px;border-bottom:1px solid #e5e7eb;color:#666;">Amount paid</td>
                      <td style="padding:8px;border-bottom:1px solid #e5e7eb;font-weight:bold;">₹%s</td></tr>
                  <tr><td style="padding:8px;color:#666;">Razorpay ID</td>
                      <td style="padding:8px;font-size:12px;color:#555;">%s</td></tr>
                </table>
                <p>You can download your receipt from the ride history page in the CabFlow app.</p>
                <hr style="border:none;border-top:1px solid #e5e7eb;margin:24px 0;">
                <p style="color:#999;font-size:12px;">CabFlow — Your ride, your way.</p>
                </body></html>
                """.formatted(fullName, rideId, amount.toPlainString(), paymentId);

        sendHtml(toEmail, subject, html);
    }

    // -----------------------------------------------------------------------
    // Internal helper
    // -----------------------------------------------------------------------

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(appProperties.getMail().getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {} — subject: {} — error: {}", to, subject, e.getMessage());
        } catch (Exception e) {
            // org.springframework.mail.MailSendException wraps transport errors
            log.error("Mail transport error sending to {} — {}: {}", to, e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
