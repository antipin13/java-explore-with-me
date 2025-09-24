package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotValidField;
import ru.practicum.ewm.mapper.event.EventMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    final EventRepository eventRepository;
    final EventMapper eventMapper;
    final UserRepository userRepository;
    final CategoryRepository categoryRepository;
    final StatsClient statsClient;
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventDto saveEvent(NewEventRequest request, Long userId) {
        User user = getUserOrThrow(userId);
        LocalDateTime eventDate = LocalDateTime.parse(request.getEventDate(), formatter);

        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new NotValidField("Дата начала события не может быть в прошлом");
        }

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format("Категория с ID - %d не найдена", request.getCategory())));

        Event newEvent = eventMapper.toEvent(request);
        newEvent.setCreatedOn(LocalDateTime.now());
        newEvent.setState(EventState.PENDING);
        newEvent.setCategory(category);
        newEvent.setInitiator(user);
        newEvent.setConfirmedRequests(0L);
        newEvent.setViews(0L);

        newEvent = eventRepository.save(newEvent);

        EventDto eventDto = eventMapper.toEventDto(newEvent);

        return eventDto;
    }

    @Override
    public EventDto updateAdminEvent(AdminUpdateEventRequest request, Long eventId) {
        Event existingEvent = getEventOrThrow(eventId);

        if (existingEvent.getState().equals(EventState.PUBLISHED) && request.getStateAction().equals(EventAdminUpdateState.PUBLISH_EVENT)) {
            throw new ConflictException(String.format("Событие с ID - %d уже опубликованно", eventId));
        }

        if (existingEvent.getState().equals(EventState.CANCELED) && request.getStateAction().equals(EventAdminUpdateState.PUBLISH_EVENT)) {
            throw new ConflictException(String.format("Нельзя опубликовать событие с ID - %d, поскольку оно было отменено", eventId));
        }

        if (existingEvent.getState().equals(EventState.PUBLISHED) && request.getStateAction().equals(EventAdminUpdateState.REJECT_EVENT)) {
            throw new ConflictException(String.format("Нельзя отменить событие с ID - %d, поскольку оно было опубликовано", eventId));
        }

        existingEvent = eventMapper.updateAdminEventFields(existingEvent, request);

        existingEvent = eventRepository.save(existingEvent);

        return eventMapper.toEventDto(existingEvent);
    }

    @Override
    public EventDto updateUserEvent(UserUpdateEventRequest request, Long initiatorId, Long eventId) {
        User user = getUserOrThrow(initiatorId);
        Event existingEvent = getEventOrThrow(eventId);

        if (existingEvent.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(String.format("Событие с ID - %d нельзя редактировать, поскольку оно опубликовано", eventId));
        }

        existingEvent = eventMapper.updateUserEventFields(existingEvent, request);

        existingEvent = eventRepository.save(existingEvent);

        return eventMapper.toEventDto(existingEvent);
    }

    @Override
    public List<EventShortDto> getEventsByInitiatorId(Long initiatorId, int from, int size) {
        User user = getUserOrThrow(initiatorId);
        Pageable pageable = PageRequest.of(from, size);
        List<EventShortDto> eventShortDtoList = eventRepository.findByInitiatorId(initiatorId, pageable).stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());

        return eventShortDtoList;
    }

    @Override
    public EventDto getEventByIdAndInitiatorId(Long initiatorId, Long eventId) {
        User user = getUserOrThrow(initiatorId);
        Event event = getEventOrThrow(eventId);

        return eventMapper.toEventDto(eventRepository.findByInitiatorIdAndId(initiatorId, eventId).get());
    }

    @Override
    public List<EventDto> getEventsByCriteria(AdminEventSearchCriteria criteria) {
        LocalDateTime start = criteria.getRangeStart() != null ? criteria.getRangeStart() : LocalDateTime.now();
        LocalDateTime end = criteria.getRangeEnd() != null ? criteria.getRangeEnd() : LocalDateTime.now().plusYears(100);

        List<Event> events = eventRepository.findEventsByCriteria(criteria.getUsers(), criteria.getStates(),
                criteria.getCategories(), start, end, PageRequest.of(criteria.getFrom(), criteria.getSize()));

        return events.stream()
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getEventsByCriteria(PublicEventSearchCriteria criteria, HttpServletRequest request) {
        LocalDateTime start = criteria.getRangeStart() != null ? criteria.getRangeStart() : LocalDateTime.now();
        LocalDateTime end = criteria.getRangeEnd() != null ? criteria.getRangeEnd() : LocalDateTime.now().plusYears(100);

        List<Event> events = eventRepository.findEventsByCriteria(
                criteria.getText(),
                criteria.getCategories(),
                criteria.getPaid(),
                start,
                end,
                criteria.getSort(),
                PageRequest.of(criteria.getFrom(), criteria.getSize())
        );

        if (criteria.getOnlyAvailable()) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit() == 0 ||
                            event.getConfirmedRequests() < event.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        statsClient.sendHit(request);

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto getEventById(Long id, HttpServletRequest request) {
        Event event = getEventOrThrow(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException(String.format("Событие с ID - %d не найдено", id));
        }

        statsClient.sendHit(request);
        Long views = statsClient.getHitsStats(request.getRequestURI());
        event.setViews(views);

        EventDto eventDto = eventMapper.toEventDto(event);

        return eventDto;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", userId)));
    }

    public Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с ID - %d не найдено", eventId)));
    }
}
