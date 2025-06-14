package lt.rimkus.paymentService.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Retrieve all payments")
    public List<PaymentDTO> getAllPayments() {
        return paymentService.getAllPayments().stream().map(Payment::convertToDTO).collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a new payment")
    public ResponseEntity<CreatePaymentResponseDTO> createPayment(@RequestBody CreatePaymentRequestDTO newPayment) {
        CreatePaymentResponseDTO responseDTO = new CreatePaymentResponseDTO();
        responseDTO = paymentService.createPayment(newPayment, responseDTO);
        if (!responseDTO.getValidationErrors().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        }
    }
}
