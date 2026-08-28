package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf10.gymapp.core.mapper.Mapper;
import gr.aueb.cf10.gymapp.dto.GymClassInsertDTO;
import gr.aueb.cf10.gymapp.dto.GymClassReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.GymClass;
import gr.aueb.cf10.gymapp.model.Trainer;
import gr.aueb.cf10.gymapp.repository.GymClassRepository;
import gr.aueb.cf10.gymapp.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GymClassServiceImpl implements IGymClassService {

    private final GymClassRepository gymClassRepository;
    private final TrainerRepository trainerRepository;
    private final Mapper mapper;

    @Override
    @Transactional
    public GymClassReadOnlyDTO createGymClass(GymClassInsertDTO insertDTO) {
        log.info("Creating gym class: {}", insertDTO.name());

        // Validate trainer exists by UUID
        Trainer trainer = trainerRepository.findByUuid(insertDTO.trainerUuid())
                .orElseThrow(() -> {
                    log.error("Trainer with uuid {} not found", insertDTO.trainerUuid());
                    return new EntityNotFoundException("Trainer", insertDTO.trainerUuid());
                });

        // Validate dateTime is in the future
        if (insertDTO.dateTime().isBefore(LocalDateTime.now())) {
            log.error("Invalid dateTime: {} (must be in the future)", insertDTO.dateTime());
            throw new EntityInvalidArgumentException("Class must be scheduled in the future");
        }

        // Map and save
        GymClass gymClass = mapper.mapToGymClass(insertDTO, trainer);
        GymClass savedGymClass = gymClassRepository.save(gymClass);

        log.info("Successfully created gym class with uuid: {}", savedGymClass.getUuid());
        return mapper.mapToReadOnlyDTO(savedGymClass);
    }

    @Override
    @Transactional
    public GymClassReadOnlyDTO updateGymClass(UUID uuid, GymClassInsertDTO insertDTO) {
        log.info("Updating gym class with uuid: {}", uuid);

        GymClass existingGymClass = gymClassRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("GymClass with uuid {} not found", uuid);
                    return new EntityNotFoundException("GymClass", uuid);
                });

        Trainer trainer = trainerRepository.findByUuid(insertDTO.trainerUuid())
                .orElseThrow(() -> {
                    log.error("Trainer with uuid {} not found", insertDTO.trainerUuid());
                    return new EntityNotFoundException("Trainer", insertDTO.trainerUuid());
                });

        if (insertDTO.dateTime().isBefore(LocalDateTime.now())) {
            log.error("Invalid dateTime: {} (must be in the future)", insertDTO.dateTime());
            throw new EntityInvalidArgumentException("Class must be scheduled in the future");
        }

        // Update fields
        existingGymClass.setName(insertDTO.name());
        existingGymClass.setTrainer(trainer);
        existingGymClass.setCapacity(insertDTO.capacity());
        existingGymClass.setDateTime(insertDTO.dateTime());

        GymClass updatedGymClass = gymClassRepository.save(existingGymClass);
        log.info("Successfully updated gym class with uuid: {}", uuid);

        return mapper.mapToReadOnlyDTO(updatedGymClass);
    }

    @Override
    @Transactional
    public void deleteGymClass(UUID uuid) {
        log.info("Deleting gym class with uuid: {}", uuid);

        GymClass gymClass = gymClassRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("GymClass with uuid {} not found", uuid);
                    return new EntityNotFoundException("GymClass", uuid);
                });

        gymClassRepository.delete(gymClass);
        log.info("Successfully deleted gym class with uuid: {}", uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public GymClassReadOnlyDTO getGymClassByUuid(UUID uuid) {
        log.info("Fetching gym class with uuid: {}", uuid);

        GymClass gymClass = gymClassRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("GymClass with uuid {} not found", uuid);
                    return new EntityNotFoundException("GymClass", uuid);
                });

        return mapper.mapToReadOnlyDTO(gymClass);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymClassReadOnlyDTO> getAllGymClasses() {
        log.info("Fetching all gym classes");

        return gymClassRepository.findAll()
                .stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymClassReadOnlyDTO> getGymClassesByTrainerId(Long trainerId) {
        log.info("Fetching gym classes for trainer id: {}", trainerId);

        return gymClassRepository.findByTrainerId(trainerId)
                .stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }
}
