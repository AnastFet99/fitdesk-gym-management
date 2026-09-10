package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf10.gymapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf10.gymapp.core.mapper.Mapper;
import gr.aueb.cf10.gymapp.dto.BookingInsertDTO;
import gr.aueb.cf10.gymapp.dto.BookingReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.Booking;
import gr.aueb.cf10.gymapp.model.GymClass;
import gr.aueb.cf10.gymapp.model.Member;
import gr.aueb.cf10.gymapp.model.User;
import gr.aueb.cf10.gymapp.model.enums.BookingStatus;
import gr.aueb.cf10.gymapp.model.enums.Role;
import gr.aueb.cf10.gymapp.repository.BookingRepository;
import gr.aueb.cf10.gymapp.repository.GymClassRepository;
import gr.aueb.cf10.gymapp.repository.MemberRepository;
import gr.aueb.cf10.gymapp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final MemberRepository memberRepository;
    private final GymClassRepository gymClassRepository;
    private final Mapper mapper;

    @Override
    @Transactional
    public BookingReadOnlyDTO createBooking(BookingInsertDTO insertDTO) {
        log.info("Creating booking for member uuid: {} and gym class uuid: {}",
                insertDTO.memberUuid(), insertDTO.gymClassUuid());

        Member member;
        if (isMember()) {
            member = requireAuthenticatedMember();
        } else {
            member = memberRepository.findByUuid(insertDTO.memberUuid())
                    .orElseThrow(() -> {
                        log.error("Member with uuid {} not found", insertDTO.memberUuid());
                        return new EntityNotFoundException("Member", insertDTO.memberUuid());
                    });
        }

        GymClass gymClass = gymClassRepository.findByUuid(insertDTO.gymClassUuid())
                .orElseThrow(() -> {
                    log.error("GymClass with uuid {} not found", insertDTO.gymClassUuid());
                    return new EntityNotFoundException("GymClass", insertDTO.gymClassUuid());
                });

        // Check if member already has a booking for this class
        if (bookingRepository.existsByMemberAndGymClass(member, gymClass)) {
            log.error("Booking already exists for member id {} and gym class id {}",
                    member.getId(), gymClass.getId());
            throw new EntityAlreadyExistsException("Booking already exists for this member and gym class");
        }

        // Check capacity
        long confirmedBookings = bookingRepository.countByGymClassAndStatus(gymClass, BookingStatus.CONFIRMED);
        if (confirmedBookings >= gymClass.getCapacity()) {
            log.error("Gym class {} is at full capacity ({}/{})",
                    gymClass.getName(), confirmedBookings, gymClass.getCapacity());
            throw new EntityInvalidArgumentException("Gym class is at full capacity");
        }

        Booking booking = mapper.mapToBooking(insertDTO, member, gymClass);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Successfully created booking with uuid: {}", savedBooking.getUuid());
        return mapper.mapToReadOnlyDTO(savedBooking);
    }

    @Override
    @Transactional
    public BookingReadOnlyDTO updateBooking(UUID uuid, BookingInsertDTO insertDTO) {
        log.info("Updating booking with uuid: {}", uuid);

        Booking existingBooking = bookingRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Booking with uuid {} not found", uuid);
                    return new EntityNotFoundException("Booking", uuid);
                });

        Member member;
        if (isMember()) {
            Member authenticatedMember = requireOwnedBooking(existingBooking);
            member = authenticatedMember;
        } else {
            member = memberRepository.findByUuid(insertDTO.memberUuid())
                    .orElseThrow(() -> {
                        log.error("Member with uuid {} not found", insertDTO.memberUuid());
                        return new EntityNotFoundException("Member", insertDTO.memberUuid());
                    });
        }

        GymClass gymClass = gymClassRepository.findByUuid(insertDTO.gymClassUuid())
                .orElseThrow(() -> {
                    log.error("GymClass with uuid {} not found", insertDTO.gymClassUuid());
                    return new EntityNotFoundException("GymClass", insertDTO.gymClassUuid());
                });

        existingBooking.setMember(member);
        existingBooking.setGymClass(gymClass);
        if (insertDTO.status() != null) {
            existingBooking.setStatus(insertDTO.status());
        }

        Booking updatedBooking = bookingRepository.save(existingBooking);
        log.info("Successfully updated booking with uuid: {}", uuid);

        return mapper.mapToReadOnlyDTO(updatedBooking);
    }

    @Override
    @Transactional
    public BookingReadOnlyDTO updateBookingStatus(UUID uuid, BookingStatus status) {
        log.info("Updating booking status with uuid: {} to {}", uuid, status);

        Booking existingBooking = bookingRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Booking with uuid {} not found", uuid);
                    return new EntityNotFoundException("Booking", uuid);
                });

        if (isMember()) {
            requireOwnedBooking(existingBooking);
        }

        existingBooking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(existingBooking);

        log.info("Successfully updated booking status with uuid: {} to {}", uuid, status);
        return mapper.mapToReadOnlyDTO(updatedBooking);
    }

    @Override
    @Transactional
    public void deleteBooking(UUID uuid) {
        log.info("Deleting booking with uuid: {}", uuid);

        Booking booking = bookingRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Booking with uuid {} not found", uuid);
                    return new EntityNotFoundException("Booking", uuid);
                });

        if (isMember()) {
            requireOwnedBooking(booking);
        }

        bookingRepository.delete(booking);
        log.info("Successfully deleted booking with uuid: {}", uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingReadOnlyDTO getBookingByUuid(UUID uuid) {
        log.info("Fetching booking with uuid: {}", uuid);

        Booking booking = bookingRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Booking with uuid {} not found", uuid);
                    return new EntityNotFoundException("Booking", uuid);
                });

        if (isMember()) {
            requireOwnedBooking(booking);
        }

        return mapper.mapToReadOnlyDTO(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingReadOnlyDTO> getAllBookings() {
        log.info("Fetching all bookings");

        if (isMember()) {
            Member member = requireAuthenticatedMember();
            return bookingRepository.findByMemberId(member.getId())
                    .stream()
                    .map(mapper::mapToReadOnlyDTO)
                    .toList();
        }

        return bookingRepository.findAll()
                .stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingReadOnlyDTO> getBookingsByMemberUuid(UUID memberUuid) {
        log.info("Fetching bookings for member uuid: {}", memberUuid);

        if (isMember()) {
            Member authenticatedMember = requireAuthenticatedMember();
            if (!authenticatedMember.getUuid().equals(memberUuid)) {
                throw new AccessDeniedException("Access denied");
            }
        }

        Member member = memberRepository.findByUuid(memberUuid)
                .orElseThrow(() -> {
                    log.error("Member with uuid {} not found", memberUuid);
                    return new EntityNotFoundException("Member", memberUuid);
                });

        return bookingRepository.findByMemberId(member.getId())
                .stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingReadOnlyDTO> getBookingsByGymClassUuid(UUID gymClassUuid) {
        log.info("Fetching bookings for gym class uuid: {}", gymClassUuid);

        GymClass gymClass = gymClassRepository.findByUuid(gymClassUuid)
                .orElseThrow(() -> {
                    log.error("GymClass with uuid {} not found", gymClassUuid);
                    return new EntityNotFoundException("GymClass", gymClassUuid);
                });

        List<Booking> bookings = bookingRepository.findByGymClassId(gymClass.getId());
        if (isMember()) {
            Member member = requireAuthenticatedMember();
            bookings = bookings.stream()
                    .filter(b -> b.getMember().getId().equals(member.getId()))
                    .toList();
        }

        return bookings.stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingReadOnlyDTO> getBookingsByStatus(BookingStatus status) {
        log.info("Fetching bookings with status: {}", status);

        List<Booking> bookings = bookingRepository.findByStatus(status);
        if (isMember()) {
            Member member = requireAuthenticatedMember();
            bookings = bookings.stream()
                    .filter(b -> b.getMember().getId().equals(member.getId()))
                    .toList();
        }

        return bookings.stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("Access denied");
        }
        return details.getUser();
    }

    private boolean isMember() {
        return currentUser().getRole() == Role.MEMBER;
    }

    private Member requireAuthenticatedMember() {
        User user = currentUser();
        return memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Access denied"));
    }

    private Member requireOwnedBooking(Booking booking) {
        Member authenticatedMember = requireAuthenticatedMember();
        if (!authenticatedMember.getId().equals(booking.getMember().getId())) {
            throw new AccessDeniedException("Access denied");
        }
        return authenticatedMember;
    }
}
