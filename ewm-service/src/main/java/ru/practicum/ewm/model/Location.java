package ru.practicum.ewm.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class Location {
    Float lat;
    Float lon;
}
