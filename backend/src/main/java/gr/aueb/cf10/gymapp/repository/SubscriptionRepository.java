package gr.aueb.cf10.gymapp.repository;

import gr.aueb.cf10.gymapp.model.Member;
import gr.aueb.cf10.gymapp.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUuid(UUID uuid);

    Optional<Subscription> findByMember(Member member);

    Optional<Subscription> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);
}
