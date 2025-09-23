package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.request.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);

    List<Request> findByIdIn(List<Long> requestIds);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long requesterId);
}
