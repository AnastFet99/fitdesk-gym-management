package gr.aueb.cf10.gymapp.controller;

import gr.aueb.cf10.gymapp.dto.GymClassInsertDTO;
import gr.aueb.cf10.gymapp.dto.GymClassReadOnlyDTO;
import gr.aueb.cf10.gymapp.service.IGymClassService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gym-classes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gym Classes", description = "Gym class management endpoints")
public class GymClassController {

    private final IGymClassService gymClassService;

    @Operation(summary = "Create a new gym class", description = "Creates a new gym class with the provided details. Use trainer UUID, not internal ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Gym class created successfully",
                    content = @Content(schema = @Schema(implementation = GymClassReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PostMapping
    public ResponseEntity<GymClassReadOnlyDTO> createGymClass(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Gym class data. Example: {\"name\": \"Morning Yoga\", \"trainerUuid\": \"e98998a9-cfca-4c1c-ac48-869561565621\", \"capacity\": 20, \"dateTime\": \"2026-07-28T10:00:00\"}")
            @Valid @RequestBody GymClassInsertDTO insertDTO) {
        log.info("POST /api/gym-classes - Creating gym class: {}", insertDTO.name());
        GymClassReadOnlyDTO created = gymClassService.createGymClass(insertDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a gym class", description = "Updates an existing gym class by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gym class updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Gym class or trainer not found")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<GymClassReadOnlyDTO> updateGymClass(
            @Parameter(description = "UUID of the gym class to update") @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated gym class data")
            @Valid @RequestBody GymClassInsertDTO insertDTO) {
        log.info("PUT /api/gym-classes/{} - Updating gym class", uuid);
        GymClassReadOnlyDTO updated = gymClassService.updateGymClass(uuid, insertDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a gym class", description = "Deletes a gym class by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Gym class deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Gym class not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteGymClass(
            @Parameter(description = "UUID of the gym class to delete") @PathVariable UUID uuid) {
        log.info("DELETE /api/gym-classes/{} - Deleting gym class", uuid);
        gymClassService.deleteGymClass(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get gym class by UUID", description = "Retrieves a single gym class by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gym class found"),
            @ApiResponse(responseCode = "404", description = "Gym class not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<GymClassReadOnlyDTO> getGymClassByUuid(
            @Parameter(description = "UUID of the gym class") @PathVariable UUID uuid) {
        log.info("GET /api/gym-classes/{} - Fetching gym class", uuid);
        GymClassReadOnlyDTO gymClass = gymClassService.getGymClassByUuid(uuid);
        return ResponseEntity.ok(gymClass);
    }

    @Operation(summary = "Get all gym classes", description = "Retrieves all gym classes, optionally filtered by trainer ID")
    @ApiResponse(responseCode = "200", description = "List of gym classes retrieved successfully")
    @GetMapping
    public ResponseEntity<List<GymClassReadOnlyDTO>> getAllGymClasses(
            @Parameter(description = "Optional trainer ID to filter classes") 
            @RequestParam(required = false) Long trainerId) {
        log.info("GET /api/gym-classes - Fetching all gym classes (trainerId: {})", trainerId);

        List<GymClassReadOnlyDTO> gymClasses = trainerId != null
                ? gymClassService.getGymClassesByTrainerId(trainerId)
                : gymClassService.getAllGymClasses();

        return ResponseEntity.ok(gymClasses);
    }
}
