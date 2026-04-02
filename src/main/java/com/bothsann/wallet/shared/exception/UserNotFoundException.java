package com.bothsann.wallet.shared.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("User not found: " + email);
    }

    public UserNotFoundException(UUID id) {
        super("User not found with id: " + id);
    }
}
