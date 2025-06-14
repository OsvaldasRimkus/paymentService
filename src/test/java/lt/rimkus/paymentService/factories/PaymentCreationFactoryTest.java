package lt.rimkus.paymentService.factories;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.MoneyDTO;
import lt.rimkus.paymentService.enums.PaymentType;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.models.TYPE1Payment;
import lt.rimkus.paymentService.models.TYPE2Payment;
import lt.rimkus.paymentService.models.TYPE3Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCreationFactory Tests")
class PaymentCreationFactoryTest {

    @InjectMocks
    private PaymentCreationFactory paymentCreationFactory;

    private CreatePaymentRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup valid money object
        MoneyDTO validMoney = new MoneyDTO();
        validMoney.setAmount(new BigDecimal("100.00"));
        validMoney.setCurrency("EUR");

        // Setup valid requestDTO
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
        @DisplayName("Should throw exception when payment type is invalid")
        void givenInvalidPaymentType_whenCreatingNewPayment_thenShouldThrowException() {
            // Given
            validRequestDTO.setType("INVALID_TYPE");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> paymentCreationFactory.createNewPayment(validRequestDTO)
            );
            assertTrue(exception.getMessage().contains("INVALID_TYPE"));
        }

        @Test
        @DisplayName("Should throw exception when payment type is null")
        void givenNullPaymentType_whenCreatingNewPayment_thenShouldThrowException() {
            // Given
            validRequestDTO.setType(null);

            // When & Then
            assertThrows(
                    NullPointerException.class,
                    () -> paymentCreationFactory.createNewPayment(validRequestDTO)
            );
        }

        @Test
        @DisplayName("Should handle case sensitivity in payment type")
        void givenLowerCasePaymentType_whenCreatingNewPayment_thenShouldThrowException() {
            // Given
            validRequestDTO.setType("type1");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> paymentCreationFactory.createNewPayment(validRequestDTO)
            );
            assertTrue(exception.getMessage().contains("type1"));
        }
    }

    @Nested
    @DisplayName("Validation Error Propagation Tests")
    class ValidationErrorPropagationTests {

        @Test
        @DisplayName("Should propagate validation exception from TYPE1 payment")
        void givenInvalidTYPE1Request_whenCreatingNewPayment_thenShouldPropagateValidationException() throws RequestValidationException {
            // Given
            validRequestDTO.setType("TYPE1");
            TYPE1Payment mockPayment = mock(TYPE1Payment.class);
            RequestValidationException validationException = new RequestValidationException("Validation failed");

            doThrow(validationException).when(mockPayment).validateEntityCreationRequest(validRequestDTO);

            try (MockedStatic<PaymentType> mockedPaymentType = mockStatic(PaymentType.class)) {
                mockedPaymentType.when(() -> PaymentType.valueOf("TYPE1")).thenReturn(PaymentType.TYPE1);

                PaymentCreationFactory spyFactory = spy(paymentCreationFactory);
                doReturn(mockPayment).when(spyFactory).createPayment(validRequestDTO);

                // When & Then
                RequestValidationException exception = assertThrows(
                        RequestValidationException.class,
                        () -> spyFactory.createNewPayment(validRequestDTO)
                );
                assertEquals("Validation failed", exception.getMessage());

                // Verify that populateEntityData was never called due to validation failure
                verify(mockPayment, never()).populateEntityData(any());
            }
        }

        @Test
        @DisplayName("Should propagate validation exception from TYPE2 payment")
        void givenInvalidTYPE2Request_whenCreatingNewPayment_thenShouldPropagateValidationException() throws RequestValidationException {
            // Given
            validRequestDTO.setType("TYPE2");
            TYPE2Payment mockPayment = mock(TYPE2Payment.class);
            RequestValidationException validationException = new RequestValidationException("TYPE2 validation failed");

            doThrow(validationException).when(mockPayment).validateEntityCreationRequest(validRequestDTO);

            try (MockedStatic<PaymentType> mockedPaymentType = mockStatic(PaymentType.class)) {
                mockedPaymentType.when(() -> PaymentType.valueOf("TYPE2")).thenReturn(PaymentType.TYPE2);

                PaymentCreationFactory spyFactory = spy(paymentCreationFactory);
                doReturn(mockPayment).when(spyFactory).createPayment(validRequestDTO);

                // When & Then
                RequestValidationException exception = assertThrows(
                        RequestValidationException.class,
                        () -> spyFactory.createNewPayment(validRequestDTO)
                );
                assertEquals("TYPE2 validation failed", exception.getMessage());

                // Verify that populateEntityData was never called due to validation failure
                verify(mockPayment, never()).populateEntityData(any());
            }
        }

        @Test
        @DisplayName("Should propagate validation exception from TYPE3 payment")
        void givenInvalidTYPE3Request_whenCreatingNewPayment_thenShouldPropagateValidationException() throws RequestValidationException {
            // Given
            validRequestDTO.setType("TYPE3");
            TYPE3Payment mockPayment = mock(TYPE3Payment.class);
            RequestValidationException validationException = new RequestValidationException("TYPE3 validation failed");

            doThrow(validationException).when(mockPayment).validateEntityCreationRequest(validRequestDTO);

            try (MockedStatic<PaymentType> mockedPaymentType = mockStatic(PaymentType.class)) {
                mockedPaymentType.when(() -> PaymentType.valueOf("TYPE3")).thenReturn(PaymentType.TYPE3);

                PaymentCreationFactory spyFactory = spy(paymentCreationFactory);
                doReturn(mockPayment).when(spyFactory).createPayment(validRequestDTO);

                // When & Then
                RequestValidationException exception = assertThrows(
                        RequestValidationException.class,
                        () -> spyFactory.createNewPayment(validRequestDTO)
                );
                assertEquals("TYPE3 validation failed", exception.getMessage());

                // Verify that populateEntityData was never called due to validation failure
                verify(mockPayment, never()).populateEntityData(any());
            }
        }
    }

    @Nested
    @DisplayName("Data Population Error Tests")
    class DataPopulationErrorTests {

        @Test
        @DisplayName("Should handle runtime exception during data population")
        void givenValidRequest_whenPopulateEntityDataThrowsException_thenShouldPropagateException() throws RequestValidationException {
            // Given
            validRequestDTO.setType("TYPE1");
            TYPE1Payment mockPayment = mock(TYPE1Payment.class);
            RuntimeException populationException = new RuntimeException("Population failed");

            doNothing().when(mockPayment).validateEntityCreationRequest(validRequestDTO);
            doThrow(populationException).when(mockPayment).populateEntityData(validRequestDTO);

            try (MockedStatic<PaymentType> mockedPaymentType = mockStatic(PaymentType.class)) {
                mockedPaymentType.when(() -> PaymentType.valueOf("TYPE1")).thenReturn(PaymentType.TYPE1);

                PaymentCreationFactory spyFactory = spy(paymentCreationFactory);
                doReturn(mockPayment).when(spyFactory).createPayment(validRequestDTO);

                // When & Then
                RuntimeException exception = assertThrows(
                        RuntimeException.class,
                        () -> spyFactory.createNewPayment(validRequestDTO)
                );
                assertEquals("Population failed", exception.getMessage());

                // Verify that validation was called before population failed
                verify(mockPayment, times(1)).validateEntityCreationRequest(validRequestDTO);
                verify(mockPayment, times(1)).populateEntityData(validRequestDTO);
            }
        }
    }

    @Nested
    @DisplayName("Private Method Behavior Tests")
    class PrivateMethodBehaviorTests {

        @Test
        @DisplayName("Should create correct payment instance for each type using reflection")
        void givenDifferentPaymentTypes_whenCreatingPayment_thenShouldReturnCorrectInstances() throws Exception {
            // Given
            CreatePaymentRequestDTO type1Request = new CreatePaymentRequestDTO();
            type1Request.setType("TYPE1");

            CreatePaymentRequestDTO type2Request = new CreatePaymentRequestDTO();
            type2Request.setType("TYPE2");

            CreatePaymentRequestDTO type3Request = new CreatePaymentRequestDTO();
            type3Request.setType("TYPE3");

            // Using reflection to access private method
            java.lang.reflect.Method createPaymentMethod = PaymentCreationFactory.class
                    .getDeclaredMethod("createPayment", CreatePaymentRequestDTO.class);
            createPaymentMethod.setAccessible(true);

            // When
            Payment type1Payment = (Payment) createPaymentMethod.invoke(paymentCreationFactory, type1Request);
            Payment type2Payment = (Payment) createPaymentMethod.invoke(paymentCreationFactory, type2Request);
            Payment type3Payment = (Payment) createPaymentMethod.invoke(paymentCreationFactory, type3Request);

            // Then
            assertInstanceOf(TYPE1Payment.class, type1Payment);
            assertInstanceOf(TYPE2Payment.class, type2Payment);
            assertInstanceOf(TYPE3Payment.class, type3Payment);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should successfully create and configure TYPE1 payment with all valid data")
        void givenCompleteValidTYPE1Request_whenCreatingNewPayment_thenShouldReturnFullyConfiguredPayment() throws RequestValidationException {
            // Given
            CreatePaymentRequestDTO completeRequest = new CreatePaymentRequestDTO();
            completeRequest.setType("TYPE1");
            completeRequest.setDetails("Details");
            MoneyDTO money = new MoneyDTO();
            money.setAmount(new BigDecimal("500.50"));
            money.setCurrency("EUR");
            completeRequest.setMoney(money);
            completeRequest.setDebtor_iban("DE89370400440532013000");
            completeRequest.setCreditor_iban("FR1420041010050500013M02606");

            // When
            Payment result = paymentCreationFactory.createNewPayment(completeRequest);

            // Then
            assertNotNull(result);
            assertInstanceOf(TYPE1Payment.class, result);
            // Additional assertions would depend on the actual implementation of populateEntityData
        }

        @Test
        @DisplayName("Should handle null request DTO gracefully")
        void givenNullRequestDTO_whenCreatingNewPayment_thenShouldThrowException() {
            // Given
            CreatePaymentRequestDTO nullRequest = null;

            // When & Then
            assertThrows(
                    NullPointerException.class,
                    () -> paymentCreationFactory.createNewPayment(nullRequest)
            );
        }

        @Test
        @DisplayName("Should create different instances for multiple calls with same type")
        void givenSamePaymentType_whenCreatingMultiplePayments_thenShouldReturnDifferentInstances() throws RequestValidationException {
            // Given
            validRequestDTO.setType("TYPE1");
            validRequestDTO.setDetails("Details");

            // When
            Payment payment1 = paymentCreationFactory.createNewPayment(validRequestDTO);
            Payment payment2 = paymentCreationFactory.createNewPayment(validRequestDTO);

            // Then
            assertNotNull(payment1);
            assertNotNull(payment2);
            assertNotSame(payment1, payment2);
            assertInstanceOf(TYPE1Payment.class, payment1);
            assertInstanceOf(TYPE1Payment.class, payment2);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty string payment type")
        void givenEmptyStringPaymentType_whenCreatingNewPayment_thenShouldThrowException() {
            // Given
            validRequestDTO.setType("");

            // When & Then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> paymentCreationFactory.createNewPayment(validRequestDTO)
            );
        }

        @Test
        @DisplayName("Should handle whitespace-only payment type")
        void givenWhitespacePaymentType_whenCreatingNewPayment_thenShouldThrowException() {
            // Given
            validRequestDTO.setType("   ");

            // When & Then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> paymentCreationFactory.createNewPayment(validRequestDTO)
            );
        }

        @Test
        @DisplayName("Should handle payment type with extra characters")
        void givenPaymentTypeWithExtraCharacters_whenCreatingNewPayment_thenShouldThrowException() {
            // Given
            validRequestDTO.setType("TYPE1_EXTRA");

            // When & Then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> paymentCreationFactory.createNewPayment(validRequestDTO)
            );
        }
    }
}