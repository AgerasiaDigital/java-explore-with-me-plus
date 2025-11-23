package ru.practicum.statserver.server;

import lombok.AllArgsConstructor;
import ru.practicum.statserver.model.StatModel;
import ru.practicum.statserver.model.StatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statserver.repository.StatRepository;
import ru.practicum.statserver.statdto.HitDto;
import ru.practicum.statserver.statdto.StatDto;

import java.time.LocalDateTime;
import java.util.Collection;

@AllArgsConstructor
@Service
public class StatService {
    private StatRepository statRepository;

    @Transactional
    public void create(HitDto hitDto) {
        StatModel statEntity = statRepository.save(StatMapper.createStatModel(hitDto));
    }

    public Collection<StatDto> getStat(LocalDateTime start, LocalDateTime end,
                                 Collection<String> uris, Boolean unique) {
        if (uris == null || uris.isEmpty()) {
            // Без фильтра по uris - отдельный запрос
            return statRepository.getStatWithoutUris(start, end, unique);
        }
        return statRepository.getStat(start, end, uris, unique);
    }
}
