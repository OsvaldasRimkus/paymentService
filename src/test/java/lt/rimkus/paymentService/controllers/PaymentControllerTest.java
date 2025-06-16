package lt.rimkus.paymentService.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lt.rimkus.paymentService.DTOs.CancelPaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.GetNotCancelledPaymentsDTO;
import lt.rimkus.paymentService.DTOs.MoneyDTO;
import lt.rimkus.paymentService.DTOs.PaymentCancellationInfoDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TestPaymentDTO;
import lt.rimkus.paymentService.models.Money;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.models.TestPayment;
import lt.rimkus.paymentService.services.GeolocationService;
import lt.rimkus.paymentService.services.PaymentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private GeolocationService geolocationService;

    @InjectMocks
    private PaymentController paymentController;

    private final Long paymentId = 123L;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Should return payments")
    void testGetAllPayments_returnsMappedDTOs() {
        // Given
        Payment mockPayment = new TestPayment() {
            @Override
            public PaymentDTO convertToDTO() {
                PaymentDTO dto = new TestPaymentDTO();
                dto.setId(1L);
                dto.setType("TYPE1");
                MoneyDTO money = new MoneyDTO();
                money.setAmount(new BigDecimal("100.00"));
                money.setCurrency("EUR");
                dto.setMoney(money);
                dto.setDebtor_iban("DE123");
                dto.setCreditor_iban("FR456");
                return dto;
            }
        };

        // When
        when(paymentService.getAllPayments()).thenReturn(List.of(mockPayment));
        List<PaymentDTO> result = paymentController.getAllPayments();

        // Then
        assertEquals(1, result.size());
        assertEquals("TYPE1", result.get(0).getType());
        assertEquals("EUR", result.get(0).getMoney().getCurrency());
    }

    @Test
    @DisplayName("Should return correct response when payment is created")
    void testCreatePayment_validRequest_returnsCreated() {
        // Given
        CreatePaymentRequestDTO requestDTO = new CreatePaymentRequestDTO();
        requestDTO.setType("TYPE1");
        MoneyDTO money = new MoneyDTO();
        money.setAmount(new BigDecimal("200.00"));
        money.setCurrency("USD");
        requestDTO.setMoney(money);
        requestDTO.setDebtor_iban("DE789");
        requestDTO.setCreditor_iban("FR321");

        CreatePaymentResponseDTO responseDTO = new CreatePaymentResponseDTO();
        PaymentDTO paymentDTO = new TestPaymentDTO();
        paymentDTO.setId(2L);
        paymentDTO.setMoney(money);
        paymentDTO.setType("TYPE1");
        paymentDTO.setDebtor_iban("DE789");
        paymentDTO.setCreditor_iban("FR321");
        responseDTO.setPaymentDTO(paymentDTO);
        HttpServletRequest httpRequest = new MockHttpServletRequest();

        // When
        when(paymentService.createPayment(eq(requestDTO), any())).thenReturn(responseDTO);
        ResponseEntity<CreatePaymentResponseDTO> result = paymentController.createPayment(requestDTO, httpRequest);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        assertEquals(2L, result.getBody().getPaymentDTO().getId());
        assertTrue(result.getBody().getValidationErrors().isEmpty());
    }

    @Test
    @DisplayName("Should return Bad Request when invalid request is received for payment creation")
    void testCreatePayment_invalidRequest_returnsBadRequest() {
        // Given
        CreatePaymentRequestDTO requestDTO = new CreatePaymentRequestDTO();
        requestDTO.setType("INVALID");

        CreatePaymentResponseDTO responseDTO = new CreatePaymentResponseDTO();
        responseDTO.getValidationErrors().add("Unsupported type: INVALID");
        HttpServletRequest httpRequest = new MockHttpServletRequest();

        // When
        when(paymentService.createPayment(eq(requestDTO), any())).thenReturn(responseDTO);
        ResponseEntity<CreatePaymentResponseDTO> result = paymentController.createPayment(requestDTO, httpRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        assertFalse(result.getBody().getValidationErrors().isEmpty());
        assertEquals("Unsupported type: INVALID", result.getBody().getValidationErrors().get(0));
    }

    @Test
    @DisplayName("Should return Bad Request when invalid request is received for payment cancellation")
    void testDeletePayment_invalidRequest_shouldReturnBadRequest() {
        // Given
        CancelPaymentResponseDTO responseDTO = new CancelPaymentResponseDTO();
        responseDTO.getValidationErrors().add("Invalid request");
        given(paymentService.cancelPayment(paymentId)).willReturn(responseDTO);
        HttpServletRequest httpRequest = new MockHttpServletRequest();

        // When
        ResponseEntity<CancelPaymentResponseDTO> response = paymentController.cancelPayment(paymentId, httpRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
    }

    @Test
    @DisplayName("Should return Not Found if no payment is found based on id")
    void testDeletePayment_paymentDTOIsNull_shouldReturnNotFound() {
        // Given
        CancelPaymentResponseDTO responseDTO = new CancelPaymentResponseDTO();
        given(paymentService.cancelPayment(paymentId)).willReturn(responseDTO);
        HttpServletRequest httpRequest = new MockHttpServletRequest();

        // When
        ResponseEntity<CancelPaymentResponseDTO> response = paymentController.cancelPayment(paymentId, httpRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
    }

    @Test
    @DisplayName("Should return a correct response when payment cancellation is successful")
    void testDeletePayment_cancellationIsSuccessful_shouldReturnOkWhen() {
        // Given
        CancelPaymentResponseDTO responseDTO = new CancelPaymentResponseDTO();
        responseDTO.setPaymentDTO(new TestPaymentDTO());

        Money fee = new Money();
        fee.setAmount(new BigDecimal("5.00"));
        fee.setCurrency("EUR");
        responseDTO.setCancellationFee(fee);

        given(paymentService.cancelPayment(paymentId)).willReturn(responseDTO);
        HttpServletRequest httpRequest = new MockHttpServletRequest();

        // When
        ResponseEntity<CancelPaymentResponseDTO> response = paymentController.cancelPayment(paymentId, httpRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        assertTrue(responseDTO.getMessage().contains("was successfully cancelled. Cancellation fee is:"));
    }

    @Test
    @DisplayName("Should correctly return cancelled payment IDs")
    void shouldReturnNotCancelledPaymentIds_givenValidRange() {
        // Given
        GetNotCancelledPaymentsDTO requestDTO = new GetNotCancelledPaymentsDTO();
        requestDTO.setMinAmount(new BigDecimal("10.00"));
        requestDTO.setMaxAmount(new BigDecimal("50.00"));

        List<Long> expected = List.of(1L, 2L);
        Mockito.when(paymentService.getNotCanceledPaymentIds(requestDTO)).thenReturn(expected);

        // When
        ResponseEntity<List<Long>> response = paymentController.getNotCanceledPaymentIds(requestDTO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(1L));
        assertTrue(response.getBody().contains(2L));
    }

    @Test
    @DisplayName("Should correctly return payment cancellation details")
    void shouldReturnCancellationDetails_givenValidId() {
        // Given
        Long paymentId = 5L;
        PaymentCancellationInfoDTO dto = new PaymentCancellationInfoDTO(paymentId, new Money(new BigDecimal("3.75"), "EUR"));

        Mockito.when(paymentService.getPaymentCancellationDetails(paymentId)).thenReturn(dto);

        // When
        ResponseEntity<PaymentCancellationInfoDTO> response = paymentController.getPaymentCancellationDetails(paymentId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getId()).isEqualTo(paymentId);
        assertThat(response.getBody().getCancellationFee().getAmount()).isEqualByComparingTo("3.75");
    }
}