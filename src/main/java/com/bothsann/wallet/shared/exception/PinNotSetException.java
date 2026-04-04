package com.bothsann.wallet.shared.exception;

public class PinNotSetException extends RuntimeException {
    public PinNotSetException() {
        super("Transaction PIN is not set. Please set your PIN first.");
    }
}
