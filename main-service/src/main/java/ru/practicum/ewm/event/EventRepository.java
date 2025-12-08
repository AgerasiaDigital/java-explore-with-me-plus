package ru.practicum.ewm.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.model.event.Event;

import java.util.Collection;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Collection<Event> findAllByInitiatorId(Long id);

    boolean existsByCategoryId(Long categoryId);

}
