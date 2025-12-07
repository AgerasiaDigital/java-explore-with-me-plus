package ru.practicum.ewm.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.dto.StatsParamDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventRequest;
import ru.practicum.ewm.exception.AccessViolationException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.event.Location;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class EventServiceImpl implements EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final StatClient statClient;
    private final CategoryRepository categoryRepository;

    private Long extractId(String uri) {
        return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
    }
    //TODO убрать костыль указания временного диапазона

    private Map<Long, Long> getViews(List<Event> events) {
        List<String> uriList = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        StatsParamDto statsParamDto = new StatsParamDto();
        statsParamDto.setStart(LocalDateTime.now().minusYears(10));
        statsParamDto.setEnd(LocalDateTime.now().plusYears(10));
        statsParamDto.setUris(uriList);

        List<ViewStatsDto> viewStatsDtoList = statClient.getStats(statsParamDto);
        return viewStatsDtoList.stream()
                .collect(Collectors.toMap(
                        dto -> Long.parseLong(dto.getUri().substring(dto.getUri().lastIndexOf('/') + 1)),
                        ViewStatsDto::getHits
                ));
    }

    private Long getRequests(Long eventId) {
        return 10L;
    }

    //TODO добавить категории
    //TODO добавить обработку валидации
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть минимум через 2 часа");
        }
        Event savedEvent = eventRepository.save(eventMapper.toEvent(newEventDto, user));
        return eventMapper.toFullDto(savedEvent, getRequests(savedEvent.getId()), getViews(List.of(savedEvent)).get(savedEvent.getId()));
    }

    public Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter eventInitiatorIdFilter,
                                                      Pageable pageable) {
        Specification<Event> spec = EventSpecification.withInitiatorId(eventInitiatorIdFilter);
        Page<Event> events = eventRepository.findAll(spec, pageable);

        Map<Long, Long> viewsMap = getViews(events.getContent());

        return events.getContent().stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        10L, // confirmedRequests пример
                        viewsMap.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    public EventFullDto getEventFullDescription(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id=%s не найдено", eventId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new AccessViolationException(String.format("Доступ ограничен! Пользователь userId=%s не является создателем события " +
                    "eventId=%s", userId, eventId));
        }
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(List.of(event)).get(eventId));
    }

    //TODO вынести 2 часа - в настройки приложения
    @Transactional
    public EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id=%s не найдено", eventId)));
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ValidationException("Можно редактировать события только в состоянии Ожидания модерации и Отмены");
        }
        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Разрешается редактировать события не позже, чем за 2 час до начала");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new AccessViolationException(String.format("Доступ ограничен! Пользователь userId=%s не является создателем события " +
                    "eventId=%s", userId, eventId));
        }

        // Проверка перехода статуса ---
        EventState newState =
                StateTransitionValidator.changeState(event.getState(), updateEventRequest.getStateAction(), false);

        Category category = null;
        if (updateEventRequest.hasCategory()) {
            category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        }

        Location newLocation = null;
        if (updateEventRequest.hasLocationDto()) {
            newLocation = eventMapper.toLocation(updateEventRequest.getLocationDto());
        }

        updateEventRequest.applyTo(event, category, newLocation, newState);
        eventRepository.save(event);
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(List.of(event)).get(eventId));
    }

    //TODO вынести 1час - в настройки приложения
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id=%s не найдено", eventId)));
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ValidationException("Можно редактировать события только в состоянии Ожидания модерации и Отмены");
        }
        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Разрешается редактировать события не позже, чем за 1 час до начала");
        }

        EventState newState =
                StateTransitionValidator.changeState(event.getState(), updateEventRequest.getStateAction(), true);

        Category category = null;
        if (updateEventRequest.hasCategory()) {
            category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        }

        Location newLocation = null;
        if (updateEventRequest.hasLocationDto()) {
            newLocation = eventMapper.toLocation(updateEventRequest.getLocationDto());
        }

        updateEventRequest.applyTo(event, category, newLocation, newState);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(List.of(savedEvent)).get(savedEvent.getId()));
    }

    public Page<EventFullDto> adminSearchEvents(EventAdminFilter eventAdminFilter, Pageable pageable) {
        Specification<Event> spec = EventSpecification.withAdminFilter(eventAdminFilter);
        Page<Event> events = eventRepository.findAll(spec, pageable);

        // Map<Long, Long> requests = requestService.getRequests(events);
        Map<Long, Long> viewsMap = getViews(events.getContent());

        return events.map(event ->
                eventMapper.toFullDto(event,
                        10L,
                        viewsMap.getOrDefault(event.getId(), 0L))
        );
    }

    public Page<EventFullDto> publicSearchEvents(EventPublicFilter eventPublicFilter, Pageable pageable) {
        Specification<Event> spec = EventSpecification.withPublicFilter(eventPublicFilter);
        Page<Event> events = eventRepository.findAll(spec, pageable);

        //Map<Long, Long> requests = requestService.getRequests(events);
        Map<Long, Long> viewsMap = getViews(events.getContent());

        return events.map(event ->
                eventMapper.toFullDto(event,
                        10L,
                        viewsMap.getOrDefault(event.getId(), 0L))
        );
    }

    public EventFullDto getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id=%s не найдено", eventId)));
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(List.of(event)).get(eventId));
    }
}