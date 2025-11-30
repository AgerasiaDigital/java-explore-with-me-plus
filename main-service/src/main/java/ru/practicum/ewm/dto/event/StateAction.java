package ru.practicum.ewm.dto.event;

import ru.practicum.ewm.model.event.EventState;

public enum StateAction {
    SEND_TO_REVIEW,   // пользователь отправляет на модерацию (оставляет PENDING)
    CANCEL_REVIEW,    // пользователь отменяет (переводит в CANCELED)

    PUBLISH_EVENT,    // админ публикует (PUBLISHED)
    REJECT_EVENT;     // админ отклоняет (CANCELED или отдельный REJECTED)

    public EventState toEventState() {
        return switch (this) {
            case SEND_TO_REVIEW -> EventState.PENDING;
            case CANCEL_REVIEW, REJECT_EVENT -> EventState.CANCELED;
            case PUBLISH_EVENT -> EventState.PUBLISHED;
        };
    }
}