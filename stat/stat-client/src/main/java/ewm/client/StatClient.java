package ewm.client;

import ewm.dto.StatsParamDto;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

public interface StatClient {

    void hit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(StatsParamDto statsParamDto);
}
