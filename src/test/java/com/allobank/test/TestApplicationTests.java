package com.allobank.test;

import com.allobank.test.dto.settlement.SettlementResponse;
import com.allobank.test.entity.Billing;
import com.allobank.test.entity.Participant;
import com.allobank.test.entity.Settlement;
import com.allobank.test.exception.BusinessException;
import com.allobank.test.repository.BillingRepository;
import com.allobank.test.repository.ExpenseRepository;
import com.allobank.test.repository.ParticipantRepository;
import com.allobank.test.repository.SettlementRepository;
import com.allobank.test.service.PaymentService;
import com.allobank.test.service.impl.SettlementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestApplicationTests {

	@Mock
	private BillingRepository billingRepository;
	@Mock
	private ExpenseRepository expenseRepository;
	@Mock
	private ParticipantRepository participantRepository;
	@Mock
	private SettlementRepository settlementRepository;
	@Mock
	private PaymentService paymentService;

	@InjectMocks
	private SettlementServiceImpl settlementService;

	private Billing billing;
	private Participant lukman;
	private Participant lia;


	@BeforeEach
	void setUp() {
		billing = new Billing();
		billing.setId(1L);
		billing.setName("Trip to Kuala Lumpur");

		lukman = new Participant();
		lukman.setId(1L);
		lukman.setName("Lukman");
		lukman.setBilling(billing);

		lia = new Participant();
		lia.setId(2L);
		lia.setName("Lia");
		lia.setBilling(billing);
	}

	@Test
	@DisplayName("billing tidak ditemukan → throw exception")
	void historySettlement_billingNotFound_throwsException() {
		when(billingRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> settlementService.historySettlementPerBill(99L))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("99");

		verifyNoInteractions(settlementRepository);
	}

	@Test
	@DisplayName("ada 3 settlement → return 3 response urut DESC")
	void historySettlement_multipleSettlements_returnsAllInOrder() {
		Settlement s1 = buildSettlement(1L, new BigDecimal("100000"), new BigDecimal("105000"));
		Settlement s2 = buildSettlement(2L, new BigDecimal("200000"), new BigDecimal("210000"));
		Settlement s3 = buildSettlement(3L, new BigDecimal("300000"), new BigDecimal("315000"));

		when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
		when(settlementRepository.findByBillingIdWithTransactions(1L))
				.thenReturn(List.of(s3, s2, s1));
		when(expenseRepository.findByBillingIdWithShares(1L)).thenReturn(List.of());
		when(participantRepository.findByBillingId(1L)).thenReturn(List.of(lukman, lia));
		when(paymentService.findByBillingId(1L)).thenReturn(List.of());

		List<SettlementResponse> result = settlementService.historySettlementPerBill(1L);

		assertThat(result).hasSize(3);
		assertThat(result.get(0).getSettlementId()).isEqualTo("3");
		assertThat(result.get(1).getSettlementId()).isEqualTo("2");
		assertThat(result.get(2).getSettlementId()).isEqualTo("1");
	}

	private Settlement buildSettlement(Long id, BigDecimal totalExpenses, BigDecimal grandTotal) {
		Settlement s = Settlement.builder()
				.billing(billing)
				.totalExpenses(totalExpenses)
				.grandTotal(grandTotal)
				.transactions(new ArrayList<>())
				.build();
		s.setId(id);
		s.setCreatedAt(LocalDateTime.now());
		return s;
	}

}
