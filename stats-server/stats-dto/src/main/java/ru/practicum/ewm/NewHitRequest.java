package ru.practicum.ewm;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewHitRequest {
    @NotBlank(message = "Поле uri должно быть заполнено")
    String app;

    @NotBlank(message = "Поле uri должно быть заполнено")
    String uri;

    @NotBlank(message = "Поле ip должно быть заполнено")
    String ip;

    @NotBlank(message = "Поле timestamp должно быть заполнено")
    String timestamp;
}
