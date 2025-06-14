package lt.rimkus.paymentService.services;

import static lt.rimkus.paymentService.messages.ValidationErrorMessages.CREATION_REQUEST_NULL;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.UNSUPPORTED_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.adapters.PaymentTypeValidationAdapter;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.factories.PaymentCreationFactory;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentCreationFactory paymentCreationFactory;

    @Mock
    private PaymentTypeValidationAdapter paymentTypeValidationAdapter;

    @InjectMocks
    private PaymentService paymentService;

    private CreatePaymentRequestDTO requestDTO;
    private CreatePaymentResponseDTO responseDTO;
    private Payment mockPayment;
    private PaymentDTO mockPaymentDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new CreatePaymentRequestDTO();
        responseDTO = new CreatePaymentResponseDTO();

        mockPayment = mock(Payment.class);
        mockPaymentDTO = mock(PaymentDTO.class);
    }

    // Tests for getAllPayments method
    @Test
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

    // Test with different payment types
    @Test
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

    // Test assertion behavior
    @Test
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

    // Test thread safety considerations (if applicable)
    @Test
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
}