package ru.practicum.ewm.controller.privates;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.service.request.RequestServiceImpl;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/users/{id}/requests")
public class PrivateRequestController {
    final RequestServiceImpl requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto create(@PathVariable(name = "id") Long userId, @RequestParam(required = true) Long eventId) {
        log.info("Запрос на добавление запроса на участие c ID - {} от пользователся с ID - {}", eventId, userId);
        return requestService.saveRequest(userId, eventId);
    }

    @PatchMapping("/{request-id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestDto cancelRequest(@PathVariable(name = "id") Long userId,
                                    @PathVariable(name = "request-id") Long requestId) {
        log.info("Отмена запроса на участие c ID - {} от пользователся с ID - {}", requestId, userId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> getRequestsByUserId(@PathVariable Long id) {
        log.info("Запрос на получение списка запросов пользователя c ID - {}", id);
        return requestService.getRequestsByRequesterId(id);
    }
}
