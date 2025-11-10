package com.es.plus.web.config;

public class EsException extends RuntimeException {
    public EsException(String message) {
        super(message);
    }

    public EsException(String message, Throwable cause) {
        super(message, cause);
    }

    public EsException(Throwable cause) {
        super(cause);
    }

    public EsException() {
        super();
    }
}