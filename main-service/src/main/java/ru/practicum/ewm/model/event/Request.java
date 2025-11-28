package ru.practicum.ewm.model.event;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewm.model.user.User;

import java.time.LocalDateTime;

/*@Entity
@Table(name = "requests")*/
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created")
    private LocalDateTime created = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    @ToString.Exclude
    private User requester;

    private String status; // TODO: наверное нужен какой-то enum
}
