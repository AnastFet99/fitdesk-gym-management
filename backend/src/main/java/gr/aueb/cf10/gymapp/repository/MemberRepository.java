package gr.aueb.cf10.gymapp.repository;

import gr.aueb.cf10.gymapp.model.Member;
import gr.aueb.cf10.gymapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUuid(UUID uuid);

    Optional<Member> findByUser(User user);

    Optional<Member> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
