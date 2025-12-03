package ru.practicum.ewm.event;

import ru.practicum.ewm.dto.event.StateAction;
import ru.practicum.ewm.exception.ForbiddenStateChangeException;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;

public final class StateTransitionValidator {

    private StateTransitionValidator() {
    }

    public static void changeState(Event event, StateAction action) {
        EventState current = event.getState();

        switch (action) {
            case SEND_TO_REVIEW:
                // нельзя отправлять в ревью, если уже опубликовано
                if (current == EventState.PUBLISHED) {
                    throw new ForbiddenStateChangeException("Нельзя отправить на модерацию уже опубликованное событие");
                }
                // в остальных случаях допустимо (например, PENDING или CANCELED -> можно попытаться отправить)
                return;

            case CANCEL_REVIEW:
                // отменить можно только если сейчас PENDING (в ожидании)
                if (current != EventState.PENDING) {
                    throw new ForbiddenStateChangeException("Отменить можно только событие в ожидании публикации (PENDING)");
                }
                return;

            case PUBLISH_EVENT:
                // публиковать можно только из PENDING
                if (current != EventState.PENDING) {
                    throw new ForbiddenStateChangeException("Публиковать можно только событие в статусе PENDING");
                }
                return;

            case REJECT_EVENT:
                // отклонять можно только из PENDING
                if (current != EventState.PENDING) {
                    throw new ForbiddenStateChangeException("Отклонить можно только событие в статусе PENDING");
                }
                return;

            default:
                throw new ForbiddenStateChangeException("Неизвестное действие: " + action);
        }
    }
}
