package com.bothsann.wallet.shared.exception;

public class PinAlreadySetException extends RuntimeException {
    public PinAlreadySetException() {
        super("Transaction PIN is already set. Use the change PIN endpoint.");
    }
}
