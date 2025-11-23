package ru.practicum.statserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statserver.exception.IllegalArgumentException;
import ru.practicum.statserver.server.StatService;
import ru.practicum.statserver.statdto.HitDto;
import ru.practicum.statserver.statdto.StatDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class StatController {
    private final StatService statService;
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody HitDto hitDto) {
        statService.create(hitDto);
    }

    @GetMapping("/stats")
    public Collection<StatDto> getStat(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
                                       @RequestParam(name = "start")
                                       LocalDateTime start,
                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
                                       @RequestParam(name = "end")
                                       LocalDateTime end,
                                       @RequestParam(name = "uris", required = false)
                                       List<String> uris,
                                       @RequestParam(name= "unique", defaultValue = "false", required = false)
                                       Boolean unique) {
        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}",
                start,end,uris,unique);
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Start не может быть позже End.");
        }
        return statService.getStat(start, end, uris, unique);
    }
}


