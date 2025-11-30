package ru.practicum.ewm.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;


@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "initiator", source = "user")
    @Mapping(target = "location", source = "newEventDto.location")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event toEvent(NewEventDto newEventDto, User user);

    @Mapping(target = "confirmedRequests", source = "requests")
    @Mapping(target = "views", source = "views")
    EventShortDto toShortDto(Event event, Long requests, Long views);

    @Mapping(target = "confirmedRequests", source = "requests")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "location", source = "event.location")
    EventFullDto toFullDto(Event event, Long requests, Long views);

}

