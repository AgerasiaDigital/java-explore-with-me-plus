package ru.practicum.ewm.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.model.event.Event;

import java.util.Collection;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

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
