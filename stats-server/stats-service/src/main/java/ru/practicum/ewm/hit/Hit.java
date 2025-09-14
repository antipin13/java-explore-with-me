package ru.practicum.ewm.hit;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "hits")
public class Hit {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    String app;

    String uri;

    String ip;

    @Column(name = "create_date")
    LocalDateTime timestamp;
}
