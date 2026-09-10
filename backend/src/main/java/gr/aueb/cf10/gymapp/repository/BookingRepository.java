package gr.aueb.cf10.gymapp.repository;

import gr.aueb.cf10.gymapp.model.Booking;
import gr.aueb.cf10.gymapp.model.GymClass;
import gr.aueb.cf10.gymapp.model.Member;
import gr.aueb.cf10.gymapp.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByUuid(UUID uuid);

    List<Booking> findByMember(Member member);

    List<Booking> findByMemberId(Long memberId);

    List<Booking> findByGymClass(GymClass gymClass);

    List<Booking> findByGymClassId(Long gymClassId);

    List<Booking> findByStatus(BookingStatus status);

    boolean existsByMemberAndGymClass(Member member, GymClass gymClass);

    long countByGymClassAndStatus(GymClass gymClass, BookingStatus status);

    long countByGymClassAndStatusIn(GymClass gymClass, Collection<BookingStatus> statuses);

    @Query("""
            SELECT b.gymClass.id, COUNT(b)
            FROM Booking b
            WHERE b.status IN :statuses
            GROUP BY b.gymClass.id
            """)
    List<Object[]> countBookingsByGymClassAndStatuses(@Param("statuses") Collection<BookingStatus> statuses);
}
