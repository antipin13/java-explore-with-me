package ru.practicum.ewm.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.model.request.RequestStatus;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    RequestStatus status;
}
