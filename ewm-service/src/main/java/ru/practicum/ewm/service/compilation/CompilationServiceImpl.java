package ru.practicum.ewm.service.compilation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationRequest;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.compilation.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.repository.CompilationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    final CompilationRepository compilationRepository;
    final CompilationMapper compilationMapper;

    @Override
    public CompilationDto saveCompilation(NewCompilationRequest request) {
        Compilation compilation = compilationMapper.toCompilation(request);

        compilation = compilationRepository.save(compilation);

        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest request, Long id) {
        Compilation compilation = getCompilationOrThrow(id);

        compilation = compilationMapper.updateCompilationFields(compilation, request);

        compilation = compilationRepository.save(compilation);

        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    public CompilationDto getCompilationById(Long id) {
        return compilationMapper.toCompilationDto(getCompilationOrThrow(id));
    }

    @Override
    public void removeCompilation(Long id) {
        Compilation compilation = getCompilationOrThrow(id);

        if (!compilation.getEvents().isEmpty()) {
            throw new ConflictException(String.format("Нельзя удалить подборку с ID - %d, в ней есть события", id));
        }

        compilationRepository.delete(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);

        if (pinned == null) {
            return compilationRepository.findAll(pageable).stream()
                    .map(compilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }

        List<Compilation> compilations = compilationRepository.findByPinned(pinned, pageable);

        return compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    private Compilation getCompilationOrThrow(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Подборка с ID - %d не найдена", id)));
    }
}
