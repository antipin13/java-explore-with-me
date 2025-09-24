package ru.practicum.ewm.service.category;

import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryRequest;

import java.util.List;

public interface CategoryService {
    CategoryDto saveCategory(NewCategoryRequest request);

    CategoryDto updateCategory(NewCategoryRequest request, Long id);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto findById(Long id);

    void removeCategory(Long id);
}
