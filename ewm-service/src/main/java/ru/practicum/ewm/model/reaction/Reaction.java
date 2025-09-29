package ru.practicum.ewm.model.reaction;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "reactions")
public class Reaction {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "event_id")
    Long eventId;

    @JoinColumn(name = "event_user_id")
    Long eventUserId;

    @JoinColumn(name = "liked_user_id")
    Long likedUserId;

    @Enumerated(EnumType.STRING)
    ReactionType reaction;
}
