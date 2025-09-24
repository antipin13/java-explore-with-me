package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.model.Location;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventRequest {
    @NotNull(message = "Поле annotation должно быть заполнено")
    @NotBlank(message = "Поле annotation должно быть заполнено")
    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    String annotation;

    @NotNull(message = "Поле category должно быть заполнено")
    Long category;

    @NotNull(message = "Поле description должно быть заполнено")
    @NotBlank(message = "Поле description должно быть заполнено")
    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
    String description;

    @NotNull(message = "Поле eventDate должно быть заполнено")
    String eventDate;

    @NotNull(message = "Поле location должно быть заполнено")
    Location location;

    Boolean paid = false;

    @Min(value = 0, message = "Количество участников не может быть отрицательным")
    Integer participantLimit = 0;

    Boolean requestModeration = true;

    @NotNull(message = "Поле title должно быть заполнено")
    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    String title;
}
