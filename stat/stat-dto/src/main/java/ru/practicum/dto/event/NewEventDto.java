package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewEventDto {
    @NotBlank
    @Size(min = 20)
    @Size(max = 2000)
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank
    @Size(min = 20)
    @Size(max = 7000)
    private String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate;

    @NotNull
    private Location location;

    private boolean paid = false;

    private int participantLimit = 0;

    private boolean requestModeration = true;

    @NotBlank
    @Size(min = 3)
    @Size(max = 120)
    private String title;
}
