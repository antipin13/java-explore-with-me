package ru.practicum.ewm.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.NewHitRequest;
import ru.practicum.ewm.event.dto.EventDto;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class StatsClient {
    final RestTemplate restTemplate;

    @Value("${stats-service.url}")
    String baseUrl;

    public void sendHit(EventDto eventDto, HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        NewHitRequest hitRequest = createHitRequest(eventDto, ip, uri);

        HitDto response = restTemplate.postForObject(
                baseUrl + "/hits",
                hitRequest,
                HitDto.class
        );
    }

    private NewHitRequest createHitRequest(EventDto eventDto, String ip, String uri) {
        return NewHitRequest.builder()
                .ip(ip)
                .uri(uri)
                .build();
    }
}
