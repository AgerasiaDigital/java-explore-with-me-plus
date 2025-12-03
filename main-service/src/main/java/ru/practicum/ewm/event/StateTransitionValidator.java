package ru.practicum.ewm.event;

import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.event.StateAction;

public class StateTransitionValidator {

    public static EventState changeState(EventState current, StateAction action, boolean isAdmin) {



        switch (action) {

            case SEND_TO_REVIEW:
                if (current == EventState.CANCELED || current == EventState.PENDING)
                    return EventState.PENDING;
                throw new IllegalStateException("Нельзя отправить в ревью из состояния " + current);

            case CANCEL_REVIEW:
                if (current == EventState.PENDING)
                    return EventState.CANCELED;
                throw new IllegalStateException("Отмена допустима только из PENDING");

            case PUBLISH_EVENT:
                if (!isAdmin)
                    throw new SecurityException("Публиковать может только администратор");
                if (current == EventState.PENDING)
                    return EventState.PUBLISHED;
                throw new IllegalStateException("Публиковать можно только PENDING");

            case REJECT_EVENT:
                if (!isAdmin)
                    throw new SecurityException("Отклонять может только администратор");
                if (current == EventState.PENDING)
                    return EventState.CANCELED;
                throw new IllegalStateException("Отклонять можно только PENDING");
        }

        throw new IllegalArgumentException("Неизвестное действие");
    }
}