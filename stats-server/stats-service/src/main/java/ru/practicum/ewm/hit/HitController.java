package ru.practicum.ewm.hit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.HitStatsDto;
import ru.practicum.ewm.NewHitRequest;
import ru.practicum.ewm.StatsRequestParam;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HitController {
    final HitServiceImpl hitService;
    ObjectMapper mapper = new ObjectMapper();

    @PostMapping
    @RequestMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitDto create(@RequestBody NewHitRequest request) {
        log.info("Запрос на добавление статистики - {}", request);
        return hitService.saveHit(request);
    }

    @GetMapping
    @RequestMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<HitStatsDto> getHitsStats(@RequestParam String start,
                                          @RequestParam String end,
                                          @RequestParam(required = false) List<String> uris,
                                          @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        StatsRequestParam statsRequestParam = StatsRequestParam.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(unique)
                .build();

        return hitService.getHitsStats(statsRequestParam);
    }
}
