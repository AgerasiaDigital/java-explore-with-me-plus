package ru.practicum.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Location {
    // TODO: подумать над типом данных
    @NotNull
    private Double lat;

    @NotNull
    private Double lon;
}
