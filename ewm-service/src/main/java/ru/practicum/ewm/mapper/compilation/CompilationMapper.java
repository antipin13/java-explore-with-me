package ru.practicum.ewm.mapper.compilation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationRequest;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.mapper.event.EventMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.service.event.EventServiceImpl;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationMapper {
    final EventServiceImpl eventService;
    final EventMapper eventMapper;

    public Compilation toCompilation(NewCompilationRequest request) {
        Set<Event> events = request.getEvents().stream()
                .map(eventId -> eventService.getEventOrThrow(eventId))
                .collect(Collectors.toSet());

        return Compilation.builder()
                .events(events)
                .pinned(request.getPinned())
                .title(request.getTitle())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents().stream()
                        .map(eventMapper::toEventShortDto).collect(Collectors.toSet()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public Compilation updateCompilationFields(Compilation compilation, UpdateCompilationRequest request) {
        if (request.getEvents() != null) {
            Set<Event> events = request.getEvents().stream()
                    .map(eventId -> eventService.getEventOrThrow(eventId))
                    .collect(Collectors.toSet());

            compilation.setEvents(events);
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        return compilation;
    }
}
