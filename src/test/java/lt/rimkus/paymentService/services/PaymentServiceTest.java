package lt.rimkus.paymentService.services;

import static lt.rimkus.paymentService.messages.ValidationErrorMessages.CREATION_REQUEST_NULL;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.UNSUPPORTED_TYPE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lt.rimkus.paymentService.DTOs.CancelPaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.GetNotCancelledPaymentsDTO;
import lt.rimkus.paymentService.DTOs.PaymentCancellationInfoDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.adapters.PaymentTypeValidationAdapter;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.factories.PaymentCreationFactory;
import lt.rimkus.paymentService.models.Money;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Class Tests")
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentCreationFactory paymentCreationFactory;

    @Mock
    private PaymentTypeValidationAdapter paymentTypeValidationAdapter;

    @Mock
    private PaymentCancellationService paymentCancellationService;

    @InjectMocks
    private PaymentService paymentService;

    private CreatePaymentRequestDTO requestDTO;
    private CreatePaymentResponseDTO responseDTO;
    private Payment mockPayment;
    private PaymentDTO mockPaymentDTO;
    private Money mockCancellationFee;
    private LocalDate fixedDate;
    private LocalDateTime fixedDateTime;

    @BeforeEach
    void setUp() {
        requestDTO = new CreatePaymentRequestDTO();
        responseDTO = new CreatePaymentResponseDTO();

        mockPayment = mock(Payment.class);
        mockPaymentDTO = mock(PaymentDTO.class);

        mockCancellationFee = new Money();
        mockCancellationFee.setAmount(BigDecimal.valueOf(25.50));
        mockCancellationFee.setCurrency("USD");

        fixedDate = LocalDate.of(2024, 6, 16);
        fixedDateTime = LocalDateTime.of(2024, 6, 16, 10, 30, 0);
    }

    @Test
    @DisplayName("Should return all payments correctly")
    void testGetAllPayments_ShouldReturnAllPayments() {
        // Given
        Payment payment1 = mock(Payment.class);
        Payment payment2 = mock(Payment.class);
        List<Payment> expectedPayments = Arrays.asList(payment1, payment2);
        when(paymentRepository.findAll()).thenReturn(expectedPayments);

        // When
        List<Payment> actualPayments = paymentService.getAllPayments();

        // Then
        assertEquals(expectedPayments, actualPayments);
        verify(paymentRepository).findAll();
    }

    @Test
    @DisplayName("Should return no payments correctly")
    void testGetAllPayments_WhenRepositoryReturnsEmptyList_ShouldReturnEmptyList() {
        // Given
        List<Payment> emptyList = new ArrayList<>();
        when(paymentRepository.findAll()).thenReturn(emptyList);

        // When
        List<Payment> actualPayments = paymentService.getAllPayments();

        // Then
        assertTrue(actualPayments.isEmpty());
        verify(paymentRepository).findAll();
    }

    // Tests for createPayment method - Success scenarios
    @Test
    @DisplayName("Should create payment successfully from a valid request")
    void testCreatePayment_WithValidRequest_ShouldCreatePaymentSuccessfully() throws RequestValidationException {
        // Given
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid(requestDTO.getType())).thenReturn(false);
        when(paymentCreationFactory.createNewPayment(requestDTO)).thenReturn(mockPayment);
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);

        // When
        CreatePaymentResponseDTO result = paymentService.createPayment(requestDTO, responseDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.getValidationErrors().isEmpty());
        assertEquals(mockPaymentDTO, result.getPaymentDTO());

        verify(paymentTypeValidationAdapter).isPaymentTypeNotValid(requestDTO.getType());
        verify(paymentCreationFactory).createNewPayment(requestDTO);
        verify(paymentRepository).save(mockPayment);
        verify(mockPayment).convertToDTO();
    }

    @Test
    @DisplayName("Should call repository to save payment once")
    void testCreatePayment_WithValidRequest_ShouldCallRepositorySaveOnce() throws RequestValidationException {
        // Given
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid(requestDTO.getType())).thenReturn(false);
        when(paymentCreationFactory.createNewPayment(requestDTO)).thenReturn(mockPayment);
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);

        // When
        paymentService.createPayment(requestDTO, responseDTO);

        // Then
        verify(paymentRepository, times(1)).save(mockPayment);
    }

    // Tests for createPayment method - Validation failure scenarios
    @Test
    @DisplayName("Should return an error if payment creation request is null")
    void testCreatePayment_WithNullRequest_ShouldAddValidationError() throws RequestValidationException {
        // When
        CreatePaymentResponseDTO result = paymentService.createPayment(null, responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getValidationErrors().size());
        assertEquals(CREATION_REQUEST_NULL, result.getValidationErrors().get(0));
        assertNull(result.getPaymentDTO());

        verify(paymentRepository, never()).save(any());
        verify(paymentCreationFactory, never()).createNewPayment(any());
    }

    @Test
    @DisplayName("Should return an error if creation request has an invalid payment type")
    void testCreatePayment_WithInvalidPaymentType_ShouldAddValidationError() throws RequestValidationException {
        // Given
        String invalidType = "INVALID_TYPE";
        requestDTO.setType(invalidType);
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid(invalidType)).thenReturn(true);

        // When
        CreatePaymentResponseDTO result = paymentService.createPayment(requestDTO, responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getValidationErrors().size());
        assertEquals(UNSUPPORTED_TYPE + invalidType, result.getValidationErrors().get(0));
        assertNull(result.getPaymentDTO());

        verify(paymentTypeValidationAdapter).isPaymentTypeNotValid(invalidType);
        verify(paymentRepository, never()).save(any());
        verify(paymentCreationFactory, never()).createNewPayment(any());
    }

    @Test
    @DisplayName("Should return a validation exception if payment creation fails")
    void testCreatePayment_WhenFactoryThrowsValidationException_ShouldAddValidationError() throws RequestValidationException {
        // Given
        String errorMessage = "Factory validation error";
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid(requestDTO.getType())).thenReturn(false);
        when(paymentCreationFactory.createNewPayment(requestDTO)).thenThrow(new RequestValidationException(errorMessage));

        // When
        CreatePaymentResponseDTO result = paymentService.createPayment(requestDTO, responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getValidationErrors().size());
        assertEquals(errorMessage, result.getValidationErrors().get(0));
        assertNull(result.getPaymentDTO());

        verify(paymentTypeValidationAdapter).isPaymentTypeNotValid(requestDTO.getType());
        verify(paymentCreationFactory).createNewPayment(requestDTO);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should be able to add errors to existing error in the list")
    void testCreatePayment_WithMultipleValidationErrors_ShouldAddAllErrors() {
        // Given
        responseDTO.getValidationErrors().add("Pre-existing error");
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid(requestDTO.getType())).thenReturn(true);

        // When
        CreatePaymentResponseDTO result = paymentService.createPayment(requestDTO, responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getValidationErrors().size());
        assertEquals("Pre-existing error", result.getValidationErrors().get(0));
        assertEquals(UNSUPPORTED_TYPE + requestDTO.getType(), result.getValidationErrors().get(1));
        assertNull(result.getPaymentDTO());
    }

    // Tests for edge cases and error scenarios
    @Test
    @DisplayName("Should not proceed with payment creation if there are errors")
    void testCreatePayment_WithResponseDTOHavingExistingErrors_ShouldNotProcessPayment() throws RequestValidationException {
        // Given
        responseDTO.getValidationErrors().add("Existing validation error");
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid(requestDTO.getType())).thenReturn(false);
        when(paymentCreationFactory.createNewPayment(requestDTO)).thenReturn(mockPayment);

        // When
        CreatePaymentResponseDTO result = paymentService.createPayment(requestDTO, responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getValidationErrors().size());
        assertEquals("Existing validation error", result.getValidationErrors().get(0));
        assertNull(result.getPaymentDTO());

        // Should not save payment if there are existing errors
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return the same responseDTO instance")
    void testCreatePayment_ShouldReturnSameResponseDTOInstance() throws RequestValidationException {
        // Given
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid(requestDTO.getType())).thenReturn(false);
        when(paymentCreationFactory.createNewPayment(requestDTO)).thenReturn(mockPayment);
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);

        // When
        CreatePaymentResponseDTO result = paymentService.createPayment(requestDTO, responseDTO);

        // Then
        assertSame(responseDTO, result);
    }

    // Test method interactions and call order
    @Test
    @DisplayName("Should validate payment type before creating a payment")
    void testCreatePayment_ShouldValidatePaymentTypeBeforeCreation() throws RequestValidationException {
        // Given
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid(requestDTO.getType())).thenReturn(false);
        when(paymentCreationFactory.createNewPayment(requestDTO)).thenReturn(mockPayment);
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);

        // When
        paymentService.createPayment(requestDTO, responseDTO);

        // Then - Verify order of operations
        InOrder inOrder = inOrder(paymentTypeValidationAdapter, paymentCreationFactory, paymentRepository, mockPayment);
        inOrder.verify(paymentTypeValidationAdapter).isPaymentTypeNotValid(requestDTO.getType());
        inOrder.verify(paymentCreationFactory).createNewPayment(requestDTO);
        inOrder.verify(paymentRepository).save(mockPayment);
        inOrder.verify(mockPayment).convertToDTO();
    }

    @Test
    @DisplayName("Should not check for payment type if payment creation request is null")
    void testCreatePayment_WhenNullRequestAndInvalidType_ShouldOnlyAddNullRequestError() {
        // When
        CreatePaymentResponseDTO result = paymentService.createPayment(null, responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getValidationErrors().size());
        assertEquals(CREATION_REQUEST_NULL, result.getValidationErrors().get(0));

        // Should not call payment type validation for null request
        verify(paymentTypeValidationAdapter, never()).isPaymentTypeNotValid(any());
    }

    @Test
    @DisplayName("Should not skip validation for different payment types")
    void testCreatePayment_WithDifferentPaymentTypes_ShouldNotSkipValidation() throws RequestValidationException {
        when(paymentCreationFactory.createNewPayment(requestDTO)).thenReturn(mockPayment);
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);

        // Test TYPE1
        requestDTO.setType("TYPE1");
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid("TYPE1")).thenReturn(false);
        CreatePaymentResponseDTO result1 = paymentService.createPayment(requestDTO, new CreatePaymentResponseDTO());
        assertTrue(result1.getValidationErrors().isEmpty());
        verify(paymentTypeValidationAdapter).isPaymentTypeNotValid("TYPE1");

        // Test TYPE2
        requestDTO.setType("TYPE2");
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid("TYPE2")).thenReturn(false);
        CreatePaymentResponseDTO result2 = paymentService.createPayment(requestDTO, new CreatePaymentResponseDTO());
        assertTrue(result2.getValidationErrors().isEmpty());
        verify(paymentTypeValidationAdapter).isPaymentTypeNotValid("TYPE2");

        // Test TYPE3
        requestDTO.setType("TYPE3");
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid("TYPE3")).thenReturn(true);
        CreatePaymentResponseDTO result3 = paymentService.createPayment(requestDTO, new CreatePaymentResponseDTO());
        assertFalse(result3.getValidationErrors().isEmpty());
        verify(paymentTypeValidationAdapter).isPaymentTypeNotValid("TYPE3");

        // Test TYPE_INVALID
        requestDTO.setType("TYPE_INVALID");
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid("TYPE_INVALID")).thenReturn(true);
        CreatePaymentResponseDTO result4 = paymentService.createPayment(requestDTO, new CreatePaymentResponseDTO());
        assertFalse(result4.getValidationErrors().isEmpty());
        verify(paymentTypeValidationAdapter).isPaymentTypeNotValid("TYPE_INVALID");
    }

    @Test
    @DisplayName("Should not throw an exception if service returns a created payment and save it")
    void testCreatePayment_AssertionBehavior_NewPaymentNotNull() throws RequestValidationException {
        // Given
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid(requestDTO.getType())).thenReturn(false);
        when(paymentCreationFactory.createNewPayment(requestDTO)).thenReturn(mockPayment);
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);

        // When - This should not throw AssertionError since mockPayment is not null
        assertDoesNotThrow(() -> {
            paymentService.createPayment(requestDTO, responseDTO);
        });

        // Then
        verify(paymentRepository).save(mockPayment);
    }

    @Test
    @DisplayName("Concurrent calls should be handled independently")
    void testCreatePayment_MultipleConcurrentCalls_ShouldHandleIndependently() throws RequestValidationException {
        // Given
        CreatePaymentRequestDTO request1 = new CreatePaymentRequestDTO();
        request1.setType("TYPE1");
        CreatePaymentResponseDTO response1 = new CreatePaymentResponseDTO();

        CreatePaymentRequestDTO request2 = new CreatePaymentRequestDTO();
        request2.setType("TYPE_INVALID");
        CreatePaymentResponseDTO response2 = new CreatePaymentResponseDTO();

        when(paymentTypeValidationAdapter.isPaymentTypeNotValid("TYPE1")).thenReturn(false);
        when(paymentTypeValidationAdapter.isPaymentTypeNotValid("TYPE_INVALID")).thenReturn(true);
        when(paymentCreationFactory.createNewPayment(request1)).thenReturn(mockPayment);
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);

        // When
        CreatePaymentResponseDTO result1 = paymentService.createPayment(request1, response1);
        CreatePaymentResponseDTO result2 = paymentService.createPayment(request2, response2);

        // Then
        assertTrue(result1.getValidationErrors().isEmpty());
        assertFalse(result2.getValidationErrors().isEmpty());
        assertNotSame(result1, result2);
    }

    @Test
    @DisplayName("Should successfully cancel payment when payment exists")
    void testCancelPayment_Success() throws RequestValidationException {
        // Given
        long paymentId = 123L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPayment));
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);
        when(mockPayment.getCancellationFee()).thenReturn(mockCancellationFee);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // When
            CancelPaymentResponseDTO result = paymentService.cancelPayment(paymentId);

            // Then
            assertNotNull(result);
            assertEquals(mockPaymentDTO, result.getPaymentDTO());
            assertNotNull(result.getCancellationFee());
            assertEquals(BigDecimal.valueOf(25.50), result.getCancellationFee().getAmount());
            assertEquals("USD", result.getCancellationFee().getCurrency());
            assertTrue(result.getValidationErrors().isEmpty());

            // Verify interactions
            verify(paymentRepository).findById(paymentId);
            verify(paymentCancellationService).preparePaymentForCancellation(mockPayment, fixedDate, fixedDateTime);
            verify(paymentRepository).save(mockPayment);
            verify(mockPayment).convertToDTO();
            verify(mockPayment, times(2)).getCancellationFee(); // Called twice for amount and currency
        }
    }

    @Test
    @DisplayName("Should return validation error when payment does not exist")
    void testCancelPayment_PaymentNotFound() throws RequestValidationException {
        // Given
        long paymentId = 999L;
        String expectedErrorMessage = "Provided payment id does not exist";

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // When
            CancelPaymentResponseDTO result = paymentService.cancelPayment(paymentId);

            // Then
            assertNotNull(result);
            assertNull(result.getPaymentDTO());
            assertNull(result.getCancellationFee());
            assertFalse(result.getValidationErrors().isEmpty());
            assertTrue(result.getValidationErrors().contains(expectedErrorMessage));

            // Verify interactions
            verify(paymentRepository).findById(paymentId);
            verify(paymentCancellationService, never()).preparePaymentForCancellation(any(), any(), any());
            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should handle RequestValidationException from cancellation service")
    void testCancelPayment_CancellationServiceThrowsException() throws RequestValidationException {
        // Given
        long paymentId = 123L;
        String errorMessage = "Payment cannot be cancelled";
        RequestValidationException exception = new RequestValidationException(errorMessage);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPayment));
        doThrow(exception).when(paymentCancellationService)
                .preparePaymentForCancellation(any(Payment.class), any(LocalDate.class), any(LocalDateTime.class));

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // When
            CancelPaymentResponseDTO result = paymentService.cancelPayment(paymentId);

            // Then
            assertNotNull(result);
            assertNull(result.getPaymentDTO());
            assertNull(result.getCancellationFee());
            assertFalse(result.getValidationErrors().isEmpty());
            assertTrue(result.getValidationErrors().contains(errorMessage));

            // Verify interactions
            verify(paymentRepository).findById(paymentId);
            verify(paymentCancellationService).preparePaymentForCancellation(mockPayment, fixedDate, fixedDateTime);
            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should handle null cancellation fee gracefully")
    void testCancelPayment_NullCancellationFee() throws RequestValidationException {
        // Given
        long paymentId = 123L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPayment));
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);
        when(mockPayment.getCancellationFee()).thenReturn(null);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                paymentService.cancelPayment(paymentId);
            });

            // Verify interactions up to the point of failure
            verify(paymentRepository).findById(paymentId);
            verify(paymentCancellationService).preparePaymentForCancellation(mockPayment, fixedDate, fixedDateTime);
            verify(paymentRepository).save(mockPayment);
        }
    }

    @Test
    @DisplayName("Should create new CancelPaymentResponseDTO with empty validation errors")
    void testCancelPayment_ResponseDTOInitialization() {
        // Given
        long paymentId = 123L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPayment));
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);
        when(mockPayment.getCancellationFee()).thenReturn(mockCancellationFee);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // When
            CancelPaymentResponseDTO result = paymentService.cancelPayment(paymentId);

            // Then
            assertNotNull(result);
            assertNotNull(result.getValidationErrors());
            assertTrue(result.getValidationErrors().isEmpty());
        }
    }

    @Test
    @DisplayName("Should use current date and time for cancellation")
    void testCancelPayment_UsesCurrentDateTime() throws RequestValidationException {
        // Given
        long paymentId = 123L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPayment));
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);
        when(mockPayment.getCancellationFee()).thenReturn(mockCancellationFee);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // When
            paymentService.cancelPayment(paymentId);

            // Then
            mockedLocalDate.verify(LocalDate::now);
            mockedLocalDateTime.verify(LocalDateTime::now);
            verify(paymentCancellationService).preparePaymentForCancellation(mockPayment, fixedDate, fixedDateTime);
        }
    }

    @Test
    @DisplayName("Should copy cancellation fee amount and currency correctly")
    void testCancelPayment_CancellationFeeCopy() {
        // Given
        long paymentId = 123L;
        Money originalFee = new Money();
        originalFee.setAmount(BigDecimal.valueOf(15.75));
        originalFee.setCurrency("EUR");

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mockPayment));
        when(mockPayment.convertToDTO()).thenReturn(mockPaymentDTO);
        when(mockPayment.getCancellationFee()).thenReturn(originalFee);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // When
            CancelPaymentResponseDTO result = paymentService.cancelPayment(paymentId);

            // Then
            assertNotNull(result.getCancellationFee());
            assertEquals(BigDecimal.valueOf(15.75), result.getCancellationFee().getAmount());
            assertEquals("EUR", result.getCancellationFee().getCurrency());

            // Verify the original fee object is not the same as the response fee object
            assertNotSame(originalFee, result.getCancellationFee());
        }
    }

    @Test
    @DisplayName("Should return all not-cancelled payment IDs when filter is disabled")
    void shouldReturnAllNotCancelledIdsWhenFilterIsOff() {
        // Given
        GetNotCancelledPaymentsDTO dto = new GetNotCancelledPaymentsDTO();
        dto.setFilter(false); // filter off
        dto.setMinAmount(new BigDecimal("10.00"));
        dto.setMaxAmount(new BigDecimal("100.00"));

        List<Long> expectedIds = List.of(1L, 2L, 3L);
        when(paymentRepository.getNotCancelledPaymentsWithinRange(null, null)).thenReturn(expectedIds);

        // When
        List<Long> result = paymentService.getNotCanceledPaymentIds(dto);

        // Then
        assertThat(result).isEqualTo(expectedIds);
        verify(paymentRepository).getNotCancelledPaymentsWithinRange(null, null);
    }

    @Test
    @DisplayName("Should return filtered not-cancelled payment IDs when filter is enabled")
    void shouldReturnFilteredIdsWhenFilterIsOn() {
        // Given
        GetNotCancelledPaymentsDTO dto = new GetNotCancelledPaymentsDTO();
        dto.setFilter(true);
        dto.setMinAmount(new BigDecimal("20.00"));
        dto.setMaxAmount(new BigDecimal("80.00"));

        List<Long> expectedIds = List.of(2L, 4L);
        when(paymentRepository.getNotCancelledPaymentsWithinRange(
                new BigDecimal("20.00"), new BigDecimal("80.00"))).thenReturn(expectedIds);

        // When
        List<Long> result = paymentService.getNotCanceledPaymentIds(dto);

        // Then
        assertThat(result).isEqualTo(expectedIds);
        verify(paymentRepository).getNotCancelledPaymentsWithinRange(new BigDecimal("20.00"), new BigDecimal("80.00"));
    }

    @Test
    @DisplayName("Should return cancellation details for a given payment ID")
    void shouldReturnPaymentCancellationDetails() {
        // Given
        Long paymentId = 10L;
        PaymentCancellationInfoDTO expectedDTO = new PaymentCancellationInfoDTO(paymentId, new Money(new BigDecimal("3.00"), "EUR"));

        when(paymentRepository.getPaymentCancellationDetails(paymentId)).thenReturn(expectedDTO);

        // When
        PaymentCancellationInfoDTO result = paymentService.getPaymentCancellationDetails(paymentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(paymentId);
        assertThat(result.getCancellationFee().getAmount()).isEqualByComparingTo("3.00");
        assertEquals(3, result.getCancellationFee().getCurrency().length());
        assertThat(result.getCancellationFee().getCurrency()).contains("EUR");
        verify(paymentRepository).getPaymentCancellationDetails(paymentId);
    }

}