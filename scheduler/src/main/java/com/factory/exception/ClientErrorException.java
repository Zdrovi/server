package com.factory.exception;

import lombok.Getter;

@Getter
public class ClientErrorException extends RuntimeException {
    private final String code;

    public ClientErrorException(String code, String message) {
        super(message);
        this.code = code;
    }
}
