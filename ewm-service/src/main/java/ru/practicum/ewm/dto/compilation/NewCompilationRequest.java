package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCompilationRequest {
    List<Long> events = new ArrayList<>();
    Boolean pinned = false;

    @NotNull(message = "Поле title должно быть заполнено")
    @NotBlank(message = "Поле title должно быть заполнено")
    @Size(min = 1, max = 50, message = "Название подборки должно быть от 1 до 50 символов")
    String title;
}
