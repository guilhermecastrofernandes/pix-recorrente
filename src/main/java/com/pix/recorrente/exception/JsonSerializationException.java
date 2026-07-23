package com.pix.recorrente.exception;

public class JsonSerializationException extends RuntimeException {
    public JsonSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonSerializationException(String message) {
        super(message);
    }
}
