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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
@Service
public class EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final StatClient statClient;
    private final CategoryRepository categoryRepository;

    //TODO убрать костыль указания временного диапазона
    private Long getViews(Long eventId) {
        StatsParamDto statsParamDto = new StatsParamDto();
        statsParamDto.setStart(LocalDateTime.now().minusYears(10));
        statsParamDto.setEnd(LocalDateTime.now().plusYears(10));
        statsParamDto.setUris(List.of("/events/" + eventId));
        List<ViewStatsDto> viewStatsDtoList = statClient.getStats(statsParamDto);
        Long hits = 0L;
        if (!viewStatsDtoList.isEmpty()) {
            hits = viewStatsDtoList.get(0).getHits();
            log.debug("Hits получены: {}", hits);
        } else {
            log.debug("Статистика пустая, hits = 0");
        }
        return hits;
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
        return eventMapper.toFullDto(savedEvent, getRequests(savedEvent.getId()), getViews(savedEvent.getId()));
    }

    public Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter eventInitiatorIdFilter,
                                                      Pageable pageable) {
        // User user = userRepository.findById(userId)
        //         .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));
        Specification<Event> spec = EventSpecification.withInitiatorId(eventInitiatorIdFilter);
        Page<Event> events = eventRepository.findAll(spec, pageable);

        //  Map<Long, Long> requests = requestService.getRequests(events);
        //  Map<Long, Long> views = statClient.getStats(new StatsParamDto());

//        Collection<Event> events = eventRepository.findAllByInitiatorId(userId);
        List<EventShortDto> eventShortDtoList = new ArrayList<>();
        for (Event e : events) {
            Long request = 10L;
            Long views = 20L;
            eventShortDtoList.add(eventMapper.toShortDto(e, request, views));
        }
        return eventShortDtoList;
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
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(eventId));
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
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(eventId));
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
        eventRepository.save(event);
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(eventId));
    }

    public Page<EventFullDto> adminSearchEvents(EventAdminFilter eventAdminFilter, Pageable pageable) {
        Specification<Event> spec = EventSpecification.withAdminFilter(eventAdminFilter);
        Page<Event> events = eventRepository.findAll(spec, pageable);

        // Map<Long, Long> requests = requestService.getRequests(events);
        // Map<Long, Long> views = statsService.getViews(events);

        return events.map(event ->
                eventMapper.toFullDto(event, 10L, 20L)
        );
    }


    public Page<EventFullDto> publicSearchEvents(EventPublicFilter eventPublicFilter, Pageable pageable) {
        Specification<Event> spec = EventSpecification.withPublicFilter(eventPublicFilter);
        Page<Event> events = eventRepository.findAll(spec, pageable);

        // Map<Long, Long> requests = requestService.getRequests(events);
        // Map<Long, Long> views = statsService.getViews(events);

        return events.map(event ->
                eventMapper.toFullDto(event, 10L, 20L)
        );
    }

    public EventFullDto getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id=%s не найдено", eventId)));
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(eventId));
    }
}