package ru.practicum.ewm.service.category;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.mapper.category.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class CategoryServiceImplTest {
    @InjectMocks
    CategoryServiceImpl categoryService;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    EventRepository eventRepository;

    @Mock
    CategoryMapper categoryMapper;

    NewCategoryRequest newCategoryRequest;
    Category newCategory;
    CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        newCategoryRequest = new NewCategoryRequest("Test name");
        newCategory = new Category();
        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Test name")
                .build();
    }

    @Test
    void testSaveCategory() {
        when(categoryMapper.toCategory(newCategoryRequest)).thenReturn(newCategory);
        when(categoryRepository.save(newCategory)).thenReturn(newCategory);
        when(categoryMapper.toCategoryDto(newCategory)).thenReturn(categoryDto);

        CategoryDto result = categoryService.saveCategory(newCategoryRequest);

        assertEquals(categoryDto, result);
    }

    @Test
    void testSaveCategoryThrowConflictException() {
        newCategory.setName("Test name");

        when(categoryMapper.toCategory(newCategoryRequest)).thenReturn(newCategory);
        when(categoryRepository.existsByName("Test name")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> categoryService.saveCategory(newCategoryRequest));

        assertEquals("Категория с именем - Test name уже существует", exception.getMessage());
    }

    @Test
    void testUpdateCategory() {
        Long categoryId = 1L;
        NewCategoryRequest updateRequest = new NewCategoryRequest("Updated name");
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Original name");

        Category updatedCategory = new Category();
        updatedCategory.setId(categoryId);
        updatedCategory.setName("Updated name");

        CategoryDto expectedDto = CategoryDto.builder()
                .id(categoryId)
                .name("Updated name")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryMapper.updateCategoryFields(existingCategory, updateRequest)).thenReturn(updatedCategory);
        when(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory);
        when(categoryMapper.toCategoryDto(updatedCategory)).thenReturn(expectedDto);

        CategoryDto result = categoryService.updateCategory(updateRequest, categoryId);

        assertEquals(expectedDto, result);
        assertEquals(categoryId, result.getId());
        assertEquals("Updated name", result.getName());
    }

    @Test
    void testFindById() {
        Long categoryId = 1L;
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Test category");

        CategoryDto expectedDto = CategoryDto.builder()
                .id(categoryId)
                .name("Test category")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryMapper.toCategoryDto(existingCategory)).thenReturn(expectedDto);

        CategoryDto result = categoryService.findById(categoryId);

        assertEquals(expectedDto, result);
        assertEquals(categoryId, result.getId());
        assertEquals("Test category", result.getName());
    }

    @Test
    void testRemoveCategory() {
        Long categoryId = 1L;
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Test category");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(eventRepository.existsByCategoryId(categoryId)).thenReturn(false);

        categoryService.removeCategory(categoryId);

        verify(categoryRepository).findById(categoryId);
        verify(eventRepository).existsByCategoryId(categoryId);
        verify(categoryRepository).delete(existingCategory);
    }

    @Test
    void testRemoveCategoryThrowConflictException() {
        Long categoryId = 1L;
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Test category");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(eventRepository.existsByCategoryId(categoryId)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> categoryService.removeCategory(categoryId));

        assertEquals("Нельзя удалить категорию с ID - 1, когда у нее есть связанные события",
                exception.getMessage());

        verify(categoryRepository).findById(categoryId);
        verify(eventRepository).existsByCategoryId(categoryId);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void testGetCategories() {
        int from = 0;
        int size = 10;

        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Category 1");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Category 2");

        List<Category> categories = List.of(category1, category2);

        CategoryDto dto1 = CategoryDto.builder().id(1L).name("Category 1").build();
        CategoryDto dto2 = CategoryDto.builder().id(2L).name("Category 2").build();
        List<CategoryDto> expectedDtos = List.of(dto1, dto2);

        Page<Category> categoryPage = new PageImpl<>(categories);

        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        when(categoryMapper.toCategoryDto(category1)).thenReturn(dto1);
        when(categoryMapper.toCategoryDto(category2)).thenReturn(dto2);

        List<CategoryDto> result = categoryService.getCategories(from, size);

        assertEquals(2, result.size());
        assertEquals(expectedDtos, result);
    }

    @Test
    void testGetCategories_WhenNoCategoriesExist_ShouldReturnEmptyList() {
        int from = 0;
        int size = 10;

        Page<Category> emptyPage = new PageImpl<>(Collections.emptyList());

        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        List<CategoryDto> result = categoryService.getCategories(from, size);

        assertTrue(result.isEmpty());
    }
}