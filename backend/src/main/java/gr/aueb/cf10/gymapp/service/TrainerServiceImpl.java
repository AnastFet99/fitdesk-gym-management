package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf10.gymapp.core.mapper.Mapper;
import gr.aueb.cf10.gymapp.dto.TrainerInsertDTO;
import gr.aueb.cf10.gymapp.dto.TrainerReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.Trainer;
import gr.aueb.cf10.gymapp.model.User;
import gr.aueb.cf10.gymapp.repository.TrainerRepository;
import gr.aueb.cf10.gymapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerServiceImpl implements ITrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    @Override
    @Transactional
    public TrainerReadOnlyDTO createTrainer(TrainerInsertDTO insertDTO) {
        log.info("Creating trainer for user uuid: {}", insertDTO.userUuid());

        User user = userRepository.findByUuid(insertDTO.userUuid())
                .orElseThrow(() -> {
                    log.error("User with uuid {} not found", insertDTO.userUuid());
                    return new EntityNotFoundException("User", insertDTO.userUuid());
                });

        if (trainerRepository.existsByUserId(user.getId())) {
            log.error("Trainer already exists for user uuid {}", insertDTO.userUuid());
            throw new EntityAlreadyExistsException("Trainer profile already exists for this user");
        }

        Trainer trainer = mapper.mapToTrainer(insertDTO, user);
        Trainer savedTrainer = trainerRepository.save(trainer);

        log.info("Successfully created trainer with uuid: {}", savedTrainer.getUuid());
        return mapper.mapToReadOnlyDTO(savedTrainer);
    }

    @Override
    @Transactional
    public TrainerReadOnlyDTO updateTrainer(UUID uuid, TrainerInsertDTO insertDTO) {
        log.info("Updating trainer with uuid: {}", uuid);

        Trainer existingTrainer = trainerRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Trainer with uuid {} not found", uuid);
                    return new EntityNotFoundException("Trainer", uuid);
                });

        User user = userRepository.findByUuid(insertDTO.userUuid())
                .orElseThrow(() -> {
                    log.error("User with uuid {} not found", insertDTO.userUuid());
                    return new EntityNotFoundException("User", insertDTO.userUuid());
                });

        existingTrainer.setUser(user);
        existingTrainer.setSpecialty(insertDTO.specialty());

        Trainer updatedTrainer = trainerRepository.save(existingTrainer);
        log.info("Successfully updated trainer with uuid: {}", uuid);

        return mapper.mapToReadOnlyDTO(updatedTrainer);
    }

    @Override
    @Transactional
    public void deleteTrainer(UUID uuid) {
        log.info("Deleting trainer with uuid: {}", uuid);

        Trainer trainer = trainerRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Trainer with uuid {} not found", uuid);
                    return new EntityNotFoundException("Trainer", uuid);
                });

        trainerRepository.delete(trainer);
        log.info("Successfully deleted trainer with uuid: {}", uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerReadOnlyDTO getTrainerByUuid(UUID uuid) {
        log.info("Fetching trainer with uuid: {}", uuid);

        Trainer trainer = trainerRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Trainer with uuid {} not found", uuid);
                    return new EntityNotFoundException("Trainer", uuid);
                });

        return mapper.mapToReadOnlyDTO(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerReadOnlyDTO> getAllTrainers() {
        log.info("Fetching all trainers");

        return trainerRepository.findAll()
                .stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }
}
