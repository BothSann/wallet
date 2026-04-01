package com.bothsann.wallet.shared.exception;

public class SelfTransferException extends RuntimeException {
    public SelfTransferException() {
        super("Cannot transfer to your own wallet");
    }
}
