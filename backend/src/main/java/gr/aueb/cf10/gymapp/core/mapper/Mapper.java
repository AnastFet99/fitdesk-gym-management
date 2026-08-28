package gr.aueb.cf10.gymapp.core.mapper;

import gr.aueb.cf10.gymapp.dto.*;
import gr.aueb.cf10.gymapp.model.*;
import gr.aueb.cf10.gymapp.model.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class Mapper {

    // ==================== GymClass Mappings ====================

    public GymClass mapToGymClass(GymClassInsertDTO dto, Trainer trainer) {
        GymClass gymClass = new GymClass();
        gymClass.setName(dto.name());
        gymClass.setTrainer(trainer);
        gymClass.setCapacity(dto.capacity());
        gymClass.setDateTime(dto.dateTime());
        return gymClass;
    }

    public GymClassReadOnlyDTO mapToReadOnlyDTO(GymClass gymClass) {
        Trainer trainer = gymClass.getTrainer();
        return new GymClassReadOnlyDTO(
                gymClass.getUuid(),
                gymClass.getName(),
                trainer.getUuid(),
                trainer.getUser().getName(),
                trainer.getSpecialty(),
                gymClass.getCapacity(),
                gymClass.getDateTime()
        );
    }

    // ==================== User Mappings ====================

    public User mapToUser(UserInsertDTO dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        // Password is set separately in the service with encoding
        user.setRole(dto.role());
        return user;
    }

    public UserReadOnlyDTO mapToReadOnlyDTO(User user) {
        return new UserReadOnlyDTO(
                user.getUuid(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    // ==================== Trainer Mappings ====================

    public Trainer mapToTrainer(TrainerInsertDTO dto, User user) {
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialty(dto.specialty());
        return trainer;
    }

    public TrainerReadOnlyDTO mapToReadOnlyDTO(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerReadOnlyDTO(
                trainer.getUuid(),
                user.getUuid(),
                user.getName(),
                user.getEmail(),
                trainer.getSpecialty()
        );
    }

    // ==================== Member Mappings ====================

    public Member mapToMember(MemberInsertDTO dto, User user) {
        Member member = new Member();
        member.setUser(user);
        member.setPhone(dto.phone());
        return member;
    }

    public MemberReadOnlyDTO mapToReadOnlyDTO(Member member) {
        User user = member.getUser();
        Subscription subscription = member.getSubscription();
        return new MemberReadOnlyDTO(
                member.getUuid(),
                user.getUuid(),
                user.getName(),
                user.getEmail(),
                member.getPhone(),
                subscription != null ? subscription.getUuid() : null
        );
    }

    // ==================== Subscription Mappings ====================

    public Subscription mapToSubscription(SubscriptionInsertDTO dto, Member member) {
        Subscription subscription = new Subscription();
        subscription.setMember(member);
        subscription.setPlanType(dto.planType());
        subscription.setStartDate(dto.startDate());
        subscription.setEndDate(dto.endDate());
        return subscription;
    }

    public SubscriptionReadOnlyDTO mapToReadOnlyDTO(Subscription subscription) {
        Member member = subscription.getMember();
        boolean isActive = subscription.getEndDate().isAfter(LocalDate.now());
        return new SubscriptionReadOnlyDTO(
                subscription.getUuid(),
                member.getUuid(),
                member.getUser().getName(),
                subscription.getPlanType(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                isActive
        );
    }

    // ==================== Booking Mappings ====================

    public Booking mapToBooking(BookingInsertDTO dto, Member member, GymClass gymClass) {
        Booking booking = new Booking();
        booking.setMember(member);
        booking.setGymClass(gymClass);
        booking.setStatus(dto.status() != null ? dto.status() : BookingStatus.PENDING);
        return booking;
    }

    public BookingReadOnlyDTO mapToReadOnlyDTO(Booking booking) {
        Member member = booking.getMember();
        GymClass gymClass = booking.getGymClass();
        return new BookingReadOnlyDTO(
                booking.getUuid(),
                member.getUuid(),
                member.getUser().getName(),
                gymClass.getUuid(),
                gymClass.getName(),
                gymClass.getDateTime(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
