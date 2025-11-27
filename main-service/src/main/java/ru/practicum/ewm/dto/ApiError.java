package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApiError {
    private String errors;
    private String message;
    private String reason;
    private String status;
    private String timestamp;
}
