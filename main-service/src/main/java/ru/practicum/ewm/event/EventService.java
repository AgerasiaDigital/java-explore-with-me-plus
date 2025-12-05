package ru.practicum.ewm.event;

import com.querydsl.core.BooleanBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.dto.StatsParamDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.AccessViolationException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.event.Location;
import ru.practicum.ewm.model.event.QEvent;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    //TODO после заливки пр по категориям заменить NoSuchElementException тут и далее на кастомный NotFoundException
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Пользователь с id=%s не найден", userId)));
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть минимум через 2 часа");
        }
        Event savedEvent = eventRepository.save(eventMapper.toEvent(newEventDto, user));
        return eventMapper.toFullDto(savedEvent, getRequests(savedEvent.getId()), getViews(savedEvent.getId()));
    }

    public Collection<EventShortDto> getEvent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Пользователь с id=%s не найден", userId)));
        Collection<Event> events = eventRepository.findAllByInitiatorId(userId);
        List<EventShortDto> eventFullDtoList = new ArrayList<>();
        for (Event e : events) {
            Long request = getRequests(e.getId());
            Long views = getViews(e.getId());
            eventFullDtoList.add(eventMapper.toShortDto(e, request, views));
        }
        return eventFullDtoList;
    }

    public EventFullDto getEventFullDescription(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Событие с id=%s не найдено", eventId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Пользователь с id=%s не найден", userId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new AccessViolationException(String.format("Доступ ограничен! Пользователь userId=%s не является создателем события " +
                    "eventId=%s", userId, eventId));
        }
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(eventId));
    }


    //TODO вынести 2 часа - в настройки приложения
    @Transactional
    public EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Событие с id=%s не найдено", eventId)));
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ValidationException("Можно редактировать события только в состоянии Ожидания модерации и Отмены");
        }
        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Разрешается редактировать события не позже, чем за 2 час до начала");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Пользователь с id=%s не найден", userId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new AccessViolationException(String.format("Доступ ограничен! Пользователь userId=%s не является создателем события " +
                    "eventId=%s", userId, eventId));
        }

        // Проверка перехода статуса ---
        EventState newState =
                StateTransitionValidator.changeState(event.getState(), updateEventUserRequest.getStateAction(), false);

        Category category = null;
        if (updateEventUserRequest.hasCategory()) {
            category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NoSuchElementException("Категория не найдена"));
        }

        Location newLocation = null;
        if (updateEventUserRequest.hasLocationDto()) {
            newLocation = eventMapper.toLocation(updateEventUserRequest.getLocationDto());
        }

        updateEventUserRequest.applyTo(event, category, newLocation, newState);
        eventRepository.save(event);
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(eventId));
    }

    //TODO вынести 1час - в настройки приложения
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Событие с id=%s не найдено", eventId)));
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ValidationException("Можно редактировать события только в состоянии Ожидания модерации и Отмены");
        }
        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Разрешается редактировать события не позже, чем за 1 час до начала");
        }

        EventState newState =
                StateTransitionValidator.changeState(event.getState(), updateEventAdminRequest.getStateAction(), true);

        Category category = null;
        if (updateEventAdminRequest.hasCategory()) {
            category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NoSuchElementException("Категория не найдена"));
        }

        Location newLocation = null;
        if (updateEventAdminRequest.hasLocationDto()) {
            newLocation = eventMapper.toLocation(updateEventAdminRequest.getLocationDto());
        }

        updateEventAdminRequest.applyTo(event, category, newLocation, newState);
        eventRepository.save(event);
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(eventId));
    }


    public List<EventFullDto> getByAdmin(List<Long> users, // TODO: в param-класс
                                         List<String> states,
                                         List<Long> categories,
                                         String rangeStart,
                                         String rangeEnd,
                                         Integer from,
                                         Integer size) {
        QEvent qEvent = QEvent.event;
        BooleanBuilder builder = new BooleanBuilder();

        if (users != null && !users.isEmpty()) {
            builder.and(qEvent.initiator.id.in(users));
        }

        if (states != null && !states.isEmpty()) {
            List<EventState> stateEnums = states.stream()
                    .map(stateString -> {
                        Optional<EventState> eventState = EventState.from(stateString);

                        if (eventState.isEmpty()) {
                            log.warn("incorrect state - {}", stateString);
                            throw new ValidationException("incorrect state");
                        }

                        return eventState.get();
                    })
                    .toList();
            builder.and(qEvent.state.in(stateEnums));
        }

        if (categories != null && !categories.isEmpty()) {
            builder.and(qEvent.category.id.in(categories));
        }

        if (rangeStart != null && !rangeStart.trim().isEmpty()) {
            try {
                LocalDateTime start = LocalDateTime.parse(rangeStart, FORMATTER);
                builder.and(qEvent.eventDate.goe(start));
            } catch (Exception e) {
                log.warn("invalid rangeStart format - {}", rangeStart);
                throw new ValidationException("invalid rangeStart format");
            }
        }

        if (rangeEnd != null && !rangeEnd.trim().isEmpty()) {
            try {
                LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
                builder.and(qEvent.eventDate.loe(end));
            } catch (Exception e) {
                log.warn("invalid rangeEnd format - {}", rangeEnd);
                throw new IllegalArgumentException("invalid rangeEnd format");
            }
        }

        if (builder.getValue() == null) {
            return Collections.emptyList();
        }

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        Page<Event> events = eventRepository.findAll(builder.getValue(), pageable);

        // requests and views
        return events.stream()
                .map(event -> eventMapper.toFullDto(
                        event,
                        getRequests(event.getId()),
                        getViews(event.getId())
                ))
                .toList();
    }
}