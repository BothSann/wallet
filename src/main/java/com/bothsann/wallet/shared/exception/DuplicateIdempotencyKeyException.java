package com.bothsann.wallet.shared.exception;

public class DuplicateIdempotencyKeyException extends RuntimeException {
    public DuplicateIdempotencyKeyException(String key) {
        super("Duplicate idempotency key: " + key);
    }
}
