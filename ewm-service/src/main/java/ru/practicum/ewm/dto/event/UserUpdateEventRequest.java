package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.model.event.EventUserUpdateState;
import ru.practicum.ewm.model.Location;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateEventRequest {
    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    String annotation;

    Long category;

    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
    String description;

    String eventDate;

    Location location;

    Boolean paid;

    @Min(value = 0, message = "Количество участников не может быть отрицательным")
    Integer participantLimit;

    Boolean requestModeration;

    EventUserUpdateState stateAction;

    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    String title;
}
