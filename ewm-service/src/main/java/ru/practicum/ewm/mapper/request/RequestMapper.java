package ru.practicum.ewm.mapper.request;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.model.request.Request;

import java.time.format.DateTimeFormatter;

@Component
public class RequestMapper {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .created(request.getCreated().format(formatter))
                .status(request.getStatus().toString())
                .build();
    }
}
