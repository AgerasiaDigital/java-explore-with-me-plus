package ru.practicum.statserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.statserver.model.StatModel;
import ru.practicum.statserver.statdto.StatDto;

import java.time.LocalDateTime;
import java.util.Collection;

public interface StatRepository extends JpaRepository<StatModel, Long> {

    @Query("""
            SELECT new ru.practicum.statserver.statdto.StatDto(
                s.app,
                s.uri,
               CASE WHEN :unique = TRUE
                    THEN COUNT(DISTINCT s.ip)
                    ELSE COUNT(s.ip)
               END
            )
            FROM StatModel s
            WHERE s.timestamp BETWEEN :start AND :end
            GROUP BY s.app, s.uri
            ORDER BY 3 DESC
            """)
    Collection<StatDto> getStatWithoutUris(LocalDateTime start, LocalDateTime end, Boolean unique);

    @Query("""
            SELECT new ru.practicum.statserver.statdto.StatDto(
                s.app,
                s.uri,
               CASE WHEN :unique = TRUE
                    THEN COUNT(DISTINCT s.ip)
                    ELSE COUNT(s.ip)
               END
            )
            FROM StatModel s
            WHERE s.timestamp BETWEEN :start AND :end
              AND (:uris IS NULL OR s.uri IN :uris)
            GROUP BY s.app, s.uri
            ORDER BY 3 DESC
            """)
    Collection<StatDto> getStat(LocalDateTime start, LocalDateTime end,
                                Collection<String> uris, Boolean unique);
}