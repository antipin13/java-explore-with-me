package ru.practicum.ewm.mapper.category;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryRequest;
import ru.practicum.ewm.model.Category;

@Component
public class CategoryMapper {
    public Category toCategory(NewCategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .build();
    }

    public CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public Category updateCategoryFields(Category category, NewCategoryRequest request) {
        category.setName(request.getName());

        return category;
    }
}
