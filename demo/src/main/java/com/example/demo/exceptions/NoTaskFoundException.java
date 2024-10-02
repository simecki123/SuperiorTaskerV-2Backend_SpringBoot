package com.example.demo.exceptions;

public class NoTaskFoundException extends RuntimeException{
    public NoTaskFoundException(String message) {
        super(message);
    }
}
