package com.bothsann.wallet.transaction.repository;

import com.bothsann.wallet.shared.enums.TransactionType;
import com.bothsann.wallet.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<Transaction> findByWalletId(UUID walletId, Pageable pageable);

    Page<Transaction> findByWalletIdAndType(UUID walletId, TransactionType type, Pageable pageable);

    Optional<Transaction> findByIdAndWalletId(UUID id, UUID walletId);
}
