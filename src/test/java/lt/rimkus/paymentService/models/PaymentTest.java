package lt.rimkus.paymentService.models;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.MoneyDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TestPaymentDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Payment Class Tests")
class PaymentTest {

    private TestPayment payment;
    private CreatePaymentRequestDTO validRequestDTO;
    private MoneyDTO validMoney;

    @BeforeEach
    void setUp() {
        payment = new TestPayment();

        // Setup valid requestDTO
        validMoney = new MoneyDTO();
        validMoney.setAmount(new BigDecimal("100.00"));
        validMoney.setCurrency("EUR");

        validRequestDTO = new CreatePaymentRequestDTO();
        validRequestDTO.setType("TYPE1");
        validRequestDTO.setMoney(validMoney);
        validRequestDTO.setDebtor_iban("DE89370400440532013000");
        validRequestDTO.setCreditor_iban("FR1420041010050500013M02606");
    }

    @Nested
    @DisplayName("Payment Type Validation Tests")
    class PaymentTypeValidationTests {

        @Test
        @DisplayName("Should pass validation when payment type is valid")
        void givenValidPaymentType_whenValidatingEntityCreationRequest_thenShouldPass() {
            // Given
            validRequestDTO.setType("TYPE1");

            // When & Then
            assertDoesNotThrow(() -> payment.validateEntityCreationRequest(validRequestDTO));
        }

        @Test
        @DisplayName("Should throw exception when payment type is null")
        void givenNullPaymentType_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validRequestDTO.setType(null);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Type is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when payment type is empty")
        void givenEmptyPaymentType_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validRequestDTO.setType("");

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Type is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when payment type is invalid")
        void givenInvalidPaymentType_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            String invalidType = "INVALID_TYPE";
            validRequestDTO.setType(invalidType);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Unsupported payment type: " + invalidType, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Amount Validation Tests")
    class AmountValidationTests {

        @Test
        @DisplayName("Should pass validation when amount is positive")
        void givenPositiveAmount_whenValidatingEntityCreationRequest_thenShouldPass() {
            // Given
            validMoney.setAmount(new BigDecimal("150.50"));
            validRequestDTO.setMoney(validMoney);

            // When & Then
            assertDoesNotThrow(() -> payment.validateEntityCreationRequest(validRequestDTO));
        }

