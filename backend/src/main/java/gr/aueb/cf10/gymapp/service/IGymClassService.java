package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.dto.GymClassInsertDTO;
import gr.aueb.cf10.gymapp.dto.GymClassReadOnlyDTO;

import java.util.List;
import java.util.UUID;

public interface IGymClassService {

    /**
     * Creates a new gym class.
     *
     * @param insertDTO the gym class data
     * @return the created gym class as ReadOnlyDTO
     * @throws gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException if trainer not found
     * @throws gr.aueb.cf10.gymapp.core.exceptions.EntityInvalidArgumentException if validation fails
     */
    GymClassReadOnlyDTO createGymClass(GymClassInsertDTO insertDTO);

    /**
     * Updates an existing gym class.
     *
     * @param uuid the gym class UUID
     * @param insertDTO the new gym class data
     * @return the updated gym class as ReadOnlyDTO
     * @throws gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException if gym class or trainer not found
     */
    GymClassReadOnlyDTO updateGymClass(UUID uuid, GymClassInsertDTO insertDTO);

    /**
     * Deletes a gym class by UUID.
     *
     * @param uuid the gym class UUID
     * @throws gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException if gym class not found
     */
    void deleteGymClass(UUID uuid);

    /**
     * Finds a gym class by UUID.
     *
     * @param uuid the gym class UUID
     * @return the gym class as ReadOnlyDTO
     * @throws gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException if not found
     */
    GymClassReadOnlyDTO getGymClassByUuid(UUID uuid);

    /**
     * Gets all gym classes.
     *
     * @return list of gym classes as ReadOnlyDTOs
     */
    List<GymClassReadOnlyDTO> getAllGymClasses();

    /**
     * Gets all gym classes for a specific trainer.
     *
     * @param trainerId the trainer's internal ID
     * @return list of gym classes as ReadOnlyDTOs
     */
    List<GymClassReadOnlyDTO> getGymClassesByTrainerId(Long trainerId);
}
