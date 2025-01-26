package com.example.demo.exceptions;

public class UserNotAdminException extends Exception{
    public UserNotAdminException(String message) {
        super(message);
    }

}