        @Test
        @DisplayName("Should throw exception when money object is null")
        void givenNullMoney_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validRequestDTO.setMoney(null);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Please check your request structure, currency and amount should go as money", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void givenNullAmount_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validMoney.setAmount(null);
            validRequestDTO.setMoney(validMoney);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Amount is required and must be more than 0", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount is zero")
        void givenZeroAmount_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validMoney.setAmount(BigDecimal.ZERO);
            validRequestDTO.setMoney(validMoney);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Amount is required and must be more than 0", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void givenNegativeAmount_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validMoney.setAmount(new BigDecimal("-10.00"));
            validRequestDTO.setMoney(validMoney);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Amount is required and must be more than 0", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount has more than 2 decimal places")
        void givenAmountWithExcessiveDecimalPlaces_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validMoney.setAmount(new BigDecimal("100.123"));
            validRequestDTO.setMoney(validMoney);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Please provide an amount with no more than 2 decimal places", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Currency Validation Tests")
    class CurrencyValidationTests {

        @Test
        @DisplayName("Should pass validation when currency is valid")
        void givenValidCurrency_whenValidatingEntityCreationRequest_thenShouldPass() {
            // Given
            validMoney.setCurrency("EUR");
            validRequestDTO.setMoney(validMoney);

            // When & Then
            assertDoesNotThrow(() -> payment.validateEntityCreationRequest(validRequestDTO));
        }

        @Test
        @DisplayName("Should throw exception when currency is null")
        void givenNullCurrency_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validMoney.setCurrency(null);
            validRequestDTO.setMoney(validMoney);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Currency is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when currency is empty")
        void givenEmptyCurrency_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validMoney.setCurrency("");
            validRequestDTO.setMoney(validMoney);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Currency is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when currency is not supported")
        void givenUnsupportedCurrency_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            String unsupportedCurrency = "XXX";
            validMoney.setCurrency(unsupportedCurrency);
            validRequestDTO.setMoney(validMoney);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Unsupported currency code: " + unsupportedCurrency, exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when currency is not compatible with payment type")
        void givenIncompatibleCurrencyForPaymentType_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            String paymentType = "TYPE2";
            String incompatibleCurrency = "EUR";
            validRequestDTO.setType(paymentType);
            validMoney.setCurrency(incompatibleCurrency);
            validRequestDTO.setMoney(validMoney);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals(paymentType + " payment is not allowed to be used with currency " + incompatibleCurrency, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("IBAN Validation Tests")
    class IBANValidationTests {

        @Test
        @DisplayName("Should pass validation when both IBANs are valid")
        void givenValidIBANs_whenValidatingEntityCreationRequest_thenShouldPass() {
            // Given
            validRequestDTO.setDebtor_iban("DE89370400440532013000");
            validRequestDTO.setCreditor_iban("FR1420041010050500013M02606");

            // When & Then
            assertDoesNotThrow(() -> payment.validateEntityCreationRequest(validRequestDTO));
        }

        @Test
        @DisplayName("Should throw exception when debtor IBAN is null")
        void givenNullDebtorIBAN_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validRequestDTO.setDebtor_iban(null);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Debtor IBAN is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when debtor IBAN is empty")
        void givenEmptyDebtorIBAN_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validRequestDTO.setDebtor_iban("");

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Debtor IBAN is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when creditor IBAN is null")
        void givenNullCreditorIBAN_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validRequestDTO.setCreditor_iban(null);

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Creditor IBAN is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when creditor IBAN is empty")
        void givenEmptyCreditorIBAN_whenValidatingEntityCreationRequest_thenShouldThrowException() {
            // Given
            validRequestDTO.setCreditor_iban("");

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(validRequestDTO)
            );
            assertEquals("Creditor IBAN is required", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Data Population Tests")
    class DataPopulationTests {

        @Test
        @DisplayName("Should populate common data correctly from request DTO")
        void givenValidRequestDTO_whenPopulatingCommonData_thenShouldSetAllFields() {
            // Given
            CreatePaymentRequestDTO requestDTO = new CreatePaymentRequestDTO();
            requestDTO.setType("TYPE1");
            MoneyDTO money = new MoneyDTO();
            money.setAmount(new BigDecimal("250.75"));
            money.setCurrency("EUR");
            requestDTO.setMoney(money);
            requestDTO.setDebtor_iban("DE89370400440532013000");
            requestDTO.setCreditor_iban("FR1420041010050500013M02606");

            // When
            payment.populateCommonData(requestDTO);

            // Then
            assertEquals("TYPE1", payment.getType());
            assertEquals(new BigDecimal("250.75"), payment.getMoney().getAmount());
            assertEquals("EUR", payment.getMoney().getCurrency());
            assertEquals("DE89370400440532013000", payment.getDebtor_iban());
            assertEquals("FR1420041010050500013M02606", payment.getCreditor_iban());
        }

        @Test
        @DisplayName("Should populate DTO data correctly from entity")
        void givenPaymentEntity_whenPopulatingCommonDTOData_thenShouldSetAllDTOFields() {
            // Given
            payment.setId(123L);
            payment.setType("TYPE1");
            Money money = new Money();
            money.setAmount(new BigDecimal("300.25"));
            money.setCurrency("USD");
            payment.setMoney(money);
            payment.setDebtor_iban("DE89370400440532013000");
            payment.setCreditor_iban("FR1420041010050500013M02606");

            PaymentDTO dto = new TestPaymentDTO();

            // When
            payment.populateCommonDTOData(dto);

            // Then
            assertEquals(123L, dto.getId());
            assertEquals("TYPE1", dto.getType());
            assertEquals(new BigDecimal("300.25"), dto.getMoney().getAmount());
            assertEquals("USD", dto.getMoney().getCurrency());
            assertEquals("DE89370400440532013000", dto.getDebtor_iban());
            assertEquals("FR1420041010050500013M02606", dto.getCreditor_iban());
        }
    }

    @Nested
    @DisplayName("Setter Validation Tests")
    class SetterValidationTests {

        @Test
        @DisplayName("Should set type successfully when value is not null")
        void givenValidType_whenSettingType_thenShouldSetSuccessfully() {
            // Given
            String validType = "TYPE1";

            // When
            payment.setType(validType);

            // Then
            assertEquals(validType, payment.getType());
        }

        @Test
        @DisplayName("Should throw exception when setting type to null")
        void givenNullType_whenSettingType_thenShouldThrowException() {
            // Given
            String nullType = null;

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> payment.setType(nullType)
            );
            assertEquals("Type cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should set debtor IBAN successfully when value is not null")
        void givenValidDebtorIBAN_whenSettingDebtorIBAN_thenShouldSetSuccessfully() {
            // Given
            String validIban = "DE89370400440532013000";

            // When
            payment.setDebtor_iban(validIban);

            // Then
            assertEquals(validIban, payment.getDebtor_iban());
        }

        @Test
        @DisplayName("Should throw exception when setting debtor IBAN to null")
        void givenNullDebtorIBAN_whenSettingDebtorIBAN_thenShouldThrowException() {
            // Given
            String nullIban = null;

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> payment.setDebtor_iban(nullIban)
            );
            assertEquals("Debtor IBAN cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should set creditor IBAN successfully when value is not null")
        void givenValidCreditorIBAN_whenSettingCreditorIBAN_thenShouldSetSuccessfully() {
            // Given
            String validIban = "FR1420041010050500013M02606";

            // When
            payment.setCreditor_iban(validIban);

            // Then
            assertEquals(validIban, payment.getCreditor_iban());
        }

        @Test
        @DisplayName("Should throw exception when setting creditor IBAN to null")
        void givenNullCreditorIBAN_whenSettingCreditorIBAN_thenShouldThrowException() {
            // Given
            String nullIban = null;

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> payment.setCreditor_iban(nullIban)
            );
            assertEquals("Creditor IBAN cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should set and get created date correctly")
        void testSetCreatedDate() {
            // Given
            LocalDate expectedDate = LocalDate.of(2025, 1, 1);

            // When
            payment.setCreatedDate(expectedDate);

            // Then
            assertEquals(expectedDate, payment.getCreatedDate());
        }

        @Test
        @DisplayName("Should handle null created date")
        void testSetCreatedDateNull() {
            // Given
            LocalDate nullDate = null;

            // When
            payment.setCreatedDate(nullDate);

            // Then
            assertNull(payment.getCreatedDate());
        }

        @Test
        @DisplayName("Should set and get created at timestamp correctly")
        void testSetCreatedAt() {
            // Given
            LocalDateTime expectedDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);

            // When
            payment.setCreatedAt(expectedDateTime);

            // Then
            assertEquals(expectedDateTime, payment.getCreatedAt());
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void testSetCreatedAtNull() {
            // Given
            LocalDateTime nullDateTime = null;

            // When
            payment.setCreatedAt(nullDateTime);

            // Then
            assertNull(payment.getCreatedAt());
        }

        @Test
        @DisplayName("Should set cancelled status to true")
        void testSetCancelledTrue() {
            // When
            payment.setCancelled(true);

            // Then
            assertTrue(payment.isCancelled());
        }

        @Test
        @DisplayName("Should set cancelled status to false")
        void testSetCancelledFalse() {
            // When
            payment.setCancelled(false);

            // Then
            assertFalse(payment.isCancelled());
        }

        @Test
        @DisplayName("Should set and get cancellation fee correctly")
        void testSetCancellationFee() {
            // Given
            Money expectedFee = new Money();
            expectedFee.setCurrency("USD");
            expectedFee.setAmount(BigDecimal.valueOf(25.50));

            // When
            payment.setCancellationFee(expectedFee);

            // Then
            assertEquals(expectedFee, payment.getCancellationFee());
        }

        @Test
        @DisplayName("Should handle null cancellation fee")
        void testSetCancellationFeeNull() {
            // Given
            Money nullFee = null;

            // When
            payment.setCancellationFee(nullFee);

            // Then
            assertNull(payment.getCancellationFee());
        }

        @Test
        @DisplayName("Should set and get cancellation time correctly")
        void testSetCancellationTime() {
            // Given
            LocalDateTime expectedCancellationTime = LocalDateTime.of(2024, 1, 15, 14, 20, 30);

            // When
            payment.setCancellationTime(expectedCancellationTime);

            // Then
            assertEquals(expectedCancellationTime, payment.getCancellationTime());
        }

        @Test
        @DisplayName("Should handle null cancellation time")
        void testSetCancellationTimeNull() {
            // Given
            LocalDateTime nullCancellationTime = null;

            // When
            payment.setCancellationTime(nullCancellationTime);

            // Then
            assertNull(payment.getCancellationTime());
        }

        @Test
        @DisplayName("Should maintain independent setter behavior")
        void testSettersIndependence() {
            // Given
            LocalDate createdDate = LocalDate.of(2024, 1, 15);
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            boolean cancelled = true;
            Money cancellationFee = new Money();
            cancellationFee.setCurrency("EUR");
            cancellationFee.setAmount(BigDecimal.valueOf(15.75));
            LocalDateTime cancellationTime = LocalDateTime.of(2024, 1, 15, 11, 45, 0);

            // When
            payment.setCreatedDate(createdDate);
            payment.setCreatedAt(createdAt);
            payment.setCancelled(cancelled);
            payment.setCancellationFee(cancellationFee);
            payment.setCancellationTime(cancellationTime);

            // Then
            assertEquals(createdDate, payment.getCreatedDate());
            assertEquals(createdAt, payment.getCreatedAt());
            assertEquals(cancelled, payment.isCancelled());
            assertEquals(cancellationFee, payment.getCancellationFee());
            assertEquals(cancellationTime, payment.getCancellationTime());
        }

        @Test
        @DisplayName("Should allow overwriting previously set values")
        void testSettersOverwrite() {
            // Given - Set initial values
            payment.setCreatedDate(LocalDate.of(2024, 1, 1));
            payment.setCancelled(false);
            Money cancellationFee = new Money();
            cancellationFee.setCurrency("USD");
            cancellationFee.setAmount(BigDecimal.valueOf(10.00));
            payment.setCancellationFee(cancellationFee);


            // When - Overwrite with new values
            LocalDate newCreatedDate = LocalDate.of(2024, 2, 1);
            boolean newCancelled = true;
            Money newCancellationFee = new Money();
            newCancellationFee.setCurrency("EUR");
            newCancellationFee.setAmount(BigDecimal.valueOf(20.00));

            payment.setCreatedDate(newCreatedDate);
            payment.setCancelled(newCancelled);
            payment.setCancellationFee(newCancellationFee);

            // Then
            assertEquals(newCreatedDate, payment.getCreatedDate());
            assertEquals(newCancelled, payment.isCancelled());
            assertEquals(newCancellationFee, payment.getCancellationFee());
        }

        @Test
        @DisplayName("Should set and get notification status correctly")
        void testSetNotificationStatus() {
            // Given
            String expectedStatus = "SUCCESS";

            // When
            payment.setNotificationStatus(expectedStatus);

            // Then
            assertEquals(expectedStatus, payment.getNotificationStatus());
        }

        @Test
        @DisplayName("Should set and get notification status correctly when it is null")
        void testSetNotificationStatusWhenNull() {
            // Given
            String expectedStatus = null;

            // When
            payment.setNotificationStatus(expectedStatus);

            // Then
            assertEquals(expectedStatus, payment.getNotificationStatus());
        }

    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should validate complete valid payment successfully")
        void givenCompleteValidPayment_whenValidatingEntityCreationRequest_thenShouldPass() {
            // Given
            CreatePaymentRequestDTO completeValidRequest = new CreatePaymentRequestDTO();
            completeValidRequest.setType("TYPE1");
            MoneyDTO money = new MoneyDTO();
            money.setAmount(new BigDecimal("1000.00"));
            money.setCurrency("EUR");
            completeValidRequest.setMoney(money);
            completeValidRequest.setDebtor_iban("DE89370400440532013000");
            completeValidRequest.setCreditor_iban("FR1420041010050500013M02606");

            // When & Then
            assertDoesNotThrow(() -> payment.validateEntityCreationRequest(completeValidRequest));
        }

        @Test
        @DisplayName("Should fail validation with multiple errors and throw first encountered error")
        void givenMultipleValidationErrors_whenValidatingEntityCreationRequest_thenShouldThrowFirstError() {
            // Given
            CreatePaymentRequestDTO invalidRequest = new CreatePaymentRequestDTO();
            invalidRequest.setType(null); // First error - type is mandatory
            invalidRequest.setMoney(null); // Second error - money is missing
            invalidRequest.setDebtor_iban(null); // Third error - debtor IBAN is mandatory
            invalidRequest.setCreditor_iban(null); // Fourth error - creditor IBAN is mandatory

            // When & Then
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> payment.validateEntityCreationRequest(invalidRequest)
            );
            // Should throw the first validation error encountered (type mandatory)
            assertEquals("Type is required", exception.getMessage());
        }
    }
}