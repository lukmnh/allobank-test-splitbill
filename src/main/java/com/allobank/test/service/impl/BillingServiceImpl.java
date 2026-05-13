package com.allobank.test.service.impl;

import com.allobank.test.dto.billing.BillingCreateRequest;
import com.allobank.test.dto.billing.BillingCreateResponse;
import com.allobank.test.entity.Billing;
import com.allobank.test.entity.Participant;
import com.allobank.test.repository.BillingRepository;
import com.allobank.test.repository.ParticipantRepository;
import com.allobank.test.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {
    private final BillingRepository billingRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public BillingCreateResponse createBillingGroup(BillingCreateRequest request) {
        log.info("entering createBillingGroup() with name of group is {} and total participants is {}", request.getName(), request.getParticipants().size());
        Billing billing = Billing.builder()
                .name(request.getName().trim())
                .build();
        billing = billingRepository.save(billing);

        Billing savedBill = billing;
        List<Participant> participants = request.getParticipants().stream()
                .map(String::trim)
                .distinct()
                .map(name -> {
                    Participant p = new Participant();
                    p.setName(name);
                    p.setBilling(savedBill);
                    return p;
                })
                .toList();

        participantRepository.saveAll(participants);

        return BillingCreateResponse.builder()
                .id(savedBill.getId().toString())
                .billingName(savedBill.getName())
                .build();
    }
}
