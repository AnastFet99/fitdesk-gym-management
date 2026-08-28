package gr.aueb.cf10.gymapp.controller;

import gr.aueb.cf10.gymapp.dto.BookingInsertDTO;
import gr.aueb.cf10.gymapp.dto.BookingReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.enums.BookingStatus;
import gr.aueb.cf10.gymapp.service.IBookingService;
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
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final IBookingService bookingService;

    @Operation(summary = "Create a new booking", description = "Creates a booking for a member to attend a gym class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully",
                    content = @Content(schema = @Schema(implementation = BookingReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or class at capacity"),
            @ApiResponse(responseCode = "404", description = "Member or gym class not found"),
            @ApiResponse(responseCode = "409", description = "Booking already exists for this member and class")
    })
    @PostMapping
    public ResponseEntity<BookingReadOnlyDTO> createBooking(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Booking data to create")
            @Valid @RequestBody BookingInsertDTO insertDTO) {
        log.info("POST /api/bookings - Creating booking for member: {} and class: {}", 
                insertDTO.memberUuid(), insertDTO.gymClassUuid());
        BookingReadOnlyDTO created = bookingService.createBooking(insertDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a booking", description = "Updates an existing booking by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Booking, member, or gym class not found")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<BookingReadOnlyDTO> updateBooking(
            @Parameter(description = "UUID of the booking to update") @PathVariable UUID uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated booking data")
            @Valid @RequestBody BookingInsertDTO insertDTO) {
        log.info("PUT /api/bookings/{} - Updating booking", uuid);
        BookingReadOnlyDTO updated = bookingService.updateBooking(uuid, insertDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Update booking status", description = "Updates only the status of a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PatchMapping("/{uuid}/status")
    public ResponseEntity<BookingReadOnlyDTO> updateBookingStatus(
            @Parameter(description = "UUID of the booking") @PathVariable UUID uuid,
            @Parameter(description = "New booking status") @RequestParam BookingStatus status) {
        log.info("PATCH /api/bookings/{}/status - Updating status to {}", uuid, status);
        BookingReadOnlyDTO updated = bookingService.updateBookingStatus(uuid, status);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a booking", description = "Deletes a booking by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Booking deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteBooking(
            @Parameter(description = "UUID of the booking to delete") @PathVariable UUID uuid) {
        log.info("DELETE /api/bookings/{} - Deleting booking", uuid);
        bookingService.deleteBooking(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get booking by UUID", description = "Retrieves a single booking by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<BookingReadOnlyDTO> getBookingByUuid(
            @Parameter(description = "UUID of the booking") @PathVariable UUID uuid) {
        log.info("GET /api/bookings/{} - Fetching booking", uuid);
        BookingReadOnlyDTO booking = bookingService.getBookingByUuid(uuid);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Get bookings by member", description = "Retrieves all bookings for a specific member")
    @ApiResponse(responseCode = "200", description = "List of member bookings retrieved successfully")
    @GetMapping("/member/{memberUuid}")
    public ResponseEntity<List<BookingReadOnlyDTO>> getBookingsByMember(
            @Parameter(description = "UUID of the member") @PathVariable UUID memberUuid) {
        log.info("GET /api/bookings/member/{} - Fetching bookings for member", memberUuid);
        List<BookingReadOnlyDTO> bookings = bookingService.getBookingsByMemberUuid(memberUuid);
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Get all bookings", description = "Retrieves all bookings with optional filters")
    @ApiResponse(responseCode = "200", description = "List of bookings retrieved successfully")
    @GetMapping
    public ResponseEntity<List<BookingReadOnlyDTO>> getAllBookings(
            @Parameter(description = "Filter by member UUID") @RequestParam(required = false) UUID memberUuid,
            @Parameter(description = "Filter by gym class UUID") @RequestParam(required = false) UUID gymClassUuid,
            @Parameter(description = "Filter by booking status") @RequestParam(required = false) BookingStatus status) {
        log.info("GET /api/bookings - Fetching bookings (member: {}, class: {}, status: {})", 
                memberUuid, gymClassUuid, status);

        List<BookingReadOnlyDTO> bookings;
        
        if (memberUuid != null) {
            bookings = bookingService.getBookingsByMemberUuid(memberUuid);
        } else if (gymClassUuid != null) {
            bookings = bookingService.getBookingsByGymClassUuid(gymClassUuid);
        } else if (status != null) {
            bookings = bookingService.getBookingsByStatus(status);
        } else {
            bookings = bookingService.getAllBookings();
        }
        
        return ResponseEntity.ok(bookings);
    }
}
