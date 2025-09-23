package ru.practicum.ewm.model.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.exception.NotValidRequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class PublicEventSearchCriteria {
    String text;
    List<Long> categories;
    Boolean paid;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    Boolean onlyAvailable;
    String sort;
    int from;
    int size;

    public static void validateCriteria(PublicEventSearchCriteria criteria) {
        if ("0".equals(criteria.getText())) {
            throw new NotValidRequestParam("Параметр text не может быть равен '0'");
        }
    }
}
