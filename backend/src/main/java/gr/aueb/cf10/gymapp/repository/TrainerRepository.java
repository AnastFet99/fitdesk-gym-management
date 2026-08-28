package gr.aueb.cf10.gymapp.repository;

import gr.aueb.cf10.gymapp.model.Trainer;
import gr.aueb.cf10.gymapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUuid(UUID uuid);

    Optional<Trainer> findByUser(User user);

    Optional<Trainer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
