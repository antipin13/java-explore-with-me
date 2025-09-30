package ru.practicum.ewm.service.compilation;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationRequest;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.compilation.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CompilationRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class CompilationServiceImplTest {
    @InjectMocks
    CompilationServiceImpl compilationService;

    @Mock
    CompilationRepository compilationRepository;

    @Mock
    CompilationMapper compilationMapper;

    @Test
    void testSaveCompilation() {
        List<Long> eventIds = List.of(1L, 2L, 3L);
        NewCompilationRequest request = new NewCompilationRequest();
        request.setEvents(eventIds);
        request.setPinned(true);
        request.setTitle("Test Compilation");

        Compilation compilation = new Compilation();
        compilation.setEvents(new HashSet<>());
        compilation.setPinned(true);
        compilation.setTitle("Test Compilation");

        Compilation savedCompilation = new Compilation();
        savedCompilation.setId(1L);
        savedCompilation.setEvents(new HashSet<>());
        savedCompilation.setPinned(true);
        savedCompilation.setTitle("Test Compilation");

        Set<EventShortDto> eventDtos = Set.of(
                new EventShortDto(), new EventShortDto(), new EventShortDto()
        );
        CompilationDto expectedDto = CompilationDto.builder()
                .id(1L)
                .events(eventDtos)
                .pinned(true)
                .title("Test Compilation")
                .build();

        when(compilationMapper.toCompilation(request)).thenReturn(compilation);
        when(compilationRepository.save(compilation)).thenReturn(savedCompilation);
        when(compilationMapper.toCompilationDto(savedCompilation)).thenReturn(expectedDto);

        CompilationDto result = compilationService.saveCompilation(request);

        assertEquals(expectedDto, result);
        assertEquals(1L, result.getId());
        assertEquals("Test Compilation", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(3, result.getEvents().size());

        verify(compilationMapper).toCompilation(request);
        verify(compilationRepository).save(compilation);
        verify(compilationMapper).toCompilationDto(savedCompilation);
    }

    @Test
    void testUpdateCompilation() {
        Long compilationId = 1L;
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setEvents(List.of(1L, 2L));
        request.setPinned(true);
        request.setTitle("Updated Title");

        Compilation existingCompilation = new Compilation();
        existingCompilation.setId(compilationId);
        existingCompilation.setTitle("Original Title");
        existingCompilation.setPinned(false);
        existingCompilation.setEvents(new HashSet<>());

        Compilation updatedCompilation = new Compilation();
        updatedCompilation.setId(compilationId);
        updatedCompilation.setTitle("Updated Title");
        updatedCompilation.setPinned(true);
        updatedCompilation.setEvents(new HashSet<>());

        Set<EventShortDto> eventDtos = Set.of(new EventShortDto(), new EventShortDto());
        CompilationDto expectedDto = CompilationDto.builder()
                .id(compilationId)
                .title("Updated Title")
                .pinned(true)
                .events(eventDtos)
                .build();

        when(compilationRepository.findById(compilationId)).thenReturn(Optional.of(existingCompilation));
        when(compilationMapper.updateCompilationFields(existingCompilation, request)).thenReturn(updatedCompilation);
        when(compilationRepository.save(updatedCompilation)).thenReturn(updatedCompilation);
        when(compilationMapper.toCompilationDto(updatedCompilation)).thenReturn(expectedDto);

        CompilationDto result = compilationService.updateCompilation(request, compilationId);

        assertEquals(expectedDto, result);
        assertEquals(compilationId, result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(2, result.getEvents().size());

        verify(compilationRepository).findById(compilationId);
        verify(compilationMapper).updateCompilationFields(existingCompilation, request);
        verify(compilationRepository).save(updatedCompilation);
        verify(compilationMapper).toCompilationDto(updatedCompilation);
    }

    @Test
    void testUpdateCompilationThrowException() {
        Long compilationId = 999L;
        UpdateCompilationRequest request = new UpdateCompilationRequest();

        when(compilationRepository.findById(compilationId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> compilationService.updateCompilation(request, compilationId));

        verify(compilationRepository).findById(compilationId);
        verify(compilationMapper, never()).updateCompilationFields(any(), any());
        verify(compilationRepository, never()).save(any());
        verify(compilationMapper, never()).toCompilationDto(any());
    }

    @Test
    void testGetCompilationById() {
        Long compilationId = 1L;
        Compilation compilation = new Compilation();
        compilation.setId(compilationId);
        compilation.setTitle("Test Compilation");
        compilation.setPinned(true);
        compilation.setEvents(new HashSet<>());

        CompilationDto expectedDto = CompilationDto.builder()
                .id(compilationId)
                .title("Test Compilation")
                .pinned(true)
                .events(Collections.emptySet())
                .build();

        when(compilationRepository.findById(compilationId)).thenReturn(Optional.of(compilation));
        when(compilationMapper.toCompilationDto(compilation)).thenReturn(expectedDto);

        CompilationDto result = compilationService.getCompilationById(compilationId);

        assertEquals(expectedDto, result);

        verify(compilationRepository).findById(compilationId);
        verify(compilationMapper).toCompilationDto(compilation);
    }

    @Test
    void testRemoveCompilation() {
        Long compilationId = 1L;
        Compilation compilation = new Compilation();
        compilation.setId(compilationId);
        compilation.setTitle("Test Compilation");
        compilation.setEvents(Collections.emptySet());

        when(compilationRepository.findById(compilationId)).thenReturn(Optional.of(compilation));

        compilationService.removeCompilation(compilationId);

        verify(compilationRepository).findById(compilationId);
        verify(compilationRepository).delete(compilation);
    }

    @Test
    void testRemoveCompilationThrowConflictException() {
        Long compilationId = 1L;
        Compilation compilation = new Compilation();
        compilation.setId(compilationId);
        compilation.setTitle("Test Compilation");

        Set<Event> events = new HashSet<>();
        events.add(new Event());
        compilation.setEvents(events);

        when(compilationRepository.findById(compilationId)).thenReturn(Optional.of(compilation));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> compilationService.removeCompilation(compilationId));

        assertEquals("Нельзя удалить подборку с ID - 1, в ней есть события",
                exception.getMessage());

        verify(compilationRepository).findById(compilationId);
        verify(compilationRepository, never()).delete(any());
    }
}