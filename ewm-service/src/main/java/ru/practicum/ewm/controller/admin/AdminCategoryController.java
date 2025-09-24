package ru.practicum.ewm.controller.admin;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryRequest;
import ru.practicum.ewm.service.category.CategoryServiceImpl;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    final CategoryServiceImpl categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid NewCategoryRequest request) {
        log.info("Запрос на добавление категории - {}", request);
        return categoryService.saveCategory(request);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(@RequestBody @Valid NewCategoryRequest request, @PathVariable Long id) {
        log.info("Запрос на обновление категории с ID - {}", id);
        return categoryService.updateCategory(request, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id) {
        log.info("Запрос на удаление категории с ID - {}", id);
        categoryService.removeCategory(id);
    }
}
