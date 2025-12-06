package ru.practicum.ewm.event;

import lombok.Data;
import ru.practicum.ewm.model.event.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventFilter {
    private String text;
    private List<Long> categories;
    private EventState state;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private Boolean paid;
}
