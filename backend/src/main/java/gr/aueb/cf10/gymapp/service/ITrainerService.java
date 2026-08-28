package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.dto.TrainerInsertDTO;
import gr.aueb.cf10.gymapp.dto.TrainerReadOnlyDTO;

import java.util.List;
import java.util.UUID;

public interface ITrainerService {

    TrainerReadOnlyDTO createTrainer(TrainerInsertDTO insertDTO);

    TrainerReadOnlyDTO updateTrainer(UUID uuid, TrainerInsertDTO insertDTO);

    void deleteTrainer(UUID uuid);

    TrainerReadOnlyDTO getTrainerByUuid(UUID uuid);

    List<TrainerReadOnlyDTO> getAllTrainers();
}
