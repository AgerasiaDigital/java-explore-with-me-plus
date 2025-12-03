package ru.practicum.ewm.event;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.event.StateAction;
import ru.practicum.ewm.dto.user.Role;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.model.event.Event;

@Service
public class EventStateService {

    public void validateAction(StateAction action, Role role) {
        if (action.getAllowedRole() != role) {
            throw new ValidationException(
                    "Role " + role + " cannot perform action " + action
            );
        }
    }

    public void apply(Event event, StateAction action, Role role) {
        validateAction(action, role);
        event.setState(action.toEventState());
    }
}