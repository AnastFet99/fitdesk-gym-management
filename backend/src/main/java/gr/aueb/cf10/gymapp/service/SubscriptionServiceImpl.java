package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf10.gymapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf10.gymapp.core.mapper.Mapper;
import gr.aueb.cf10.gymapp.dto.SubscriptionInsertDTO;
import gr.aueb.cf10.gymapp.dto.SubscriptionReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.Member;
import gr.aueb.cf10.gymapp.model.Subscription;
import gr.aueb.cf10.gymapp.repository.MemberRepository;
import gr.aueb.cf10.gymapp.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final Mapper mapper;

    @Override
    @Transactional
    public SubscriptionReadOnlyDTO createSubscription(SubscriptionInsertDTO insertDTO) {
        log.info("Creating subscription for member uuid: {}", insertDTO.memberUuid());

        Member member = memberRepository.findByUuid(insertDTO.memberUuid())
                .orElseThrow(() -> {
                    log.error("Member with uuid {} not found", insertDTO.memberUuid());
                    return new EntityNotFoundException("Member", insertDTO.memberUuid());
                });

        if (subscriptionRepository.existsByMemberId(member.getId())) {
            log.error("Subscription already exists for member id {}", member.getId());
            throw new EntityAlreadyExistsException("Subscription already exists for this member");
        }

        if (insertDTO.endDate().isBefore(insertDTO.startDate())) {
            log.error("End date {} is before start date {}", insertDTO.endDate(), insertDTO.startDate());
            throw new EntityInvalidArgumentException("End date must be after start date");
        }

        Subscription subscription = mapper.mapToSubscription(insertDTO, member);
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        log.info("Successfully created subscription with uuid: {}", savedSubscription.getUuid());
        return mapper.mapToReadOnlyDTO(savedSubscription);
    }

    @Override
    @Transactional
    public SubscriptionReadOnlyDTO updateSubscription(UUID uuid, SubscriptionInsertDTO insertDTO) {
        log.info("Updating subscription with uuid: {}", uuid);

        Subscription existingSubscription = subscriptionRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Subscription with uuid {} not found", uuid);
                    return new EntityNotFoundException("Subscription", uuid);
                });

        Member member = memberRepository.findByUuid(insertDTO.memberUuid())
                .orElseThrow(() -> {
                    log.error("Member with uuid {} not found", insertDTO.memberUuid());
                    return new EntityNotFoundException("Member", insertDTO.memberUuid());
                });

        if (insertDTO.endDate().isBefore(insertDTO.startDate())) {
            log.error("End date {} is before start date {}", insertDTO.endDate(), insertDTO.startDate());
            throw new EntityInvalidArgumentException("End date must be after start date");
        }

        existingSubscription.setMember(member);
        existingSubscription.setPlanType(insertDTO.planType());
        existingSubscription.setStartDate(insertDTO.startDate());
        existingSubscription.setEndDate(insertDTO.endDate());

        Subscription updatedSubscription = subscriptionRepository.save(existingSubscription);
        log.info("Successfully updated subscription with uuid: {}", uuid);

        return mapper.mapToReadOnlyDTO(updatedSubscription);
    }

    @Override
    @Transactional
    public void deleteSubscription(UUID uuid) {
        log.info("Deleting subscription with uuid: {}", uuid);

        Subscription subscription = subscriptionRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Subscription with uuid {} not found", uuid);
                    return new EntityNotFoundException("Subscription", uuid);
                });

        subscriptionRepository.delete(subscription);
        log.info("Successfully deleted subscription with uuid: {}", uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionReadOnlyDTO getSubscriptionByUuid(UUID uuid) {
        log.info("Fetching subscription with uuid: {}", uuid);

        Subscription subscription = subscriptionRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Subscription with uuid {} not found", uuid);
                    return new EntityNotFoundException("Subscription", uuid);
                });

        return mapper.mapToReadOnlyDTO(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionReadOnlyDTO getSubscriptionByMemberUuid(UUID memberUuid) {
        log.info("Fetching subscription for member uuid: {}", memberUuid);

        Member member = memberRepository.findByUuid(memberUuid)
                .orElseThrow(() -> {
                    log.error("Member with uuid {} not found", memberUuid);
                    return new EntityNotFoundException("Member", memberUuid);
                });

        Subscription subscription = subscriptionRepository.findByMemberId(member.getId())
                .orElseThrow(() -> {
                    log.error("Subscription not found for member id {}", member.getId());
                    return new EntityNotFoundException("Subscription not found for this member");
                });

        return mapper.mapToReadOnlyDTO(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionReadOnlyDTO> getAllSubscriptions() {
        log.info("Fetching all subscriptions");

        return subscriptionRepository.findAll()
                .stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionReadOnlyDTO> getActiveSubscriptions() {
        log.info("Fetching active subscriptions");

        LocalDate today = LocalDate.now();
        return subscriptionRepository.findAll()
                .stream()
                .filter(sub -> sub.getEndDate().isAfter(today))
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }
}
