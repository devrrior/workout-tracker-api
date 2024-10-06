package com.example.APISkeleton.web.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ConflictException extends RuntimeException {
    private final HttpStatus httpStatus = HttpStatus.CONFLICT;

    public ConflictException(String message) {
        super(message);
    }
}
