package ru.practicum.ewm.controller;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.dto.ApiError;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.warn("400 {}", e.getMessage(), e);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);

        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        return new ApiError(String.join(". ", errors),
                e.getMessage(),
                "Method arguments was not valid",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.warn("400 {}", e.getMessage(), e);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);

        String stackTrace = stringWriter.toString();

        return new ApiError(stackTrace,
                e.getMessage(),
                "Недопустимое значение параметра",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.warn("404 {}", e.getMessage(), e);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);

        String stackTrace = stringWriter.toString();

        return new ApiError(stackTrace,
                e.getMessage(),
                "The required object was not found",
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.warn("409 {}", e.getMessage(), e);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);

        String stackTrace = stringWriter.toString();

        return new ApiError(stackTrace,
                e.getMessage(),
                "Parameter value conflict",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleHibernateConstraintViolationException(final ConstraintViolationException e) {
        log.warn("409 {}", e.getMessage(), e);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);

        return new ApiError(String.format("Constraint name: %s. Kind: %s", e.getConstraintName(), e.getKind()),
                e.getMessage(),
                "Integrity constraint has been violated",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        log.warn("500 {}", e.getMessage(), e);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);

        String stackTrace = stringWriter.toString();

        return new ApiError(stackTrace,
                e.getMessage(),
                "Unexpected error",
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                LocalDateTime.now().format(formatter));
    }

}
