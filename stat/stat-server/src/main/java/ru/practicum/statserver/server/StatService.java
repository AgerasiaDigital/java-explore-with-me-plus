package ru.practicum.statserver.server;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collection;

public interface StatService {

    void create(EndpointHitDto endpointHitDto);

    Collection<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end,
                                     Collection<String> uris, Boolean unique);
}
