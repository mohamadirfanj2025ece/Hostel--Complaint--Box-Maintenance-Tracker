package com.example.complaintbox.exception;

public class InvalidStatusChangeException extends RuntimeException {
    public InvalidStatusChangeException() {
        super("Invalid status change");
    }
}
