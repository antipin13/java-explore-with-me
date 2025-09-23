package ru.practicum.ewm.service.request;

import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.RequestDto;

import java.util.List;

public interface RequestService {
    RequestDto saveRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getRequestsByRequesterId(Long requestId);

    List<RequestDto> getRequestsByEventId(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
