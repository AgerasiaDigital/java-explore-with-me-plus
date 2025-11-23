package ru.practicum.statserver.exception;

public class ValidationException extends jakarta.validation.ValidationException {
    public ValidationException(String message) {
        super(message);
    }
}