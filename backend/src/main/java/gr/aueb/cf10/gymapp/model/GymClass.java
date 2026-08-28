package gr.aueb.cf10.gymapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gym_classes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GymClass extends AbstractEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @OneToMany(mappedBy = "gymClass")
    private List<Booking> bookings = new ArrayList<>();
}
