package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotValidField;
import ru.practicum.ewm.mapper.event.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.model.reaction.Reaction;
import ru.practicum.ewm.model.reaction.ReactionType;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ReactionRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class EventServiceImplTest {
    @Mock
    EventRepository eventRepository;

    @Mock
    EventMapper eventMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    ReactionRepository reactionRepository;

    @Mock
    StatsClient statsClient;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    EventServiceImpl eventService;

    User testUser;
    Category testCategory;
    Event testEvent;
    NewEventRequest newEventRequest;
    DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@email.ru");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setInitiator(testUser);
        testEvent.setCategory(testCategory);
        testEvent.setState(EventState.PENDING);
        testEvent.setCreatedOn(LocalDateTime.now());
        testEvent.setConfirmedRequests(0L);
        testEvent.setViews(0L);
        testEvent.setEventRating(0L);

        newEventRequest = new NewEventRequest();
        newEventRequest.setTitle("Test Event");
        newEventRequest.setDescription("Test Description");
        newEventRequest.setCategory(1L);
        newEventRequest.setEventDate(LocalDateTime.now().plusDays(1).format(formatter));
    }

    @Test
    void testSaveEvent() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(eventMapper.toEvent(newEventRequest)).thenReturn(testEvent);
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(eventMapper.toEventDto(testEvent)).thenReturn(new EventDto());

        EventDto result = eventService.saveEvent(newEventRequest, 1L);

        assertNotNull(result);
        verify(eventRepository).save(testEvent);
        verify(eventMapper).toEventDto(testEvent);
    }

    @Test
    void testSaveEventNotValidField() {
        newEventRequest.setEventDate(LocalDateTime.now().minusDays(1).format(formatter));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(NotValidField.class, () -> eventService.saveEvent(newEventRequest, 1L));
    }

    @Test
    void testSaveEventUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.saveEvent(newEventRequest, 1L));
    }

    @Test
    void testSaveEventCategoryNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.saveEvent(newEventRequest, 1L));
    }

    @Test
    void testUpdateAdminEvent() {
        AdminUpdateEventRequest updateRequest = new AdminUpdateEventRequest();
        updateRequest.setStateAction(EventAdminUpdateState.PUBLISH_EVENT);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.updateAdminEventFields(testEvent, updateRequest)).thenReturn(testEvent);
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(eventMapper.toEventDto(testEvent)).thenReturn(new EventDto());

        EventDto result = eventService.updateAdminEvent(updateRequest, 1L);

        assertNotNull(result);
        verify(eventRepository).save(testEvent);
    }

    @Test
    void testUpdateAdminEventEventAlreadyPublished() {
        testEvent.setState(EventState.PUBLISHED);
        AdminUpdateEventRequest updateRequest = new AdminUpdateEventRequest();
        updateRequest.setStateAction(EventAdminUpdateState.PUBLISH_EVENT);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> eventService.updateAdminEvent(updateRequest, 1L));
    }

    @Test
    void testUpdateUserEvent() {
        UserUpdateEventRequest updateRequest = new UserUpdateEventRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.updateUserEventFields(testEvent, updateRequest)).thenReturn(testEvent);
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(eventMapper.toEventDto(testEvent)).thenReturn(new EventDto());

        EventDto result = eventService.updateUserEvent(updateRequest, 1L, 1L);

        assertNotNull(result);
        verify(eventRepository).save(testEvent);
    }

    @Test
    void testGetEventsByInitiatorId() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findByInitiatorId(1L, pageable)).thenReturn(List.of(testEvent));
        when(eventMapper.toEventShortDto(testEvent)).thenReturn(new EventShortDto());

        List<EventShortDto> result = eventService.getEventsByInitiatorId(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetEventByIdAndInitiatorId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.findByInitiatorIdAndId(1L, 1L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toEventDto(testEvent)).thenReturn(new EventDto());

        EventDto result = eventService.getEventByIdAndInitiatorId(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void testGetEventsByCriteriaAdmin() {
        AdminEventSearchCriteria criteria = new AdminEventSearchCriteria();
        criteria.setFrom(0);
        criteria.setSize(10);

        when(eventRepository.findEventsByCriteria(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(testEvent));
        when(eventMapper.toEventDto(testEvent)).thenReturn(new EventDto());

        List<EventDto> result = eventService.getEventsByCriteria(criteria);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetEventsByCriteriaPublic() {
        PublicEventSearchCriteria criteria = new PublicEventSearchCriteria();
        criteria.setFrom(0);
        criteria.setSize(10);
        criteria.setOnlyAvailable(false);

        when(eventRepository.findEventsByCriteria(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(testEvent));
        when(eventMapper.toEventShortDto(testEvent)).thenReturn(new EventShortDto());

        List<EventShortDto> result = eventService.getEventsByCriteria(criteria, request);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(statsClient).sendHit(request);
    }

    @Test
    void testGetEventById() {
        testEvent.setState(EventState.PUBLISHED);

        when(request.getRequestURI()).thenReturn("/events/1");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(statsClient.getHitsStats("/events/1")).thenReturn(10L);
        when(eventMapper.toEventDto(testEvent)).thenReturn(new EventDto());

        EventDto result = eventService.getEventById(1L, request);

        assertNotNull(result);
        verify(statsClient).sendHit(request);
        verify(statsClient).getHitsStats("/events/1");
    }

    @Test
    void testGetEventByIdEventNotPublished() {
        testEvent.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(NotFoundException.class, () -> eventService.getEventById(1L, request));
    }

    @Test
    void testAddLike() {
        User likedUser = new User();
        likedUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(likedUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reactionRepository.existsReaction(1L, 2L, ReactionType.LIKE)).thenReturn(false);
        when(reactionRepository.existsReaction(1L, 2L, ReactionType.DISLIKE)).thenReturn(false);
        when(reactionRepository.save(any(Reaction.class))).thenReturn(new Reaction());
        when(reactionRepository.countReactionsByEvent(1L, ReactionType.LIKE)).thenReturn(1L);
        when(reactionRepository.countReactionsByEvent(1L, ReactionType.DISLIKE)).thenReturn(0L);
        when(reactionRepository.countReactionsByInitiator(1L, ReactionType.LIKE)).thenReturn(1L);
        when(reactionRepository.countReactionsByInitiator(1L, ReactionType.DISLIKE)).thenReturn(0L);
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(eventMapper.toEventDtoWithRating(testEvent)).thenReturn(new EventDtoWithRating());

        EventDtoWithRating result = eventService.addLike(1L, 1L, 2L);

        // Assert
        assertNotNull(result);
        verify(reactionRepository).save(any(Reaction.class));
    }


    @Test
    void testAddLikeConflictEx() {
        User likedUser = new User();
        likedUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(likedUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reactionRepository.existsReaction(1L, 2L, ReactionType.LIKE)).thenReturn(true);

        assertThrows(ConflictException.class, () -> eventService.addLike(1L, 1L, 2L));
    }

    @Test
    void testAddDislike() {
        User dislikedUser = new User();
        dislikedUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(dislikedUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reactionRepository.existsReaction(1L, 2L, ReactionType.DISLIKE)).thenReturn(false);
        when(reactionRepository.existsReaction(1L, 2L, ReactionType.LIKE)).thenReturn(false);
        when(reactionRepository.save(any(Reaction.class))).thenReturn(new Reaction());
        when(reactionRepository.countReactionsByEvent(1L, ReactionType.LIKE)).thenReturn(0L);
        when(reactionRepository.countReactionsByEvent(1L, ReactionType.DISLIKE)).thenReturn(1L);
        when(reactionRepository.countReactionsByInitiator(1L, ReactionType.LIKE)).thenReturn(0L);
        when(reactionRepository.countReactionsByInitiator(1L, ReactionType.DISLIKE)).thenReturn(1L);
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(eventMapper.toEventDtoWithRating(testEvent)).thenReturn(new EventDtoWithRating());

        EventDtoWithRating result = eventService.addDislike(1L, 1L, 2L);

        assertNotNull(result);
        verify(reactionRepository).save(any(Reaction.class));
    }

    @Test
    void testRemoveLike() {
        User likedUser = new User();
        likedUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(likedUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reactionRepository.existsReaction(1L, 2L, ReactionType.LIKE)).thenReturn(true);
        when(reactionRepository.countReactionsByEvent(1L, ReactionType.LIKE)).thenReturn(0L);
        when(reactionRepository.countReactionsByEvent(1L, ReactionType.DISLIKE)).thenReturn(0L);
        when(reactionRepository.countReactionsByInitiator(1L, ReactionType.LIKE)).thenReturn(0L);
        when(reactionRepository.countReactionsByInitiator(1L, ReactionType.DISLIKE)).thenReturn(0L);
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(eventMapper.toEventDtoWithRating(testEvent)).thenReturn(new EventDtoWithRating());

        EventDtoWithRating result = eventService.removeLike(1L, 1L, 2L);

        assertNotNull(result);
        verify(reactionRepository).deleteByEventIdAndLikedUserId(1L, 2L);
    }
}