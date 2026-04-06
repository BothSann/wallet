package com.bothsann.wallet.wallet.service;

import com.bothsann.wallet.shared.enums.TransactionStatus;
import com.bothsann.wallet.shared.enums.TransactionType;
import com.bothsann.wallet.shared.event.DepositSuccessEvent;
import com.bothsann.wallet.shared.event.TransferReceivedEvent;
import com.bothsann.wallet.shared.exception.DailyLimitCapExceededException;
import com.bothsann.wallet.shared.exception.DuplicateIdempotencyKeyException;
import com.bothsann.wallet.shared.exception.InsufficientBalanceException;
import com.bothsann.wallet.shared.exception.InvalidPinException;
import com.bothsann.wallet.shared.exception.PinAlreadySetException;
import com.bothsann.wallet.shared.exception.PinNotSetException;
import com.bothsann.wallet.shared.exception.SelfTransferException;
import com.bothsann.wallet.shared.exception.UserNotFoundException;
import com.bothsann.wallet.shared.exception.WalletNotFoundException;
import com.bothsann.wallet.transaction.dto.TransactionResponse;
import com.bothsann.wallet.transaction.entity.Transaction;
import com.bothsann.wallet.transaction.repository.TransactionRepository;
import com.bothsann.wallet.user.repository.UserRepository;
import com.bothsann.wallet.wallet.dto.ChangePinRequest;
import com.bothsann.wallet.wallet.dto.DailyLimitResponse;
import com.bothsann.wallet.wallet.dto.DepositRequest;
import com.bothsann.wallet.wallet.dto.SetPinRequest;
import com.bothsann.wallet.wallet.dto.TransferRequest;
import com.bothsann.wallet.wallet.dto.UpdateDailyLimitRequest;
import com.bothsann.wallet.wallet.dto.WalletResponse;
import com.bothsann.wallet.wallet.dto.WithdrawRequest;
import com.bothsann.wallet.wallet.entity.Wallet;
import com.bothsann.wallet.wallet.repository.WalletRepository;
import com.bothsann.wallet.shared.config.WalletProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DailyLimitService dailyLimitService;
    private final WalletProperties walletProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);
        return WalletResponse.from(wallet);
    }

    public void setPin(UUID userId, SetPinRequest req) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);
        if (wallet.getPinHash() != null) {
            throw new PinAlreadySetException();
        }
        wallet.setPinHash(passwordEncoder.encode(req.pin()));
        walletRepository.save(wallet);
    }

    public void changePin(UUID userId, ChangePinRequest req) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);
        if (wallet.getPinHash() == null) {
            throw new PinNotSetException();
        }
        if (!passwordEncoder.matches(req.currentPin(), wallet.getPinHash())) {
            throw new InvalidPinException();
        }
        wallet.setPinHash(passwordEncoder.encode(req.newPin()));
        walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public DailyLimitResponse getDailyLimitStatus(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);
        BigDecimal todaySpend = dailyLimitService.getTodaySpend(wallet.getId());
        BigDecimal remaining = wallet.getDailyLimit().subtract(todaySpend);
        Instant resetAt = LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return new DailyLimitResponse(wallet.getDailyLimit(), todaySpend, remaining, resetAt);
    }

    public DailyLimitResponse updateDailyLimit(UUID userId, UpdateDailyLimitRequest req) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);
        if (req.dailyLimit().compareTo(walletProperties.getMaxDailyLimit()) > 0) {
            throw new DailyLimitCapExceededException(walletProperties.getMaxDailyLimit());
        }
        wallet.setDailyLimit(req.dailyLimit());
        walletRepository.save(wallet);
        return getDailyLimitStatus(userId);
    }

    public TransactionResponse deposit(UUID userId, DepositRequest req, String idempotencyKey) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateIdempotencyKeyException(idempotencyKey);
        }
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);

        BigDecimal balanceBefore = wallet.getBalance();
        Transaction tx = transactionRepository.save(Transaction.builder()
                .wallet(wallet)
                .idempotencyKey(idempotencyKey)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .amount(req.amount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceBefore)
                .description(req.description())
                .build());

        wallet.setBalance(balanceBefore.add(req.amount()));
        walletRepository.save(wallet);

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setBalanceAfter(wallet.getBalance());
        transactionRepository.save(tx);

        eventPublisher.publishEvent(new DepositSuccessEvent(
                wallet.getUser().getEmail(),
                wallet.getUser().getFullName(),
                req.amount(),
                wallet.getBalance()
        ));

        return TransactionResponse.from(tx);
    }

    public TransactionResponse withdraw(UUID userId, WithdrawRequest req, String idempotencyKey) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateIdempotencyKeyException(idempotencyKey);
        }
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);

        verifyPin(wallet, req.pin());
        dailyLimitService.checkLimit(wallet, req.amount());

        if (wallet.getBalance().compareTo(req.amount()) < 0) {
            throw new InsufficientBalanceException(wallet.getBalance(), req.amount());
        }

        BigDecimal balanceBefore = wallet.getBalance();
        Transaction tx = transactionRepository.save(Transaction.builder()
                .wallet(wallet)
                .idempotencyKey(idempotencyKey)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .amount(req.amount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceBefore)
                .description(req.description())
                .build());

        wallet.setBalance(balanceBefore.subtract(req.amount()));
        walletRepository.save(wallet);

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setBalanceAfter(wallet.getBalance());
        transactionRepository.save(tx);

        return TransactionResponse.from(tx);
    }

    public TransactionResponse transfer(UUID senderId, TransferRequest req, String idempotencyKey) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateIdempotencyKeyException(idempotencyKey);
        }

        Wallet senderWallet = walletRepository.findByUserId(senderId)
                .orElseThrow(WalletNotFoundException::new);

        verifyPin(senderWallet, req.pin());
        dailyLimitService.checkLimit(senderWallet, req.amount());

        var recipient = userRepository.findByEmail(req.recipientEmail())
                .orElseThrow(() -> new UserNotFoundException(req.recipientEmail()));

        if (senderId.equals(recipient.getId())) {
            throw new SelfTransferException();
        }

        Wallet recipientWallet = walletRepository.findByUserId(recipient.getId())
                .orElseThrow(WalletNotFoundException::new);

        if (senderWallet.getBalance().compareTo(req.amount()) < 0) {
            throw new InsufficientBalanceException(senderWallet.getBalance(), req.amount());
        }

        BigDecimal senderBalanceBefore = senderWallet.getBalance();
        BigDecimal recipientBalanceBefore = recipientWallet.getBalance();

        Transaction senderTx = transactionRepository.save(Transaction.builder()
                .wallet(senderWallet)
                .idempotencyKey(idempotencyKey)
                .type(TransactionType.TRANSFER_OUT)
                .status(TransactionStatus.PENDING)
                .amount(req.amount())
                .balanceBefore(senderBalanceBefore)
                .balanceAfter(senderBalanceBefore)
                .description(req.description())
                .build());

        Transaction recipientTx = transactionRepository.save(Transaction.builder()
                .wallet(recipientWallet)
                .idempotencyKey(idempotencyKey + "-in")
                .type(TransactionType.TRANSFER_IN)
                .status(TransactionStatus.PENDING)
                .amount(req.amount())
                .balanceBefore(recipientBalanceBefore)
                .balanceAfter(recipientBalanceBefore)
                .description(req.description())
                .build());

        senderWallet.setBalance(senderBalanceBefore.subtract(req.amount()));
        walletRepository.save(senderWallet);

        recipientWallet.setBalance(recipientBalanceBefore.add(req.amount()));
        walletRepository.save(recipientWallet);

        senderTx.setStatus(TransactionStatus.SUCCESS);
        senderTx.setBalanceAfter(senderWallet.getBalance());
        transactionRepository.save(senderTx);

        recipientTx.setStatus(TransactionStatus.SUCCESS);
        recipientTx.setBalanceAfter(recipientWallet.getBalance());
        transactionRepository.save(recipientTx);

        eventPublisher.publishEvent(new TransferReceivedEvent(
                recipient.getEmail(),
                recipient.getFullName(),
                senderWallet.getUser().getEmail(),
                req.amount(),
                recipientWallet.getBalance()
        ));

        return TransactionResponse.from(senderTx);
    }

    private void verifyPin(Wallet wallet, String submittedPin) {
        if (wallet.getPinHash() == null) {
            throw new PinNotSetException();
        }
        if (submittedPin == null || submittedPin.isBlank() || !passwordEncoder.matches(submittedPin, wallet.getPinHash())) {
            throw new InvalidPinException();
        }
    }
}
