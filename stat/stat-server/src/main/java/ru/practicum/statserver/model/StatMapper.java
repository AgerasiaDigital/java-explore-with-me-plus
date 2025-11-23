package ru.practicum.statserver.model;

import lombok.experimental.UtilityClass;
import ru.practicum.statserver.statdto.HitDto;

@UtilityClass
public class StatMapper {
    public static StatModel createStatModel(HitDto hitDto) {
        return new StatModel(
                null,
                hitDto.getApp(),
                hitDto.getUri(),
                hitDto.getIp(),
                hitDto.getTimestamp()
        );
    }
}