package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;

import java.util.Collection;

@Slf4j
@AllArgsConstructor
@RestController

public class EventController {
    EventService eventService;

    // Создание события пользователем
    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Запрос на создание события, userId={}", userId);
        log.debug("newEventDto: {}", newEventDto);
        return eventService.create(userId, newEventDto);
    }

    // Получение всех событий созданных пользователем, без подробностей
    @GetMapping("/users/{userId}/events")
    public Collection<EventShortDto> getEventsOfUser(@PathVariable Long userId) {
        log.info("Запрос событий пользователя, userId={}", userId);
        Collection<EventShortDto> events = eventService.getEvent(userId);
        log.info("Найдено событий: {}", events.size());
        events.forEach(ev -> log.debug("EVENT: {}", ev));
        return events;
    }

    // Получение подробного описания события созданного пользователем
    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventFullDescription(@PathVariable Long userId,
                                                @PathVariable Long eventId) {
        log.info("Запрос пользователем события с подробных описанием, userId={}, eventId={}", userId, eventId);
        EventFullDto eventFullDto = eventService.getEventFullDescription(userId, eventId);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
    }

    // Редактирование события созданного пользователем
    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventByCreator (@PathVariable Long userId,
                                               @PathVariable Long eventId,
                                               @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Запрос на редактирование события пользователем, userId={}, eventId={}", userId, eventId);
        EventFullDto eventFullDto = eventService.updateEventByCreator(userId, eventId, updateEventUserRequest);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
    }
}
