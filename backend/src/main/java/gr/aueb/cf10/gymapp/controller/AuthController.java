package gr.aueb.cf10.gymapp.controller;

import gr.aueb.cf10.gymapp.dto.AuthResponse;
import gr.aueb.cf10.gymapp.dto.LoginRequest;
import gr.aueb.cf10.gymapp.dto.RegisterRequest;
import gr.aueb.cf10.gymapp.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final IAuthService authService;

    @Operation(summary = "Register a new user", 
               description = "Creates a new user account and returns a JWT token. " +
                           "Use this endpoint to register ADMIN, TRAINER, or MEMBER accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration data. Example: {\"name\": \"John Doe\", \"email\": \"john@gym.com\", \"password\": \"password123\", \"role\": \"TRAINER\"}")
            @Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - Registering user: {}", request.email());
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Login", 
               description = "Authenticates a user and returns a JWT token. " +
                           "Use the token in the Authorization header as 'Bearer {token}' for protected endpoints.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials. Example: {\"email\": \"john@gym.com\", \"password\": \"password123\"}")
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - Login attempt for: {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
