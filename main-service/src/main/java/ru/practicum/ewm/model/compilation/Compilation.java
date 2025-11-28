package ru.practicum.ewm.model.compilation;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewm.model.event.Event;

import java.util.Set;

/*@Entity
@Table(name = "compilations")*/
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: ManyToMany
    private Set<Event> events;

    @Column(name = "pinned")
    private Boolean pinned;

    @Column(name = "title")
    private String title;
}
