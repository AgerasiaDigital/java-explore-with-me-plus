package ru.practicum.ewm.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClientImpl;
import ru.practicum.dto.StatsParamDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@AllArgsConstructor
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final StatClientImpl statClient;

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

    @Transactional
    public void create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Пользователь с id=%s не найден", userId)));
        Event newEvent = eventMapper.toEvent(newEventDto, user);
        eventRepository.save(newEvent);
    }

    public Collection<EventShortDto> getEvent(Long userId) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Пользователь с id=%s не найден", userId)));
        Collection<Event> events = eventRepository.findAllByInitiatorId(userId);
        List<EventShortDto> eventFullDtoList = new ArrayList<>();
        for (Event e : events) {
            Long request = 0L;
            Long views = getViews(e.getId());
            eventFullDtoList.add(eventMapper.toShortDto(e, request, views));
        }
        return eventFullDtoList;
    }
}