package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.model.event.AdminEventSearchCriteria;
import ru.practicum.ewm.model.event.PublicEventSearchCriteria;

import java.util.List;

public interface EventService {
    EventDto saveEvent(NewEventRequest request, Long userId);

    EventDto updateAdminEvent(AdminUpdateEventRequest request, Long eventId);

    EventDto updateUserEvent(UserUpdateEventRequest request, Long initiatorId, Long eventId);

    List<EventShortDto> getEventsByInitiatorId(Long initiatorId, int from, int size);

    EventDto getEventByIdAndInitiatorId(Long initiatorId, Long eventId);

    List<EventDto> getEventsByCriteria(AdminEventSearchCriteria criteria);

    List<EventShortDto> getEventsByCriteria(PublicEventSearchCriteria criteria);

    EventDto getEventById(Long id, HttpServletRequest request);
}
