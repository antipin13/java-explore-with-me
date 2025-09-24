package ru.practicum.ewm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewUserRequest {
    @NotBlank(message = "Поле name должно быть заполнено")
    @Size(min = 2, max = 250, message = "Поле name должно быть размером от 1 до 50 символов")
    String name;

    @Email(message = "Неккоректный email")
    @NotBlank(message = "Поле email должно быть заполнено")
    @Size(min = 6, max = 254, message = "Поле name должно быть размером от 1 до 50 символов")
    String email;
}
