package ru.practicum.dto.event;

import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    private String status; // TODO: enum [ CONFIRMED, REJECTED ]
}
