package ru.practicum.ewm.model.event;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Location {
    private Double lat;
    private Double lon;
}