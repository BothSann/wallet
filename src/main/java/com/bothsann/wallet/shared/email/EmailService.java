package com.bothsann.wallet.shared.email;

import com.bothsann.wallet.shared.event.DepositSuccessEvent;
import com.bothsann.wallet.shared.event.TransferReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("emailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDepositSuccess(DepositSuccessEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(event.recipientEmail());
            message.setSubject("You received a deposit of $" + event.amount().toPlainString());
            message.setText(buildDepositBody(event.recipientName(), event.amount(), event.newBalance()));
            mailSender.send(message);
            log.info("Deposit notification sent to {}", event.recipientEmail());
        } catch (Exception e) {
            log.error("Failed to send deposit notification to {}: {}", event.recipientEmail(), e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransferReceived(TransferReceivedEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(event.recipientEmail());
            message.setSubject(event.senderEmail() + " sent you $" + event.amount().toPlainString());
            message.setText(buildTransferBody(event.recipientName(), event.senderEmail(), event.amount(), event.newBalance()));
            mailSender.send(message);
            log.info("Transfer notification sent to {}", event.recipientEmail());
        } catch (Exception e) {
            log.error("Failed to send transfer notification to {}: {}", event.recipientEmail(), e.getMessage());
        }
    }

    private String buildDepositBody(String recipientName, BigDecimal amount, BigDecimal newBalance) {
        return """
                Hello %s,

                You have received a deposit of $%s to your Digital Wallet.

                New balance: $%s
                Date: %s

                \u2014 Digital Wallet Team
                """.formatted(
                recipientName,
                amount.toPlainString(),
                newBalance.toPlainString(),
                Instant.now().toString()
        );
    }

    private String buildTransferBody(String recipientName, String senderEmail, BigDecimal amount, BigDecimal newBalance) {
        return """
                Hello %s,

                %s has transferred $%s to your Digital Wallet.

                New balance: $%s
                Date: %s

                \u2014 Digital Wallet Team
                """.formatted(
                recipientName,
                senderEmail,
                amount.toPlainString(),
                newBalance.toPlainString(),
                Instant.now().toString()
        );
    }
}
