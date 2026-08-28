package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.dto.BookingInsertDTO;
import gr.aueb.cf10.gymapp.dto.BookingReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.enums.BookingStatus;

import java.util.List;
import java.util.UUID;

public interface IBookingService {

    BookingReadOnlyDTO createBooking(BookingInsertDTO insertDTO);

    BookingReadOnlyDTO updateBooking(UUID uuid, BookingInsertDTO insertDTO);

    BookingReadOnlyDTO updateBookingStatus(UUID uuid, BookingStatus status);

    void deleteBooking(UUID uuid);

    BookingReadOnlyDTO getBookingByUuid(UUID uuid);

    List<BookingReadOnlyDTO> getAllBookings();

    List<BookingReadOnlyDTO> getBookingsByMemberUuid(UUID memberUuid);

    List<BookingReadOnlyDTO> getBookingsByGymClassUuid(UUID gymClassUuid);

    List<BookingReadOnlyDTO> getBookingsByStatus(BookingStatus status);
}
