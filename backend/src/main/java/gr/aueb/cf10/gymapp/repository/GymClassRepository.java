package gr.aueb.cf10.gymapp.repository;

import gr.aueb.cf10.gymapp.model.GymClass;
import gr.aueb.cf10.gymapp.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    Optional<GymClass> findByUuid(UUID uuid);

    List<GymClass> findByTrainer(Trainer trainer);

    List<GymClass> findByTrainerId(Long trainerId);

    List<GymClass> findByDateTimeAfter(LocalDateTime dateTime);

    List<GymClass> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
