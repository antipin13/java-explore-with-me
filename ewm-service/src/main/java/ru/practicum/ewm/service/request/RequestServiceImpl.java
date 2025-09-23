package ru.practicum.ewm.service.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.request.RequestMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.request.RequestStatus;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    final RequestRepository requestRepository;
    final RequestMapper requestMapper;
    final UserRepository userRepository;
    final EventRepository eventRepository;

    @Override
    public RequestDto saveRequest(Long userId, Long eventId) {
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(String.format("Событие с ID - %d не опубликованно", eventId));
        }

        if (userId == event.getInitiator().getId()) {
            throw new ConflictException(String.format("Пользователь с ID - %d не может участвовать в мероприятии с ID - %d, поскольку является инициатором", userId, eventId));
        }

        if ((event.getConfirmedRequests() >= event.getParticipantLimit()) && (event.getParticipantLimit() != 0)) {
            throw new ConflictException(String.format("Достигнут лимит участников на событие с ID - %d", eventId));
        }

        Optional<Request> existingRequest = requestRepository.findByEventIdAndRequesterId(eventId, userId);

        if (existingRequest.isPresent()) {
            throw new ConflictException(
                    String.format("Пользователь с ID - %d уже создал запрос на участие в мероприятии с ID - %d", userId, eventId));
        }

        Request newRequest = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .build();

        if (event.getRequestModeration() == false) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            newRequest.setStatus(RequestStatus.PENDING);
        }

        if (event.getParticipantLimit() == 0) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        }

        newRequest = requestRepository.save(newRequest);

        if (newRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            Long countConfirmedRequests = event.getConfirmedRequests() + 1;
            event.setConfirmedRequests(countConfirmedRequests);
            eventRepository.save(event);
        }

        return requestMapper.toRequestDto(newRequest);
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        User user = getUserOrThrow(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос с ID - %d не найден", requestId)));

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);

        return requestMapper.toRequestDto(request);
    }

    @Override
    public List<RequestDto> getRequestsByRequesterId(Long userId) {
        User user = getUserOrThrow(userId);

        return requestRepository.findByRequesterId(userId).stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    public List<RequestDto> getRequestsByEventId(Long userId, Long eventId) {
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        return requestRepository.findByEventId(eventId).stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        List<Long> requestIds = request.getRequestIds();
        RequestStatus newStatus = request.getStatus();
        List<Request> requests = requestRepository.findByIdIn(requestIds);

        /*if (event.getParticipantLimit() > 0 && event.getRequestModeration() &&
                event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException(String.format("Достигнут лимит по заявкам на событие с ID - %d", eventId));
        }*/

        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();

        Long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        for (Request req : requests) {
            if (!req.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Статус можно изменить только у заявок в состоянии ожидания");
            }

            if (newStatus.equals(RequestStatus.CONFIRMED)) {
                if (confirmedCount < event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmedCount++;
                    confirmedRequests.add(requestMapper.toRequestDto(req));
                } else {
                    /*req.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toRequestDto(req));*/
                    throw new ConflictException(String.format("Достигнут лимит по заявкам на событие с ID - %d", eventId));

                }
            } else {
                req.setStatus(newStatus);
                rejectedRequests.add(requestMapper.toRequestDto(req));
            }
        }

        requestRepository.saveAll(requests);

        event.setConfirmedRequests(confirmedCount);
        eventRepository.save(event);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);

        return result;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", userId)));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с ID - %d не найдено", eventId)));
    }
}
