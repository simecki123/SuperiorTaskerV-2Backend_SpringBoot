package com.example.demo.exceptions;

public class NoProjectFoundException extends RuntimeException {
    public NoProjectFoundException(String message) {
        super(message);
    }
}
