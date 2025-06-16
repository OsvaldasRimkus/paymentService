package lt.rimkus.paymentService.services;

import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.models.Money;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.models.TestPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentCancellationServiceTest {

    private PaymentCancellationService paymentCancellationService;
    private Payment payment;
    private LocalDate cancellationDate;
    private LocalDateTime cancellationTime;

    @BeforeEach
    void setUp() {
        paymentCancellationService = new PaymentCancellationService();
        cancellationDate = LocalDate.of(2023, 10, 15);
        cancellationTime = LocalDateTime.of(2023, 10, 15, 14, 30);
        payment = createTestPayment();
    }

    private Payment createTestPayment() {
        Payment payment = new TestPayment();
        payment.setId(1L);
        payment.setCancelled(false);
        payment.setType("TYPE1");
        payment.setCreatedDate(cancellationDate);
        payment.setCreatedAt(LocalDateTime.of(2023, 10, 15, 10, 0)); // 4.5 hours before cancellation
        return payment;
    }

    @Test
    @DisplayName("Should successfully prepare payment for cancellation when all conditions are met")
    void preparePaymentForCancellation_Success() throws RequestValidationException {
        // Given
        assertFalse(payment.isCancelled());

        // When
        paymentCancellationService.preparePaymentForCancellation(payment, cancellationDate, cancellationTime);

        // Then
        assertTrue(payment.isCancelled());
        assertNotNull(payment.getCancellationFee());
        assertEquals("EUR", payment.getCancellationFee().getCurrency());
        assertEquals(cancellationTime, payment.getCancellationTime());

        BigDecimal expectedFee = BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(0.05));
        assertEquals(expectedFee, payment.getCancellationFee().getAmount());
    }

    @Test
    @DisplayName("Should throw exception when payment is already cancelled")
    void preparePaymentForCancellation_AlreadyCancelled() {
        // Given
        payment.setCancelled(true);

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.preparePaymentForCancellation(payment, cancellationDate, cancellationTime)
        );

        assertTrue(exception.getMessage().contains("Payment with id "));
        assertTrue(exception.getMessage().contains(" is already canceled"));
        assertTrue(exception.getMessage().contains(payment.getId().toString()));
    }

    @Test
    @DisplayName("Should throw exception when cancellation is not on same day as creation")
    void preparePaymentForCancellation_DifferentDay() {
        // Given
        LocalDate differentDate = cancellationDate.plusDays(1);

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.preparePaymentForCancellation(payment, differentDate, cancellationTime)
        );

        assertTrue(exception.getMessage().contains("Payment can be cancelled only on the day of its creation"));
    }

    @ParameterizedTest
    @DisplayName("Should calculate correct cancellation fee for different payment types")
    @CsvSource({
            "TYPE1, 10, 0.50",    // 10 hours * 0.05 = 0.5
            "TYPE2, 5, 0.50",     // 5 hours * 0.1 = 0.5
            "TYPE3, 2, 0.30",     // 2 hours * 0.15 = 0.3
            "TYPE1, 0, 0.00",     // 0 hours * 0.05 = 0.0
            "TYPE2, 1, 0.10"      // 1 hour * 0.1 = 0.1
    })
    void calculateCancellationFee_ValidTypes(String paymentType, long hours, double expectedFee) throws RequestValidationException {
        // When
        BigDecimal result = paymentCancellationService.calculateCancellationFee(paymentType, hours);

        // Then
        assertEquals(BigDecimal.valueOf(expectedFee).setScale(2), result.setScale(2));
    }

    @Test
    @DisplayName("Should throw exception for unknown payment type")
    void calculateCancellationFee_UnknownType() {
        // Given
        String unknownType = "UNKNOWN_TYPE";

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.calculateCancellationFee(unknownType, 5)
        );

        assertTrue(exception.getMessage().contains("No data found for payment type: "));
        assertTrue(exception.getMessage().contains(unknownType));
    }

    @ParameterizedTest
    @DisplayName("Should handle edge cases for hours calculation")
    @ValueSource(longs = {0, 1, 24, 100, 1000})
    void calculateCancellationFee_EdgeCaseHours(long hours) throws RequestValidationException {
        // Given
        String paymentType = "TYPE1";
        BigDecimal expectedFee = BigDecimal.valueOf(hours).multiply(BigDecimal.valueOf(0.05));

        // When
        BigDecimal result = paymentCancellationService.calculateCancellationFee(paymentType, hours);

        // Then
        assertEquals(expectedFee, result);
    }

    @Test
    @DisplayName("Should correctly calculate hours between creation and cancellation time")
    void preparePaymentForCancellation_HoursCalculation() throws RequestValidationException {
        // Given - payment created at 10:00, cancelled at 16:30 (6.5 hours later)
        payment.setCreatedAt(LocalDateTime.of(2023, 10, 15, 10, 0));
        LocalDateTime cancellationTime = LocalDateTime.of(2023, 10, 15, 16, 30);

        // When
        paymentCancellationService.preparePaymentForCancellation(payment, cancellationDate, cancellationTime);

        // Then
        BigDecimal expectedFee = BigDecimal.valueOf(6).multiply(BigDecimal.valueOf(0.05));
        assertEquals(expectedFee, payment.getCancellationFee().getAmount());
    }

    @Test
    @DisplayName("Should handle same creation and cancellation time (0 hours)")
    void preparePaymentForCancellation_ZeroHours() throws RequestValidationException {
        // Given
        LocalDateTime sameTime = LocalDateTime.of(2023, 10, 15, 10, 0);
        payment.setCreatedAt(sameTime);

        // When
        paymentCancellationService.preparePaymentForCancellation(payment, cancellationDate, sameTime);

        // Then
        assertEquals(0, BigDecimal.valueOf(0.0).compareTo(payment.getCancellationFee().getAmount()));
    }

    @Test
    @DisplayName("Should properly initialize Money object for cancellation fee")
    void preparePaymentForCancellation_MoneyObjectInitialization() throws RequestValidationException {
        // When
        paymentCancellationService.preparePaymentForCancellation(payment, cancellationDate, cancellationTime);

        // Then
        assertNotNull(payment.getCancellationFee());
        assertNotNull(payment.getCancellationFee().getCurrency());
        assertNotNull(payment.getCancellationFee().getAmount());
        assertEquals("EUR", payment.getCancellationFee().getCurrency());
    }

    @Test
    @DisplayName("Should handle TYPE2 payment correctly")
    void preparePaymentForCancellation_Type2Payment() throws RequestValidationException {
        // Given
        payment.setType("TYPE2");
        payment.setCreatedAt(LocalDateTime.of(2023, 10, 15, 12, 0)); // 2.5 hours before cancellation

        // When
        paymentCancellationService.preparePaymentForCancellation(payment, cancellationDate, cancellationTime);

        // Then
        BigDecimal expectedFee = BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(0.1).setScale(2));
        assertEquals(expectedFee, payment.getCancellationFee().getAmount());
    }

    @Test
    @DisplayName("Should handle TYPE3 payment correctly")
    void preparePaymentForCancellation_Type3Payment() throws RequestValidationException {
        // Given
        payment.setType("TYPE3");
        payment.setCreatedAt(LocalDateTime.of(2023, 10, 15, 11, 30)); // 3 hours before cancellation

        // When
        paymentCancellationService.preparePaymentForCancellation(payment, cancellationDate, cancellationTime);

        // Then
        BigDecimal expectedFee = BigDecimal.valueOf(3).multiply(BigDecimal.valueOf(0.15));
        assertEquals(expectedFee, payment.getCancellationFee().getAmount());
    }

    @Test
    @DisplayName("Should handle payment with unknown type in preparePaymentForCancellation")
    void preparePaymentForCancellation_UnknownPaymentType() {
        // Given
        payment.setType("UNKNOWN_TYPE");

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.preparePaymentForCancellation(payment, cancellationDate, cancellationTime)
        );

        assertTrue(exception.getMessage().contains("No data found for payment type: "));
        assertTrue(exception.getMessage().contains("UNKNOWN_TYPE"));
    }

    @Test
    @DisplayName("Should maintain payment state when exception occurs")
    void preparePaymentForCancellation_StatePreservationOnException() {
        // Given
        payment.setCancelled(true); // This will cause an exception
        boolean originalCancelledState = payment.isCancelled();

        // When & Then
        assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.preparePaymentForCancellation(payment, cancellationDate, cancellationTime)
        );

        // Payment state should remain unchanged
        assertEquals(originalCancelledState, payment.isCancelled());
    }

    @Test
    @DisplayName("Should calculate fee correctly for valid payment type and positive hours")
    void shouldCalculateFeeForValidPaymentTypeAndPositiveHours() throws RequestValidationException {
        // Given
        String paymentType = "TYPE1";
        long hours = 5;

        // When
        BigDecimal result = paymentCancellationService.calculateCancellationFee(paymentType, hours);

        // Then
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(new BigDecimal("0.25"), result);
    }

    @Test
    @DisplayName("Should calculate fee correctly for zero hours")
    void shouldCalculateFeeForZeroHours() throws RequestValidationException {
        // Given
        String paymentType = "TYPE3";
        long hours = 0;

        // When
        BigDecimal result = paymentCancellationService.calculateCancellationFee(paymentType, hours);

        // Then
        assertEquals(BigDecimal.ZERO.setScale(2), result);
    }

    @Test
    @DisplayName("Should throw RequestValidationException for invalid payment type")
    void shouldThrowExceptionForInvalidPaymentType() {
        // Given
        String invalidPaymentType = "INVALID_TYPE";
        long hours = 5;

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.calculateCancellationFee(invalidPaymentType, hours)
        );

        assertTrue(exception.getMessage().contains("INVALID_TYPE"));
        assertTrue(exception.getMessage().contains("No data found for payment type: "));
    }

    @Test
    @DisplayName("Should throw RequestValidationException for null payment type")
    void shouldThrowExceptionForNullPaymentType() {
        // Given
        String nullPaymentType = null;
        long hours = 5;

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.calculateCancellationFee(nullPaymentType, hours)
        );
        assertTrue(exception.getMessage().contains("Type is required"));
    }

    @Test
    @DisplayName("Should throw RequestValidationException for empty payment type")
    void shouldThrowExceptionForEmptyPaymentType() {
        // Given
        String emptyPaymentType = "";
        long hours = 5;

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.calculateCancellationFee(emptyPaymentType, hours)
        );
        assertTrue(exception.getMessage().contains("Type is required"));
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 10, 24, 48, 100, 1000})
    @DisplayName("Should calculate fee correctly for various hour values")
    void shouldCalculateFeeForVariousHours(long hours) throws RequestValidationException {
        // Given
        String paymentType = "TYPE2";

        // When
        BigDecimal result = paymentCancellationService.calculateCancellationFee(paymentType, hours);

        // Then
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest
    @CsvSource({
            "TYPE1, 5",
            "TYPE2, 10",
            "TYPE3, 3"
    })
    @DisplayName("Should calculate fee correctly for different payment types")
    void shouldCalculateFeeForDifferentPaymentTypes(String paymentType, long hours)
            throws RequestValidationException {
        // Given
        // Payment type and hours are provided as parameters

        // When
        BigDecimal result = paymentCancellationService.calculateCancellationFee(paymentType, hours);

        // Then
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Should handle negative hours correctly")
    void shouldHandleNegativeHours() {
        // Given
        String paymentType = "CREDIT_CARD";
        long negativeHours = -5;

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.calculateCancellationFee(paymentType, negativeHours)
        );
        assertTrue(exception.getMessage().contains("Hours cannot be negative"));
    }

    @Test
    @DisplayName("Should handle very large hour values")
    void shouldHandleVeryLargeHours() {
        // Given
        String paymentType = "TYPE1";
        long largeHours = Long.MAX_VALUE;

        // When & Then
        assertDoesNotThrow(() -> {
            BigDecimal result = paymentCancellationService.calculateCancellationFee(paymentType, largeHours);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should return precise BigDecimal calculation")
    void shouldReturnPreciseBigDecimalCalculation() throws RequestValidationException {
        // Given
        String paymentType = "TYPE3";
        long hours = 3;

        // When
        BigDecimal result = paymentCancellationService.calculateCancellationFee(paymentType, hours);

        // Then
        assertNotNull(result);
        assertEquals(2, result.scale());
    }

    @Test
    @DisplayName("Should be case sensitive for payment types")
    void shouldBeCaseSensitiveForPaymentTypes() {
        // Given
        String lowerCasePaymentType = "type1";
        long hours = 5;

        // When & Then
        assertThrows(
                RequestValidationException.class,
                () -> paymentCancellationService.calculateCancellationFee(lowerCasePaymentType, hours)
        );
    }

    @Test
    @DisplayName("Should correctly update payment data")
    void shouldUpdatePaymentWithCancellationData() {
        // Given
        Payment payment = new TestPayment();
        payment.setMoney(new Money());
        LocalDateTime cancellationTime = LocalDateTime.now();
        BigDecimal fee = new BigDecimal("10.50");

        // When
        paymentCancellationService.updatePaymentData(payment, cancellationTime, fee);

        // Then
        assertTrue(payment.isCancelled());
        assertNotNull(payment.getCancellationFee());
        assertEquals("EUR", payment.getCancellationFee().getCurrency());
        assertEquals(fee, payment.getCancellationFee().getAmount());
        assertEquals(cancellationTime, payment.getCancellationTime());
    }
}