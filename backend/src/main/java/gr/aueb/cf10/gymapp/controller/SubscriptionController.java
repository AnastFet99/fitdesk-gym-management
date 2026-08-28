package gr.aueb.cf10.gymapp.controller;

import gr.aueb.cf10.gymapp.dto.SubscriptionInsertDTO;
import gr.aueb.cf10.gymapp.dto.SubscriptionReadOnlyDTO;
import gr.aueb.cf10.gymapp.service.ISubscriptionService;
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
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscriptions", description = "Subscription management endpoints")
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;

    @Operation(summary = "Create a new subscription", description = "Creates a subscription for an existing member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscription created successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "409", description = "Subscription already exists for this member")
    })
    @PostMapping
    public ResponseEntity<SubscriptionReadOnlyDTO> createSubscription(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Subscription data to create")
            @Valid @RequestBody SubscriptionInsertDTO insertDTO) {
        log.info("POST /api/subscriptions - Creating subscription for member uuid: {}", insertDTO.memberUuid());
        SubscriptionReadOnlyDTO created = subscriptionService.createSubscription(insertDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a subscription", description = "Updates an existing subscription by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Subscription or member not found")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<SubscriptionReadOnlyDTO> updateSubscription(
            @Parameter(description = "UUID of the subscription to update") @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated subscription data")
            @Valid @RequestBody SubscriptionInsertDTO insertDTO) {
        log.info("PUT /api/subscriptions/{} - Updating subscription", uuid);
        SubscriptionReadOnlyDTO updated = subscriptionService.updateSubscription(uuid, insertDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a subscription", description = "Deletes a subscription by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subscription deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteSubscription(
            @Parameter(description = "UUID of the subscription to delete") @PathVariable UUID uuid) {
        log.info("DELETE /api/subscriptions/{} - Deleting subscription", uuid);
        subscriptionService.deleteSubscription(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get subscription by UUID", description = "Retrieves a single subscription by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription found"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<SubscriptionReadOnlyDTO> getSubscriptionByUuid(
            @Parameter(description = "UUID of the subscription") @PathVariable UUID uuid) {
        log.info("GET /api/subscriptions/{} - Fetching subscription", uuid);
        SubscriptionReadOnlyDTO subscription = subscriptionService.getSubscriptionByUuid(uuid);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Get subscription by member UUID", description = "Retrieves the subscription for a specific member")
    @ApiResponse(responseCode = "200", description = "Subscription found")
    @GetMapping("/member/{memberUuid}")
    public ResponseEntity<SubscriptionReadOnlyDTO> getSubscriptionByMemberUuid(
            @Parameter(description = "UUID of the member") @PathVariable UUID memberUuid) {
        log.info("GET /api/subscriptions/member/{} - Fetching subscription for member", memberUuid);
        SubscriptionReadOnlyDTO subscription = subscriptionService.getSubscriptionByMemberUuid(memberUuid);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Get all subscriptions", description = "Retrieves all subscriptions, optionally filtered by active status")
    @ApiResponse(responseCode = "200", description = "List of subscriptions retrieved successfully")
    @GetMapping
    public ResponseEntity<List<SubscriptionReadOnlyDTO>> getAllSubscriptions(
            @Parameter(description = "Filter by active status") 
            @RequestParam(required = false) Boolean active) {
        log.info("GET /api/subscriptions - Fetching subscriptions (active filter: {})", active);
        
        List<SubscriptionReadOnlyDTO> subscriptions = (active != null && active)
                ? subscriptionService.getActiveSubscriptions()
                : subscriptionService.getAllSubscriptions();
        
        return ResponseEntity.ok(subscriptions);
    }
}
