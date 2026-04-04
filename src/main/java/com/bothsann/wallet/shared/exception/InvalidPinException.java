package com.bothsann.wallet.shared.exception;

public class InvalidPinException extends RuntimeException {
    public InvalidPinException() {
        super("Invalid PIN");
    }
}
