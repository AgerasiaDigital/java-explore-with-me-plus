package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsParamDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StatClientImpl implements StatClient {
    private final RestClient restClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatClientImpl(String statUrl) {
        restClient = RestClient.builder()
                .baseUrl(statUrl)
                .build();
    }

    // TODO: обработка ошибок
    @Override
    public void hit(EndpointHitDto endpointHitDto) {
        restClient.post()
                .uri(URI.create("/hit"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitDto)
                .retrieve()
                .toBodilessEntity();

        log.debug("hit: {}", endpointHitDto);
    }

    // TODO: обработка ошибок
    @Override
    public List<ViewStatsDto> getStats(StatsParamDto statsParamDto) {
        log.info("Запрос стастистики для uri: {}", statsParamDto.getUris());
        log.debug("statsParamDto: {}", statsParamDto);
        List<ViewStatsDto> stats = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/stats")
                            .queryParam("start", statsParamDto.getStart().format(formatter))
                            .queryParam("end", statsParamDto.getEnd().format(formatter));

                    if (statsParamDto.getUris() != null && !statsParamDto.getUris().isEmpty()) {
                        uriBuilder.queryParam("uris", statsParamDto.getUris());
                    }

                    if (statsParamDto.getIsUnique() != null) {
                        uriBuilder.queryParam("unique", statsParamDto.getIsUnique());
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (stats == null) {
            log.warn("Отсутствует статистика посещений для: {}", statsParamDto);
            return Collections.emptyList();
        }
        log.debug("Выгружена статистика: {}", stats);
        return stats;
    }
}
