package com.flip.exception;

import io.jsonwebtoken.JwtException;

/**
 * 无效Token异常
 */
public class InvalidTokenException extends JwtException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
