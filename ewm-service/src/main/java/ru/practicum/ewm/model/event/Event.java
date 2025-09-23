package ru.practicum.ewm.model.event;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    String annotation;

    @JoinColumn(name = "confirmed_requests")
    Long confirmedRequests;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @JoinColumn(name = "created_on")
    LocalDateTime createdOn;

    String description;

    @JoinColumn(name = "event_date")
    LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    User initiator;

    @Embedded
    Location location;

    Boolean paid;
    Integer participantLimit;
    LocalDateTime publishedOn;
    Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    EventState state;
    String title;
    Long views;

    @ManyToMany(mappedBy = "events")
    Set<Compilation> collections = new HashSet<>();
}
