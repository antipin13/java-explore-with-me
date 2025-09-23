package ru.practicum.ewm.controller.admin;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventDto;
import ru.practicum.ewm.dto.event.AdminUpdateEventRequest;
import ru.practicum.ewm.model.event.AdminEventSearchCriteria;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.service.event.EventServiceImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/admin/events")
public class AdminEventController {
    final EventServiceImpl eventService;
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto update(@RequestBody @Valid AdminUpdateEventRequest request, @PathVariable Long id) {
        log.info("Запрос на обновление события с ID - {}", id);
        return eventService.updateAdminEvent(request, id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getEvents(@RequestParam(required = false) List<Long> users,
                                    @RequestParam(required = false) List<EventState> states,
                                    @RequestParam(required = false) List<Long> categories,
                                    @RequestParam(required = false) String rangeStart,
                                    @RequestParam(required = false) String rangeEnd,
                                    @RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, formatter);
        }
        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, formatter);
        }

        AdminEventSearchCriteria criteria = AdminEventSearchCriteria.builder()
                .users(users)
                .categories(categories)
                .states(states)
                .rangeStart(start)
                .rangeEnd(end)
                .from(from)
                .size(size)
                .build();

        log.info("Параметры запроса - {}", criteria.toString());
        return eventService.getEventsByCriteria(criteria);
    }
}
