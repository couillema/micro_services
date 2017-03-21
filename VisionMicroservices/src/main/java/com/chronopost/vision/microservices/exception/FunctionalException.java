package com.chronopost.vision.microservices.exception;

public class FunctionalException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2694186981954855100L;

    public FunctionalException() {
    }

    public FunctionalException(String message) {
        super(message);
    }

    public FunctionalException(Throwable cause) {
        super(cause);
    }

    public FunctionalException(String message, Throwable cause) {
        super(message, cause);
    }

    public FunctionalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
