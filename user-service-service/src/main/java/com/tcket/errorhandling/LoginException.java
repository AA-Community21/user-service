package com.tcket.errorhandling;

import java.io.Serial;

public class LoginException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public LoginException(String message) {
        super(message);
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
