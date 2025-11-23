package ru.practicum.statserver.statdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class StatDto {
    private String app;
    private String uri;
    private Long hits;
}
