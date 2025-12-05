package ru.practicum.ewm.dto.event;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PublicEventSearchRequest {

    private String text;
    private List<Long> categories;
    private Boolean paid;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    private String sort;
    private Boolean onlyAvailable;

    private Integer from = 0;
    private Integer page = 0;
    private Integer size = 10;

    public EventSort toSort() {
        if (sort == null || sort.isBlank()) {
            return EventSort.EVENT_DATE; // сортировка по умолчанию
        }
        try {
            return EventSort.valueOf(sort.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EventSort.EVENT_DATE; // fallback для неверных значений
        }
    }

    public Pageable toPageable() {
        int page = from / size;

        Sort s = sort == null ?
                Sort.unsorted() :
                Sort.by(sort).ascending();

        return PageRequest.of(page, size, s);
    }

}
