package ru.practicum.ewm.service.request;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.request.RequestMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.request.RequestStatus;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class RequestServiceImplTest {
    @Mock
    RequestRepository requestRepository;

    @Mock
    RequestMapper requestMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    EventRepository eventRepository;

    @InjectMocks
    RequestServiceImpl requestService;

    User testUser;
    User testInitiator;
    Event testEvent;
    Request testRequest;
    RequestDto testRequestDto;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@email.com");

        testInitiator = new User();
        testInitiator.setId(2L);
        testInitiator.setName("Initiator");
        testInitiator.setEmail("initiator@email.com");

        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setInitiator(testInitiator);
        testEvent.setState(EventState.PUBLISHED);
        testEvent.setParticipantLimit(10);
        testEvent.setConfirmedRequests(5L);
        testEvent.setRequestModeration(true);

        testRequest = Request.builder()
                .id(1L)
                .event(testEvent)
                .requester(testUser)
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .build();

        testRequestDto = new RequestDto();
        testRequestDto.setId(1L);
        testRequestDto.setEvent(1L);
        testRequestDto.setRequester(1L);
        testRequestDto.setStatus(RequestStatus.PENDING.toString());
        testRequestDto.setCreated(LocalDateTime.now().format(formatter));
    }

    @Test
    void testSaveRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.findByEventIdAndRequesterId(1L, 1L)).thenReturn(Optional.empty());
        when(requestRepository.save(any(Request.class))).thenReturn(testRequest);
        when(requestMapper.toRequestDto(testRequest)).thenReturn(testRequestDto);

        RequestDto result = requestService.saveRequest(1L, 1L);

        assertNotNull(result);
        verify(requestRepository).save(any(Request.class));
    }

    @Test
    void testSaveRequestEventNotPublished() {
        testEvent.setState(EventState.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> requestService.saveRequest(1L, 1L));
    }

    @Test
    void testSaveRequestUserIsInitiator() {
        testEvent.setInitiator(testUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> requestService.saveRequest(1L, 1L));
    }

    @Test
    void testSaveRequestParticipantLimitReached() {
        testEvent.setConfirmedRequests(10L);
        testEvent.setParticipantLimit(10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> requestService.saveRequest(1L, 1L));
    }

    @Test
    void testSaveRequestRequestAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.findByEventIdAndRequesterId(1L, 1L)).thenReturn(Optional.of(testRequest));

        assertThrows(ConflictException.class, () -> requestService.saveRequest(1L, 1L));
    }

    @Test
    void testCancelRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(requestMapper.toRequestDto(testRequest)).thenReturn(testRequestDto);

        RequestDto result = requestService.cancelRequest(1L, 1L);

        assertNotNull(result);
        assertEquals(RequestStatus.CANCELED, testRequest.getStatus());
        verify(requestRepository).save(testRequest);
    }

    @Test
    void testCancelRequestRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.cancelRequest(1L, 1L));
    }

    @Test
    void testGetRequestsByRequesterId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.findByRequesterId(1L)).thenReturn(List.of(testRequest));
        when(requestMapper.toRequestDto(testRequest)).thenReturn(testRequestDto);

        List<RequestDto> result = requestService.getRequestsByRequesterId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(requestRepository).findByRequesterId(1L);
    }

    @Test
    void testGetRequestsByEventId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.findByEventId(1L)).thenReturn(List.of(testRequest));
        when(requestMapper.toRequestDto(testRequest)).thenReturn(testRequestDto);

        List<RequestDto> result = requestService.getRequestsByEventId(1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(requestRepository).findByEventId(1L);
    }

    @Test
    void testUpdateStatusRequest() {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        Request request1 = Request.builder().id(1L).event(testEvent).status(RequestStatus.PENDING).build();
        Request request2 = Request.builder().id(2L).event(testEvent).status(RequestStatus.PENDING).build();

        RequestDto requestDto1 = new RequestDto();
        RequestDto requestDto2 = new RequestDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(request1, request2));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(5L);
        when(requestMapper.toRequestDto(request1)).thenReturn(requestDto1);
        when(requestMapper.toRequestDto(request2)).thenReturn(requestDto2);
        when(requestRepository.saveAll(anyList())).thenReturn(List.of(request1, request2));
        when(eventRepository.save(testEvent)).thenReturn(testEvent);

        EventRequestStatusUpdateResult result = requestService.updateStatusRequest(1L, 1L, updateRequest);

        assertNotNull(result);
        assertEquals(2, result.getConfirmedRequests().size());
        assertEquals(0, result.getRejectedRequests().size());
        assertEquals(RequestStatus.CONFIRMED, request1.getStatus());
        assertEquals(RequestStatus.CONFIRMED, request2.getStatus());
    }
}