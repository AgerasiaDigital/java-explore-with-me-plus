package ru.practicum.ewm.dto.event;

import ru.practicum.ewm.dto.user.Role;
import ru.practicum.ewm.model.event.EventState;

public enum StateAction {
    SEND_TO_REVIEW(Role.USER),
    CANCEL_REVIEW(Role.USER),

    PUBLISH_EVENT(Role.ADMIN),
    REJECT_EVENT(Role.ADMIN);

    private final Role allowedRole;

    StateAction(Role allowedRole) {
        this.allowedRole = allowedRole;
    }

    public Role getAllowedRole() {
        return allowedRole;
    }

    public EventState toEventState() {
        return switch (this) {
            case SEND_TO_REVIEW -> EventState.PENDING;
            case CANCEL_REVIEW, REJECT_EVENT -> EventState.CANCELED;
            case PUBLISH_EVENT -> EventState.PUBLISHED;
        };
    }
}