package com.chronopost.vision.microservices.exception;

public class MSTechnicalException extends RuntimeException {

    private static final long serialVersionUID = 7817237556800955993L;

    public MSTechnicalException() {
    }

    public MSTechnicalException(String message) {
        super(message);
    }

    public MSTechnicalException(Throwable cause) {
        super(cause);
    }

    public MSTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public MSTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}