package org.openspp.keycloak.user.storage;

public class UserStorageException extends RuntimeException {

    public UserStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserStorageException(Throwable cause) {
        super(cause);
    }

    public UserStorageException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
