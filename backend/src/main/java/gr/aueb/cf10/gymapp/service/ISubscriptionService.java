package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.dto.SubscriptionInsertDTO;
import gr.aueb.cf10.gymapp.dto.SubscriptionReadOnlyDTO;

import java.util.List;
import java.util.UUID;

public interface ISubscriptionService {

    SubscriptionReadOnlyDTO createSubscription(SubscriptionInsertDTO insertDTO);

    SubscriptionReadOnlyDTO updateSubscription(UUID uuid, SubscriptionInsertDTO insertDTO);

    void deleteSubscription(UUID uuid);

    SubscriptionReadOnlyDTO getSubscriptionByUuid(UUID uuid);

    SubscriptionReadOnlyDTO getSubscriptionByMemberUuid(UUID memberUuid);

    List<SubscriptionReadOnlyDTO> getAllSubscriptions();

    List<SubscriptionReadOnlyDTO> getActiveSubscriptions();
}
