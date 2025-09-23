package ru.practicum.ewm.hit;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.HitStatsDto;
import ru.practicum.ewm.NewHitRequest;
import ru.practicum.ewm.StatsRequestParam;
import ru.practicum.ewm.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Slf4j
public class HitServiceImpl implements HitService {
    final HitRepository hitRepository;
    final HitMapper hitMapper;
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public HitDto saveHit(NewHitRequest request) {
        Hit hit = hitMapper.toHit(request);

        hit = hitRepository.save(hit);

        return hitMapper.toHitDto(hit);
    }

    @Override
    public List<HitStatsDto> getHitsStats(StatsRequestParam statsRequestParam) {
        LocalDateTime start = LocalDateTime.parse(statsRequestParam.getStart(), formatter);
        LocalDateTime end = LocalDateTime.parse(statsRequestParam.getEnd(), formatter);
        List<String> uris = statsRequestParam.getUris();
        Boolean unique = statsRequestParam.getUnique();

        List<HitStatsDto> listStats = new ArrayList<>();

        if (uris == null || uris.isEmpty()) {
            List<Hit> hits = hitRepository.findAll();

            for (Hit hit : hits) {
                Long countHits;
                if (unique) {
                    countHits = hitRepository.countStatsByParamUnique(start, end, hit.getUri());
                } else {
                    countHits = hitRepository.countStatsByParamUnUnique(start, end, hit.getUri());
                }

                HitStatsDto hitStatsDto = HitStatsDto.builder()
                        .app(hit.getApp())
                        .uri(hit.getUri())
                        .hits(countHits)
                        .build();

                listStats.add(hitStatsDto);
            }
        } else {
            for (String uri : uris) {
                List<Hit> hits = getHitsOrThrow(uri);
                Long countHits;

                if (unique) {
                    countHits = hitRepository.countStatsByParamUnique(start, end, uri);
                } else {
                    countHits = hitRepository.countStatsByParamUnUnique(start, end, uri);
                }

                HitStatsDto hitStatsDto = HitStatsDto.builder()
                        .app(hits.getFirst().getApp())
                        .uri(uri)
                        .hits(countHits)
                        .build();

                listStats.add(hitStatsDto);
            }
        }
        listStats.sort(Comparator.comparingLong(HitStatsDto::getHits).reversed());

        return listStats;
    }

    @Override
    public Long countViewsByIp(String uri) {
        return hitRepository.countDistinctIpsByUri(uri);
    }

    private List<Hit> getHitsOrThrow(String uri) {
        List<Hit> hitsUri = hitRepository.findByUri(uri);

        if (hitsUri.isEmpty()) {
            throw  new NotFoundException(String.format("URI - %s не найден", uri));
        }

        return hitsUri;
    }
}
