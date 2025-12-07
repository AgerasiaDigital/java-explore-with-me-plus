package ru.practicum.ewm.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.ewm.dto.event.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController


public class EventController {
    private final EventService eventService;
    private final StatClient statClient;

    private void saveHit(HttpServletRequest request) {
        statClient.hit(new EndpointHitDto(
                "ewm-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        ));
    }

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
    public Collection<EventShortDto> getEventsOfUser(@PathVariable Long userId,
                                                     EventInitiatorIdFilter eventInitiatorIdFilter,
                                                     PageRequestDto pageRequestDto) {
        log.info("Запрос событий пользователя, userId={}", userId);
        Collection<EventShortDto> events = eventService.getEventByUserId(eventInitiatorIdFilter,
                pageRequestDto.toPageable());
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
    public EventFullDto updateEventByCreator(@PathVariable Long userId,
                                             @PathVariable Long eventId,
                                             @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Запрос на редактирование события пользователем, userId={}, eventId={}", userId, eventId);
        EventFullDto eventFullDto = eventService.updateEventByCreator(userId, eventId, updateEventRequest);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Запрос на редактирование события админом, eventId={}", eventId);
        EventFullDto eventFullDto = eventService.updateEventByAdmin(eventId, updateEventRequest);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
    }

    // Админский запрос событий
    @GetMapping("/admin/events")
    public List<EventFullDto> getEventsAdmin(EventAdminFilter eventAdminFilter,
                                             PageRequestDto pageRequestDto) {
        log.debug("Админский запрос событий с параметрами: {}", eventAdminFilter);
        Page<EventFullDto> page = eventService.adminSearchEvents(eventAdminFilter, pageRequestDto.toPageable());
        return page.getContent();
    }

    // Публичный поиск событий
    @GetMapping("/events")
    public List<EventFullDto> getEvents(EventPublicFilter eventPublicFilter,
                                        PageRequestDto pageRequestDto,
                                        HttpServletRequest request) {
        log.info("Публичный запрос событий с параметрами: {}", eventPublicFilter);
        log.debug("Параметры запроса: {}", eventPublicFilter);
        log.info("client ip: {}", request.getRemoteAddr());
        saveHit(request);
        Page<EventFullDto> page = eventService.publicSearchEvents(eventPublicFilter, pageRequestDto.toPageable());
        return page.getContent();
    }

    // Публичный запрос подробной информации по событию
    @GetMapping("/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long eventId,
                                 HttpServletRequest request) {
        log.info("Публичный запрос подробной информации по событию с id: {}", eventId);
        log.info("client ip: {}", request.getRemoteAddr());
        saveHit(request);
        return eventService.getEvent(eventId);
    }
}
