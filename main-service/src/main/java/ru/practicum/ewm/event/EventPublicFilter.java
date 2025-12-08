package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.annotation.EndAfterStart;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EndAfterStart
public class EventPublicFilter {
    private String text;
    @Valid
    private List<@Positive(message = "categoryId должен быть > 0") Long> categories;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private Boolean paid;

    public List<Long> getCategory() {
        return categories;
    }
}
