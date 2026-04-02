package com.bothsann.wallet.transaction.service;

import com.bothsann.wallet.shared.dto.PageResponse;
import com.bothsann.wallet.shared.enums.TransactionType;
import com.bothsann.wallet.shared.exception.TransactionNotFoundException;
import com.bothsann.wallet.shared.exception.WalletNotFoundException;
import com.bothsann.wallet.transaction.dto.TransactionResponse;
import com.bothsann.wallet.transaction.repository.TransactionRepository;
import com.bothsann.wallet.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public PageResponse<TransactionResponse> getHistory(UUID userId, TransactionType type, Pageable pageable) {
        var wallet = walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);

        Page<TransactionResponse> page = type != null
                ? transactionRepository.findByWalletIdAndType(wallet.getId(), type, pageable)
                        .map(TransactionResponse::from)
                : transactionRepository.findByWalletId(wallet.getId(), pageable)
                        .map(TransactionResponse::from);

        return PageResponse.of(page);
    }

    public TransactionResponse getById(UUID userId, UUID transactionId) {
        var wallet = walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);

        return transactionRepository.findByIdAndWalletId(transactionId, wallet.getId())
                .map(TransactionResponse::from)
                .orElseThrow(TransactionNotFoundException::new);
    }
}
