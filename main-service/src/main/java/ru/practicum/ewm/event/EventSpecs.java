package ru.practicum.ewm.event;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.model.event.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecs {

    public static Specification<Event> withFilter(EventFilter f) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            LocalDateTime now = LocalDateTime.now();

            if (f.getText() != null) {
                Predicate p = cb.or(
                        cb.like(cb.lower(root.get("annotation")), "%" + f.getText().toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("description")), "%" + f.getText().toLowerCase() + "%")
                );
                predicates.add(p);
            }

            if (f.getCategories() != null) {
                predicates.add(root.get("category").get("id").in(f.getCategories()));
            }

            if (f.getState() != null) {
                predicates.add(cb.equal(root.get("state"), f.getState()));
            }

            if (f.getPaid() != null) {
                predicates.add(cb.equal(root.get("paid"), f.getPaid()));
            }

            if (f.getRangeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), f.getRangeStart()));
            }

            if (f.getRangeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), f.getRangeEnd()));
            }

            if (f.getRangeStart() == null && f.getRangeEnd() == null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), now));
            }

            if (f.getOnlyAvailable() != null && f.getOnlyAvailable()) {
                predicates.add(
                        cb.lessThan(
                                root.get("confirmedRequests"),
                                root.get("participantLimit")
                        )
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
