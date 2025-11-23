package ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ewm.dto.StatsParamDto;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StatClientImpl implements StatClient {
    private final RestClient restClient;
    private final String statUrl;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatClientImpl(@Value("http://localhost:9090") String statUrl) {
        this.statUrl = statUrl;
        restClient = RestClient.builder().baseUrl(statUrl).build();
    }

    // TODO: обработка ошибок
    @Override
    public void hit(EndpointHitDto endpointHitDto) {
        restClient.post()
                .uri(URI.create(statUrl + "/hit"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitDto)
                .retrieve()
                .toBodilessEntity();

        log.debug("hit: {}", endpointHitDto);
    }

    // TODO: обработка ошибок
    @Override
    public List<ViewStatsDto> getStats(StatsParamDto statsParamDto) {
        ResponseEntity<List<ViewStatsDto>> response = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(statUrl + "/stats")
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
                .body(new ParameterizedTypeReference<>() {});

        List<ViewStatsDto> stats;

        if (response == null) {
            log.warn("no stats for: {}", statsParamDto);
            stats = Collections.emptyList();
        } else {
            stats = response.getBody();
        }

        return stats;
    }
}
