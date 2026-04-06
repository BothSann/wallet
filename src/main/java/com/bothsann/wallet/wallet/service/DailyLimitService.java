package com.bothsann.wallet.wallet.service;

import com.bothsann.wallet.shared.enums.TransactionStatus;
import com.bothsann.wallet.shared.enums.TransactionType;
import com.bothsann.wallet.shared.exception.DailyLimitExceededException;
import com.bothsann.wallet.transaction.repository.TransactionRepository;
import com.bothsann.wallet.wallet.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyLimitService {

    private final TransactionRepository transactionRepository;

    public BigDecimal getTodaySpend(UUID walletId) {
        LocalDateTime todayMidnightUtc = LocalDate.now(ZoneOffset.UTC).atStartOfDay();
        return transactionRepository.sumAmountByWalletIdAndTypeInAndStatusAndCreatedAtAfter(
                walletId,
                List.of(TransactionType.WITHDRAWAL, TransactionType.TRANSFER_OUT),
                TransactionStatus.SUCCESS,
                todayMidnightUtc
        );
    }

    public void checkLimit(Wallet wallet, BigDecimal requestedAmount) {
        BigDecimal todaySpend = getTodaySpend(wallet.getId());
        if (todaySpend.add(requestedAmount).compareTo(wallet.getDailyLimit()) > 0) {
            throw new DailyLimitExceededException(wallet.getDailyLimit(), todaySpend, requestedAmount);
        }
    }
}
