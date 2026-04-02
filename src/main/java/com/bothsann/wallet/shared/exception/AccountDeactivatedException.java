package com.bothsann.wallet.shared.exception;

public class AccountDeactivatedException extends RuntimeException {
    public AccountDeactivatedException() {
        super("Account is deactivated");
    }
}
