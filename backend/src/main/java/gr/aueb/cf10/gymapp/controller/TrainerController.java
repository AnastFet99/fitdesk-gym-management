package gr.aueb.cf10.gymapp.controller;

import gr.aueb.cf10.gymapp.dto.TrainerInsertDTO;
import gr.aueb.cf10.gymapp.dto.TrainerReadOnlyDTO;
import gr.aueb.cf10.gymapp.service.ITrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trainers", description = "Trainer management endpoints")
public class TrainerController {

    private final ITrainerService trainerService;

    @Operation(summary = "Create a new trainer", description = "Creates a trainer profile for an existing user (provide user UUID)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer created successfully",
                    content = @Content(schema = @Schema(implementation = TrainerReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Trainer profile already exists for this user")
    })
    @PostMapping
    public ResponseEntity<TrainerReadOnlyDTO> createTrainer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Trainer data (use user UUID, not ID)")
            @Valid @RequestBody TrainerInsertDTO insertDTO) {
        log.info("POST /api/trainers - Creating trainer for user uuid: {}", insertDTO.userUuid());
        TrainerReadOnlyDTO created = trainerService.createTrainer(insertDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a trainer", description = "Updates an existing trainer by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<TrainerReadOnlyDTO> updateTrainer(
            @Parameter(description = "UUID of the trainer to update") @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated trainer data")
            @Valid @RequestBody TrainerInsertDTO insertDTO) {
        log.info("PUT /api/trainers/{} - Updating trainer", uuid);
        TrainerReadOnlyDTO updated = trainerService.updateTrainer(uuid, insertDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a trainer", description = "Deletes a trainer by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trainer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteTrainer(
            @Parameter(description = "UUID of the trainer to delete") @PathVariable UUID uuid) {
        log.info("DELETE /api/trainers/{} - Deleting trainer", uuid);
        trainerService.deleteTrainer(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get trainer by UUID", description = "Retrieves a single trainer by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer found"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<TrainerReadOnlyDTO> getTrainerByUuid(
            @Parameter(description = "UUID of the trainer") @PathVariable UUID uuid) {
        log.info("GET /api/trainers/{} - Fetching trainer", uuid);
        TrainerReadOnlyDTO trainer = trainerService.getTrainerByUuid(uuid);
        return ResponseEntity.ok(trainer);
    }

    @Operation(summary = "Get all trainers", description = "Retrieves all trainers")
    @ApiResponse(responseCode = "200", description = "List of trainers retrieved successfully")
    @GetMapping
    public ResponseEntity<List<TrainerReadOnlyDTO>> getAllTrainers() {
        log.info("GET /api/trainers - Fetching all trainers");
        List<TrainerReadOnlyDTO> trainers = trainerService.getAllTrainers();
        return ResponseEntity.ok(trainers);
    }
}
