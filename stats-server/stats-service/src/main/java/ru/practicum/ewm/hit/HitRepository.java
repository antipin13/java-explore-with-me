package ru.practicum.ewm.hit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {
    List<Hit> findByUri(String uri);

    @Query("select count(h.ip) from Hit h where (h.uri = ?3) and (h.timestamp between ?1 and ?2)")
    Long countStatsByParamUnUnique(LocalDateTime start, LocalDateTime end, String uri);

    @Query("select count(distinct h.ip) from Hit h where (h.uri = ?3) and (h.timestamp between ?1 and ?2)")
    Long countStatsByParamUnique(LocalDateTime start, LocalDateTime end, String uri);

    @Query("SELECT COUNT(h) FROM Hit h WHERE h.ip = :ip AND h.uri = :uri")
    Long countByIpAndUri(@Param("ip") String ip, @Param("uri") String uri);

    @Query("SELECT COUNT(DISTINCT h.ip) FROM Hit h WHERE h.uri = :uri")
    Long countDistinctIpsByUri(@Param("uri") String uri);
}
