package com.example.demo.exceptions;

public class NoGroupFoundException extends RuntimeException {
    public NoGroupFoundException(String message) {
        super(message);
    }
}
