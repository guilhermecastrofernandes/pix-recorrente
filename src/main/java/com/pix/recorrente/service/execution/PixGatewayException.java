package com.pix.recorrente.service.execution;

public class PixGatewayException extends RuntimeException {
    public PixGatewayException(String message) {
        super(message);
    }

    public PixGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
