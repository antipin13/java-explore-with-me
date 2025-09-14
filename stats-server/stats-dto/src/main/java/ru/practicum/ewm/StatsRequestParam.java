package ru.practicum.ewm;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsRequestParam {
    String start;
    String end;
    List<String> uris;
    Boolean unique;
}
