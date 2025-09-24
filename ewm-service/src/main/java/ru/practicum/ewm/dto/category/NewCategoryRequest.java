package ru.practicum.ewm.dto.category;

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
public class NewCategoryRequest {
    @Size(min = 1, max = 50, message = "Поле name должно быть размером от 1 до 50 символов")
    @NotBlank(message = "Поле name должно быть заполнено")
    String name;
}
