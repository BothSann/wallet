package com.bothsann.wallet.shared.exception;

public class SelfDeactivationException extends RuntimeException {
    public SelfDeactivationException() {
        super("You cannot deactivate your own account");
    }
}
