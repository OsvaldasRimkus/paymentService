package lt.rimkus.paymentService.controllers;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.MoneyDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TestPaymentDTO;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.models.TestPayment;
import lt.rimkus.paymentService.services.PaymentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testGetAllPayments_returnsMappedDTOs() {
        // Arrange
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

        when(paymentService.getAllPayments()).thenReturn(List.of(mockPayment));

        // Act
        List<PaymentDTO> result = paymentController.getAllPayments();

        // Assert
        assertEquals(1, result.size());
        assertEquals("TYPE1", result.get(0).getType());
        assertEquals("EUR", result.get(0).getMoney().getCurrency());
    }

    @Test
    void testCreatePayment_validRequest_returnsCreated() {
        // Arrange
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

        when(paymentService.createPayment(eq(requestDTO), any())).thenReturn(responseDTO);

        // Act
        ResponseEntity<CreatePaymentResponseDTO> result = paymentController.createPayment(requestDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        assertEquals(2L, result.getBody().getPaymentDTO().getId());
        assertTrue(result.getBody().getValidationErrors().isEmpty());
    }

    @Test
    void testCreatePayment_invalidRequest_returnsBadRequest() {
        // Arrange
        CreatePaymentRequestDTO requestDTO = new CreatePaymentRequestDTO();
        requestDTO.setType("INVALID");

        CreatePaymentResponseDTO responseDTO = new CreatePaymentResponseDTO();
        responseDTO.getValidationErrors().add("Unsupported type: INVALID");

        when(paymentService.createPayment(eq(requestDTO), any())).thenReturn(responseDTO);

        // Act
        ResponseEntity<CreatePaymentResponseDTO> result = paymentController.createPayment(requestDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        assertFalse(result.getBody().getValidationErrors().isEmpty());
        assertEquals("Unsupported type: INVALID", result.getBody().getValidationErrors().get(0));
    }
}