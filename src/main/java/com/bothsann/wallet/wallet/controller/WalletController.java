package com.bothsann.wallet.wallet.controller;

import com.bothsann.wallet.transaction.dto.TransactionResponse;
import com.bothsann.wallet.user.entity.User;
import com.bothsann.wallet.wallet.dto.DepositRequest;
import com.bothsann.wallet.wallet.dto.TransferRequest;
import com.bothsann.wallet.wallet.dto.WalletResponse;
import com.bothsann.wallet.wallet.dto.WithdrawRequest;
import com.bothsann.wallet.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getWallet(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(walletService.getWallet(currentUser.getId()));
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(walletService.getWallet(currentUser.getId()).balance());
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody DepositRequest req,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(walletService.deposit(currentUser.getId(), req, idempotencyKey));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody WithdrawRequest req,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(walletService.withdraw(currentUser.getId(), req, idempotencyKey));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TransferRequest req,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(walletService.transfer(currentUser.getId(), req, idempotencyKey));
    }
}
