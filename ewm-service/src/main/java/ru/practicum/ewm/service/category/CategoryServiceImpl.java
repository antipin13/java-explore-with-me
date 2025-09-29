package ru.practicum.ewm.service.category;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.category.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    final CategoryRepository categoryRepository;
    final CategoryMapper categoryMapper;
    final EventRepository eventRepository;

    @Override
    public CategoryDto saveCategory(NewCategoryRequest request) {
        Category category = categoryMapper.toCategory(request);

        if (categoryRepository.existsByName(category.getName())) {
            throw new ConflictException(String.format("Категория с именем - %s уже существует", category.getName()));
        }

        category = categoryRepository.save(category);

        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategory(NewCategoryRequest request, Long id) {
        Category existingCategory = getCategoryOrThrow(id);

        existingCategory = categoryMapper.updateCategoryFields(existingCategory, request);

        existingCategory = categoryRepository.save(existingCategory);

        return categoryMapper.toCategoryDto(existingCategory);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from, size);

        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findById(Long id) {
        Category categoryById = getCategoryOrThrow(id);

        return categoryMapper.toCategoryDto(categoryById);
    }

    @Override
    public void removeCategory(Long id) {
        Category category = getCategoryOrThrow(id);

        if (eventRepository.existsByCategoryId(id)) {
            throw new ConflictException(String.format("Нельзя удалить категорию с ID - %d, когда у нее есть связанные события", id));
        }

        categoryRepository.delete(category);
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Категория с ID - %d не найдена", categoryId)));
    }
}
