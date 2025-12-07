package ru.practicum.ewm.dto.event;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.ewm.event.OffsetBasedPageRequest;

@Data
public class PageRequestDto {
    private Integer from = 0;
    private Integer size = 10;
    private EventSort sort; // необязательно

    public Pageable toPageable() {
        int offset = (from == null) ? 0 : from;
        int limit = (size == null) ? 10 : size;

        Sort sorting = sort == EventSort.EVENT_DATE
                ? Sort.by("eventDate").ascending()
                : Sort.unsorted();

        return new OffsetBasedPageRequest(offset, limit, sorting);
    }
}
