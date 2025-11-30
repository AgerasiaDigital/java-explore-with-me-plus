package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.ewm.model.event.Location;

@Data
public class UpdateEventAdminRequest { // TODO: patch-поведение
    @Pattern(regexp = "^(?!\\s*$).+") // допускает null
    @Size(min = 20)
    @Size(max = 2000)
    private String annotation;

    private Long category;

    @Size(min = 20)
    @Size(max = 7000)
    @Pattern(regexp = "^(?!\\s*$).+") // допускает null
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private String stateAction; // TODO: enum [ PUBLISH_EVENT, REJECT_EVENT ]

    @Size(min = 3)
    @Size(max = 120)
    @Pattern(regexp = "^(?!\\s*$).+") // допускает null
    private String title;

    public boolean hasAnnotation() {
        return annotation != null;
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public boolean hasPaid() {
        return paid != null;
    }

    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

    public boolean hasRequestModeration() {
        return requestModeration != null;
    }

    public boolean hasStateAction() {
        return stateAction != null;
    }

    public boolean hasTitle() {
        return title != null;
    }
}
