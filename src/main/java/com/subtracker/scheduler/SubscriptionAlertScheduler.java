package com.subtracker.scheduler;

import com.subtracker.entity.Notification;
import com.subtracker.entity.Subscription;
import com.subtracker.repository.NotificationRepository;
import com.subtracker.repository.SubscriptionRepository;
import com.subtracker.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionAlertScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    private static final int ALERT_DAYS_BEFORE_BILLING = 3;

    // Runs once every day at 8 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void generateUnusedSubscriptionAlerts() {
        LocalDate targetDate = LocalDate.now().plusDays(ALERT_DAYS_BEFORE_BILLING);

        List<Subscription> unusedSubscriptions =
                subscriptionRepository.findByActiveTrueAndUsedRecentlyFalseAndNextBillingDate(targetDate);

        for (Subscription subscription : unusedSubscriptions) {
            String message = String.format(
                    "You haven't used %s recently, but ₹%s will be charged on %s. Cancel now to avoid wasting money.",
                    subscription.getServiceName(),
                    subscription.getAmount(),
                    subscription.getNextBillingDate()
            );

            Notification notification = Notification.builder()
                    .message(message)
                    .user(subscription.getUser())
                    .subscription(subscription)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

            emailService.sendAlertEmail(
                    subscription.getUser().getEmail(),
                    "Subscription Alert: " + subscription.getServiceName(),
                    message
            );
        }
    }
}