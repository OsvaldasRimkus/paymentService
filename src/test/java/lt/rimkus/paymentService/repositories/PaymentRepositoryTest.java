package lt.rimkus.paymentService.repositories;

import lt.rimkus.paymentService.DTOs.PaymentCancellationInfoDTO;
import lt.rimkus.paymentService.models.Money;
import lt.rimkus.paymentService.models.TYPE1Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Should return payments not cancelled and within range")
    void testGetNotCancelledPaymentsWithinRange() {
        // Given
        TYPE1Payment payment1 = createAndPopulatePayment();
        payment1.setMoney(new Money(new BigDecimal("10.00"), "EUR"));
        payment1.setCancellationFee(new Money());

        TYPE1Payment payment2 = createAndPopulatePayment();
        payment2.setMoney(new Money(new BigDecimal("50.00"), "USD"));
        payment2.setCancellationFee(new Money());

        TYPE1Payment payment3 = createAndPopulatePayment();
        payment3.setMoney(new Money(new BigDecimal("100.00"), "EUR"));
        payment3.setCancelled(true);
        payment3.setCancellationFee(new Money(new BigDecimal("1.00"), "EUR"));

        TYPE1Payment payment4 = createAndPopulatePayment();
        payment4.setMoney(new Money(new BigDecimal("2.50"), "USD"));
        payment4.setCancellationFee(new Money());

        TYPE1Payment payment5 = createAndPopulatePayment();
        payment5.setMoney(new Money(new BigDecimal("75.00"), "USD"));
        payment5.setCancellationFee(new Money());

        paymentRepository.saveAll(List.of(payment1, payment2, payment3, payment4, payment5));

        // When
        List<Long> ids = paymentRepository.getNotCancelledPaymentsWithinRange(
                new BigDecimal("5.00"), new BigDecimal("60.00"));

        // Then
        assertThat(ids).containsExactlyInAnyOrder(payment1.getId(), payment2.getId());
        assertThat(ids).doesNotContain(payment3.getId(), payment4.getId(), payment5.getId());
    }

    @Test
    @DisplayName("Should return cancellation details for a payment")
    void testGetPaymentCancellationDetails() {
        // Given
        TYPE1Payment payment = createAndPopulatePayment();
        payment.setMoney(new Money(new BigDecimal("90.99"), "EUR"));
        payment.setCancelled(true);
        payment.setCancellationFee(new Money(new BigDecimal("3.25"), "EUR"));
        paymentRepository.save(payment);

        // When
        PaymentCancellationInfoDTO dto = paymentRepository.getPaymentCancellationDetails(payment.getId());

        // Then
        assertThat(dto.getId()).isEqualTo(payment.getId());
        assertEquals(0, dto.getCancellationFee().getAmount().compareTo(new BigDecimal("3.25")));
    }

    private TYPE1Payment createAndPopulatePayment() {
        TYPE1Payment payment = new TYPE1Payment();
        payment.setType("TYPE1");
        payment.setDebtor_iban("Debtor IBAN");
        payment.setCreditor_iban("Creditor IBAN");
        payment.setCreatedDate(LocalDate.now());
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }
}