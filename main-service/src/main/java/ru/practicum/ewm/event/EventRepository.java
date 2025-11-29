package ru.practicum.ewm.event;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.event.Event;

import java.util.Collection;

public interface EventRepository extends JpaRepository<Event, Long> {

    Collection<Event> findAllByInitiatorId(Long id);

}
