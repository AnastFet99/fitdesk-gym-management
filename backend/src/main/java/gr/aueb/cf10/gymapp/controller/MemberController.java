package gr.aueb.cf10.gymapp.controller;

import gr.aueb.cf10.gymapp.dto.MemberInsertDTO;
import gr.aueb.cf10.gymapp.dto.MemberReadOnlyDTO;
import gr.aueb.cf10.gymapp.service.IMemberService;
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
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Members", description = "Member management endpoints")
public class MemberController {

    private final IMemberService memberService;

    @Operation(summary = "Create a new member", description = "Creates a member profile for an existing user (provide user UUID)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member created successfully",
                    content = @Content(schema = @Schema(implementation = MemberReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Member profile already exists for this user")
    })
    @PostMapping
    public ResponseEntity<MemberReadOnlyDTO> createMember(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Member data (use user UUID, not ID)")
            @Valid @RequestBody MemberInsertDTO insertDTO) {
        log.info("POST /api/members - Creating member for user uuid: {}", insertDTO.userUuid());
        MemberReadOnlyDTO created = memberService.createMember(insertDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a member", description = "Updates an existing member by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<MemberReadOnlyDTO> updateMember(
            @Parameter(description = "UUID of the member to update") @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated member data")
            @Valid @RequestBody MemberInsertDTO insertDTO) {
        log.info("PUT /api/members/{} - Updating member", uuid);
        MemberReadOnlyDTO updated = memberService.updateMember(uuid, insertDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a member", description = "Deletes a member by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteMember(
            @Parameter(description = "UUID of the member to delete") @PathVariable UUID uuid) {
        log.info("DELETE /api/members/{} - Deleting member", uuid);
        memberService.deleteMember(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get member by UUID", description = "Retrieves a single member by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<MemberReadOnlyDTO> getMemberByUuid(
            @Parameter(description = "UUID of the member") @PathVariable UUID uuid) {
        log.info("GET /api/members/{} - Fetching member", uuid);
        MemberReadOnlyDTO member = memberService.getMemberByUuid(uuid);
        return ResponseEntity.ok(member);
    }

    @Operation(summary = "Get all members", description = "Retrieves all members")
    @ApiResponse(responseCode = "200", description = "List of members retrieved successfully")
    @GetMapping
    public ResponseEntity<List<MemberReadOnlyDTO>> getAllMembers() {
        log.info("GET /api/members - Fetching all members");
        List<MemberReadOnlyDTO> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }
}
