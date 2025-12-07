package ru.practicum.ewm.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventRequest;

import java.util.Collection;
import java.util.Map;

public interface EventService {
    EventFullDto create(Long userId, NewEventDto newEventDto);
    Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter eventInitiatorIdFilter,
                                                  Pageable pageable);
    EventFullDto getEventFullDescription(Long userId, Long eventId);
    EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventRequest updateEventRequest);
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest updateEventRequest);
    Page<EventFullDto> adminSearchEvents(EventAdminFilter eventAdminFilter, Pageable pageable);

    Page<EventFullDto> publicSearchEvents(EventPublicFilter eventPublicFilter, Pageable pageable);

    EventFullDto getEvent(Long eventId);
}
