package ru.practicum.ewm.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    Collection<Event> findAllByInitiatorId(Long id);

//
//    @Query("""
//    SELECT e
//            WHERE (
//                   -- Условие: если диапазон не указан, ищем события позже текущего времени
//                   (:rangeStart IS NULL OR :rangeEnd IS NULL AND e.eventDate > CURRENT_TIMESTAMP())
//               )
//            OR (
//                 -- Стандартный фильтр по диапазону
//                 (:rangeStart IS NOT NULL AND e.eventDate >= :rangeStart)
//                 AND (:rangeEnd IS NOT NULL AND e.eventDate <= :rangeEnd)
//               )
//            AND (
//                (:annotation IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%'))))
//                OR (
//                (:description IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%')))
//            AND (:categories IS NULL OR e.category.id IN (:categories)
//            AND (e.state = :PUBLISHED);
//    """)
//    List<Event> publicSearchEvents(
//            String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
//            String sort, Boolean onlyAvailable, Long from, Long size);
}
