package ru.practicum.ewm.hit;

import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.HitStatsDto;
import ru.practicum.ewm.NewHitRequest;
import ru.practicum.ewm.StatsRequestParam;

import java.util.List;

public interface HitService {
    HitDto saveHit(NewHitRequest request);

    List<HitStatsDto> getHitsStats(StatsRequestParam statsRequestParam);
}
