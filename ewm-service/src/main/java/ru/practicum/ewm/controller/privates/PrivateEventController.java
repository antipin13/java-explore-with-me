package ru.practicum.ewm.controller.privates;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.service.event.EventServiceImpl;
import ru.practicum.ewm.service.request.RequestServiceImpl;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/users/{id}/events")
public class PrivateEventController {
    final EventServiceImpl eventService;
    final RequestServiceImpl requestService;
    final String likesUrl = "/{event-id}/likes/{liked-user-id}";
    final String dislikesUrl = "/{event-id}/dislikes/{disliked-user-id}";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto create(@RequestBody @Valid NewEventRequest request, @PathVariable(name = "id") Long userId) {
        log.info("Запрос на добавление события - {}", request);
        return eventService.saveEvent(request, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByInitiatorId(@PathVariable(name = "id") Long initiatorId,
                                                      @RequestParam(defaultValue = "0") int from,
                                                      @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение событий пользователя с ID - {}, начиная с - {} и количество - {}",
                initiatorId, from, size);
        return eventService.getEventsByInitiatorId(initiatorId, from, size);
    }

    @GetMapping("/{event-id}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEventByInitiatorIdAndEventId(@PathVariable(name = "id") Long initiatorId,
                                                    @PathVariable(name = "event-id") Long eventId) {
        log.info("Запрос на получение события с ID - {} пользователя с ID - {}", eventId, initiatorId);
        return eventService.getEventByIdAndInitiatorId(initiatorId, eventId);
    }

    @PatchMapping("/{event-id}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto update(@RequestBody @Valid UserUpdateEventRequest request,
                           @PathVariable Long id,
                           @PathVariable(name = "event-id") Long eventId) {
        log.info("Запрос на обновление категории с ID - {}", id);
        return eventService.updateUserEvent(request, id, eventId);
    }

    @GetMapping("/{event-id}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> getRequestsByEventId(@PathVariable(name = "id") Long initiatorId,
                                                 @PathVariable(name = "event-id") Long eventId) {
        log.info("Получение списка запросов на участие в событии с ID - {} пользователя с ID - {}", eventId, initiatorId);
        return requestService.getRequestsByEventId(initiatorId, eventId);
    }

    @PatchMapping("/{event-id}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestsByEventId(@PathVariable(name = "id") Long initiatorId,
                                                               @PathVariable(name = "event-id") Long eventId,
                                                               @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Обновление статусов у заявок - {}", request);
        return requestService.updateStatusRequest(initiatorId, eventId, request);
    }

    @PutMapping(value = likesUrl)
    @ResponseStatus(HttpStatus.OK)
    public EventDtoWithRating addLike(@PathVariable(name = "id") Long initiatorId,
                                      @PathVariable(name = "event-id") Long eventId,
                                      @PathVariable(name = "liked-user-id") Long likedUser) {
        log.info("Добавление лайка на мероприятие с ID - {} от пользователя с ID - {}", eventId, likedUser);
        return eventService.addLike(initiatorId, eventId, likedUser);
    }

    @PutMapping(value = dislikesUrl)
    @ResponseStatus(HttpStatus.OK)
    public EventDtoWithRating addDislike(@PathVariable(name = "id") Long initiatorId,
                                      @PathVariable(name = "event-id") Long eventId,
                                      @PathVariable(name = "disliked-user-id") Long dislikedUser) {
        log.info("Добавление дизлайка на мероприятие с ID - {} от пользователя с ID - {}", eventId, dislikedUser);
        return eventService.addDislike(initiatorId, eventId, dislikedUser);
    }

    @DeleteMapping(value = likesUrl)
    @ResponseStatus(HttpStatus.OK)
    public EventDtoWithRating removeLike(@PathVariable(name = "id") Long initiatorId,
                                      @PathVariable(name = "event-id") Long eventId,
                                      @PathVariable(name = "liked-user-id") Long likedUser) {
        log.info("Удаление лайка на мероприятии с ID - {} от пользователя с ID - {}", eventId, likedUser);
        return eventService.removeLike(initiatorId, eventId, likedUser);
    }

    @DeleteMapping(value = dislikesUrl)
    @ResponseStatus(HttpStatus.OK)
    public EventDtoWithRating removeDislike(@PathVariable(name = "id") Long initiatorId,
                                         @PathVariable(name = "event-id") Long eventId,
                                         @PathVariable(name = "disliked-user-id") Long dislikedUser) {
        log.info("Удаление дизлайка на мероприятии с ID - {} от пользователя с ID - {}", eventId, dislikedUser);
        return eventService.removeDislike(initiatorId, eventId, dislikedUser);
    }
}
