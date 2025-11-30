package ru.practicum.ewm.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final StatClient statClient;
    private final CategoryRepository categoryRepository;

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

    //Заглушка. Заменить на выборку
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

//    private void applyStateTransition(Event event, StateAction action) {
//        StateTransitionValidator.validate(event, action);
//        event.setState(action.toEventState());
//        if (action == StateAction.PUBLISH_EVENT) {
//            event.setPublishedOn(java.time.LocalDateTime.now());
//        }
//    }

    @Transactional
    public EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Событие с id=%s не найдено", eventId)));
        if (event.getState()!= EventState.PENDING && event.getState()!= EventState.CANCELED) {
            throw new ValidationException("Можно редактировать события только в состоянии Ожидания модерации и Отмены");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Разрешается редактировать события не позже, чем за 2 час до начала");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Пользователь с id=%s не найден", userId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new AccessViolationException(String.format("Доступ ограничен! Пользователь userId=%s не является создателем события " +
                    "eventId=%s", userId, eventId));
        }
        Category category = null;
        if (updateEventUserRequest.hasCategory()) {
            category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NoSuchElementException("Категория не найдена"));
        }
        Location newLocation = null;
        if (updateEventUserRequest.hasLocationDto()) {
            newLocation = eventMapper.toLocation(updateEventUserRequest.getLocationDto());
        }
        updateEventUserRequest.applyTo(event, category, newLocation);
        eventRepository.save(event);
        return eventMapper.toFullDto(event, getRequests(eventId), getViews(eventId));
    }


}