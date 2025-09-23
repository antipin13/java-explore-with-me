package ru.practicum.ewm.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.NewHitRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class StatsClient {
    final RestTemplate restTemplate;
    String baseUrl;
    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendHit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        NewHitRequest hitRequest = createHitRequest(ip, uri);
        HitDto response = restTemplate.postForObject(
                baseUrl + "/hit",
                hitRequest,
                HitDto.class
        );
    }

    public Long getHitsStats(String uri) {
        String url = String.format("%s/stats/uri?uri=%s", baseUrl, uri);
        ResponseEntity<Long> response = restTemplate.getForEntity(url, Long.class);
        return response.getBody();
    }

    private NewHitRequest createHitRequest(String ip, String uri) {
        return NewHitRequest.builder()
                .app("ewm-main-service")
                .ip(ip)
                .uri(uri)
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }
}



