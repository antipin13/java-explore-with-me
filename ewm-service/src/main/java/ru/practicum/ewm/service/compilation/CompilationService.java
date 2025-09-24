package ru.practicum.ewm.service.compilation;

import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationRequest;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto saveCompilation(NewCompilationRequest request);

    CompilationDto updateCompilation(UpdateCompilationRequest request, Long id);

    CompilationDto getCompilationById(Long id);

    void removeCompilation(Long id);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);
}
