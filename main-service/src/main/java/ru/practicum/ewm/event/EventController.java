package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import java.util.Collection;

@Slf4j
@AllArgsConstructor
@RestController
//@RequestMapping(path = "/bookings")
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

    @GetMapping("/users/{userId}/events")
    public Collection<EventShortDto> getEventsOfUser(@PathVariable Long userId) {
        log.info("Запрос событий пользователя, userId={}", userId);
        Collection<EventShortDto> events = eventService.getEvent(userId);
        log.info("Найдено событий: {}", events.size());
        events.forEach(ev -> log.debug("EVENT: {}", ev));
        return events;
    }
}
