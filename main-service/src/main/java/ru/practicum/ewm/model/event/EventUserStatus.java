package ru.practicum.ewm.model.event;

import java.util.Optional;

public enum EventUserStatus {
    SEND_TO_REVIEW,
    CANCEL_REVIEW;

    public static Optional<EventUserStatus> from(String stringStatus) {
        for (EventUserStatus state : values()) {
            if (state.name().equalsIgnoreCase(stringStatus)) {
                return Optional.of(state);
            }
        }

        return Optional.empty();
    }
}
