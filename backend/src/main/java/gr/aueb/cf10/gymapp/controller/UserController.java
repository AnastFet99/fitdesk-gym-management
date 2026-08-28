package gr.aueb.cf10.gymapp.controller;

import gr.aueb.cf10.gymapp.dto.UserInsertDTO;
import gr.aueb.cf10.gymapp.dto.UserReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.enums.Role;
import gr.aueb.cf10.gymapp.service.IUserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final IUserService userService;

    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    public ResponseEntity<UserReadOnlyDTO> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User data to create")
            @Valid @RequestBody UserInsertDTO insertDTO) {
        log.info("POST /api/users - Creating user: {}", insertDTO.email());
        UserReadOnlyDTO created = userService.createUser(insertDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a user", description = "Updates an existing user by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> updateUser(
            @Parameter(description = "UUID of the user to update") @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated user data")
            @Valid @RequestBody UserInsertDTO insertDTO) {
        log.info("PUT /api/users/{} - Updating user", uuid);
        UserReadOnlyDTO updated = userService.updateUser(uuid, insertDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a user", description = "Deletes a user by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "UUID of the user to delete") @PathVariable UUID uuid) {
        log.info("DELETE /api/users/{} - Deleting user", uuid);
        userService.deleteUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user by UUID", description = "Retrieves a single user by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> getUserByUuid(
            @Parameter(description = "UUID of the user") @PathVariable UUID uuid) {
        log.info("GET /api/users/{} - Fetching user", uuid);
        UserReadOnlyDTO user = userService.getUserByUuid(uuid);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user by email", description = "Retrieves a user by their email address")
    @ApiResponse(responseCode = "200", description = "User found")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserReadOnlyDTO> getUserByEmail(
            @Parameter(description = "Email of the user") @PathVariable String email) {
        log.info("GET /api/users/email/{} - Fetching user", email);
        UserReadOnlyDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get all users", description = "Retrieves all users, optionally filtered by role")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    @GetMapping
    public ResponseEntity<List<UserReadOnlyDTO>> getAllUsers(
            @Parameter(description = "Optional role filter (ADMIN, TRAINER, MEMBER)")
            @RequestParam(required = false) Role role) {
        log.info("GET /api/users - Fetching all users (role: {})", role);

        List<UserReadOnlyDTO> users = role != null
                ? userService.getUsersByRole(role)
                : userService.getAllUsers();

        return ResponseEntity.ok(users);
    }
}
