package ru.practicum.ewm.dto.event;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageRequestDto {
    private Integer from = 0;
    private Integer size = 10;
    private EventSort sort; // необязательно

    public Pageable toPageable() {
        int page = from == null ? 0 : from / size;
        int s = size == null ? 10 : size;

        // сортировка по дате — действительная
        if (sort == EventSort.EVENT_DATE) {
            return PageRequest.of(page, s, Sort.by("eventDate").ascending());
        }

        // сортировка по views — только после маппинга → в БД сортировки нет
        return PageRequest.of(page, s, Sort.unsorted());
    }
}
