package ru.practicum.ewm.exception;

public class EmailConflictException extends RuntimeException {
    public EmailConflictException(String message) {
        super(message);
    }
}
