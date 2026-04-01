package com.bothsann.wallet.shared.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException() {
        super("Wallet not found");
    }
}
