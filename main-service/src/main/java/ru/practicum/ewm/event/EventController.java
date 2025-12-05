package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventRequest;

import java.util.Collection;
import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController


public class EventController {
    private final EventService eventService;

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
    // TODO Для лучшей читаемости можно сделать несколько контроллеров событий (по уровню доступа - admin, public и private)
    //  и перенести туда обработку соответствующих эндпоинтов
    // TODO Лучше всю портянку параметров вынести в класс, например, EventParam, и в сервис уже передавать объект этого класса

    @GetMapping("/admin/events")
    public List<EventFullDto> get(@RequestParam(required = false) List<Long> users,
                                  @RequestParam(required = false) List<String> states,
                                  @RequestParam(required = false) List<Long> categories,
                                  @RequestParam(required = false) String rangeStart,
                                  @RequestParam(required = false) String rangeEnd,
                                  @RequestParam(required = false, defaultValue = "0") Integer from,
                                  @RequestParam(required = false, defaultValue = "10") Integer size) {

        log.debug("Запрос событий админом с параметрами: users: {}, states: {}, categories: {}, rangeStart: {}," +
                "rangeEnd: {}, from: {}, size: {}", users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getByAdmin(users,
                states,
                categories, rangeStart,
                rangeEnd,
                from,
                size);
    }

    // Публичный поиск событий
//    @GetMapping("/events")
//    public Page<EventFullDto> publicSearchEvents(@Validated PublicEventSearchRequest request) {
//        log.debug("Публичный запрос событий с параметрами: {}", request.toString());
//        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
//        return eventService.publicSearchEvents(request, pageable);
//    }

}
