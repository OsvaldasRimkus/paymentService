package lt.rimkus.paymentService.services;

import static lt.rimkus.paymentService.enums.PaymentType.TYPE1;
import static lt.rimkus.paymentService.enums.PaymentType.TYPE2;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.UNSUPPORTED_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.models.TestPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationProcessor Tests")
class NotificationProcessorTest {

    @Mock
    private NotificationServiceOne notificationServiceOne;

    @Mock
    private NotificationServiceTwo notificationServiceTwo;

    @InjectMocks
    private NotificationProcessor notificationProcessor;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new TestPayment();
    }

    @Test
    @DisplayName("Should notify service one when payment type is TYPE1")
    void shouldNotifyServiceOneWhenPaymentTypeIsType1() throws RequestValidationException, ExecutionException, InterruptedException {
        // Given
        payment.setType(TYPE1.getCode());
        CompletableFuture<String> expectedResult = CompletableFuture.completedFuture("Success from service one");
        when(notificationServiceOne.notifyServiceAsync("osvaldasrimkus")).thenReturn(expectedResult);

        // When
        CompletableFuture<String> result = notificationProcessor.notifyServiceAboutCreatedPayment(payment);

        // Then
        assertNotNull(result);
        assertEquals("Success from service one", result.get());
        verify(notificationServiceOne).notifyServiceAsync("osvaldasrimkus");
        verify(notificationServiceTwo, never()).notifyServiceAsync(anyString());
    }

    @Test
    @DisplayName("Should notify service two when payment type is TYPE2")
    void shouldNotifyServiceTwoWhenPaymentTypeIsType2() throws RequestValidationException, ExecutionException, InterruptedException {
        // Given
        payment.setType(TYPE2.getCode());
        CompletableFuture<String> expectedResult = CompletableFuture.completedFuture("Success from service two");
        when(notificationServiceTwo.notifyServiceAsync("osvaldasrimkus")).thenReturn(expectedResult);

        // When
        CompletableFuture<String> result = notificationProcessor.notifyServiceAboutCreatedPayment(payment);

        // Then
        assertNotNull(result);
        assertEquals("Success from service two", result.get());
        verify(notificationServiceTwo).notifyServiceAsync("osvaldasrimkus");
        verify(notificationServiceOne, never()).notifyServiceAsync(anyString());
    }

    @Test
    @DisplayName("Should throw RequestValidationException when payment type is not supported")
    void shouldThrowRequestValidationExceptionWhenPaymentTypeIsUnsupported() {
        // Given
        String unsupportedType = "UNSUPPORTED_TYPE";
        payment.setType(unsupportedType);

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> notificationProcessor.notifyServiceAboutCreatedPayment(payment)
        );

        assertEquals(UNSUPPORTED_TYPE + unsupportedType, exception.getMessage());
        verify(notificationServiceOne, never()).notifyServiceAsync(anyString());
        verify(notificationServiceTwo, never()).notifyServiceAsync(anyString());
    }

    @Test
    @DisplayName("Should throw RequestValidationException when payment type is empty string")
    void shouldThrowRequestValidationExceptionWhenPaymentTypeIsEmptyString() {
        // Given
        payment.setType("");

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> notificationProcessor.notifyServiceAboutCreatedPayment(payment)
        );

        assertEquals(UNSUPPORTED_TYPE, exception.getMessage());
        verify(notificationServiceOne, never()).notifyServiceAsync(anyString());
        verify(notificationServiceTwo, never()).notifyServiceAsync(anyString());
    }

    @Test
    @DisplayName("Should propagate exception when service one fails")
    void shouldPropagateExceptionWhenServiceOneFails() throws RequestValidationException {
        // Given
        payment.setType(TYPE1.getCode());
        RuntimeException serviceException = new RuntimeException("Service one failed");
        CompletableFuture<String> failedFuture = CompletableFuture.failedFuture(serviceException);
        when(notificationServiceOne.notifyServiceAsync("osvaldasrimkus")).thenReturn(failedFuture);

        // When
        CompletableFuture<String> result = notificationProcessor.notifyServiceAboutCreatedPayment(payment);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());

        ExecutionException executionException = assertThrows(
                ExecutionException.class,
                result::get
        );
        assertEquals("Service one failed", executionException.getCause().getMessage());

        verify(notificationServiceOne).notifyServiceAsync("osvaldasrimkus");
        verify(notificationServiceTwo, never()).notifyServiceAsync(anyString());
    }

    @Test
    @DisplayName("Should propagate exception when service two fails")
    void shouldPropagateExceptionWhenServiceTwoFails() throws RequestValidationException {
        // Given
        payment.setType(TYPE2.getCode());
        RuntimeException serviceException = new RuntimeException("Service two failed");
        CompletableFuture<String> failedFuture = CompletableFuture.failedFuture(serviceException);
        when(notificationServiceTwo.notifyServiceAsync("osvaldasrimkus")).thenReturn(failedFuture);

        // When
        CompletableFuture<String> result = notificationProcessor.notifyServiceAboutCreatedPayment(payment);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());

        ExecutionException executionException = assertThrows(
                ExecutionException.class,
                result::get
        );
        assertEquals("Service two failed", executionException.getCause().getMessage());

        verify(notificationServiceTwo).notifyServiceAsync("osvaldasrimkus");
        verify(notificationServiceOne, never()).notifyServiceAsync(anyString());
    }

    @Test
    @DisplayName("Should handle concurrent execution correctly for TYPE1")
    void shouldHandleConcurrentExecutionCorrectlyForType1() throws RequestValidationException, ExecutionException, InterruptedException {
        // Given
        payment.setType(TYPE1.getCode());
        CompletableFuture<String> asyncResult = new CompletableFuture<>();
        when(notificationServiceOne.notifyServiceAsync("osvaldasrimkus")).thenReturn(asyncResult);

        // When
        CompletableFuture<String> result = notificationProcessor.notifyServiceAboutCreatedPayment(payment);

        // Then
        assertFalse(result.isDone());

        // Complete the async operation
        asyncResult.complete("Async success");

        assertEquals("Async success", result.get());
        verify(notificationServiceOne).notifyServiceAsync("osvaldasrimkus");
    }

    @Test
    @DisplayName("Should handle concurrent execution correctly for TYPE2")
    void shouldHandleConcurrentExecutionCorrectlyForType2() throws RequestValidationException, ExecutionException, InterruptedException {
        // Given
        payment.setType(TYPE2.getCode());
        CompletableFuture<String> asyncResult = new CompletableFuture<>();
        when(notificationServiceTwo.notifyServiceAsync("osvaldasrimkus")).thenReturn(asyncResult);

        // When
        CompletableFuture<String> result = notificationProcessor.notifyServiceAboutCreatedPayment(payment);

        // Then
        assertFalse(result.isDone());

        // Complete the async operation
        asyncResult.complete("Async success");

        assertEquals("Async success", result.get());
        verify(notificationServiceTwo).notifyServiceAsync("osvaldasrimkus");
    }
}