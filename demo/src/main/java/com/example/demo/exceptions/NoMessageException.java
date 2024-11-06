package com.example.demo.exceptions;

public class NoMessageException extends RuntimeException{
    public  NoMessageException(String message) {
        super(message);
    }
}
