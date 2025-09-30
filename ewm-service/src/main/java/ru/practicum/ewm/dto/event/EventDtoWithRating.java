package ru.practicum.ewm.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Location;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDtoWithRating {
    Long id;
    String annotation;
    Category category;
    Long confirmedRequests;
    String createdOn;
    String description;
    String eventDate;
    UserShortDto initiator;
    Location location;
    Boolean paid;
    Integer participantLimit;
    String publishedOn;
    Boolean requestModeration;
    String state;
    String title;
    Long views;
    Long eventRating;
}
