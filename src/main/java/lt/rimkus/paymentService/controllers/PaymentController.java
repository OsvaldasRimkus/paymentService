package lt.rimkus.paymentService.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lt.rimkus.paymentService.DTOs.CancelPaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.GetNotCancelledPaymentsDTO;
import lt.rimkus.paymentService.DTOs.PaymentCancellationInfoDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.services.GeolocationService;
import lt.rimkus.paymentService.services.PaymentService;
import lt.rimkus.paymentService.utilities.IpAddressUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static lt.rimkus.paymentService.messages.ValidationErrorMessages.PAYMENT_WITH_ID;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.WAS_CANCELLED_WITH_FEE;

@RestController
@RequestMapping("api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private GeolocationService geolocationService;

    @GetMapping
    @Operation(summary = "Retrieve all payments")
    public List<PaymentDTO> getAllPayments() {
        return paymentService.getAllPayments().stream().map(Payment::convertToDTO).collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a new payment")
    public ResponseEntity<CreatePaymentResponseDTO> createPayment(@RequestBody CreatePaymentRequestDTO newPayment, HttpServletRequest httpRequest) {

        String clientIp = IpAddressUtil.getClientIpAddress(httpRequest);
        geolocationService.logCountryAsync(clientIp, "<Payment creation>");

        CreatePaymentResponseDTO responseDTO = new CreatePaymentResponseDTO();
        responseDTO = paymentService.createPayment(newPayment, responseDTO);
        if (!responseDTO.getValidationErrors().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        }
    }

    @DeleteMapping
    @Operation(summary = "Cancel an existing payment")
    public ResponseEntity<CancelPaymentResponseDTO> cancelPayment(@RequestBody Long paymentId, HttpServletRequest httpRequest) {

        String clientIp = IpAddressUtil.getClientIpAddress(httpRequest);
        geolocationService.logCountryAsync(clientIp, "<Payment cancellation>");

        CancelPaymentResponseDTO responseDTO = paymentService.cancelPayment(paymentId);
        if (!responseDTO.getValidationErrors().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
        } else if (responseDTO.getPaymentDTO() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDTO);
        } else {
            responseDTO.setMessage(PAYMENT_WITH_ID + paymentId + WAS_CANCELLED_WITH_FEE + responseDTO.getCancellationFee().getAmount() + " " + responseDTO.getCancellationFee().getCurrency());
            return ResponseEntity.ok(responseDTO);
        }
    }

    @Operation(summary = "Get all payments that are not canceled")
    @RequestMapping(value = "querying/notCancelled", method = RequestMethod.POST)
    public ResponseEntity<List<Long>> getNotCanceledPaymentIds(@RequestBody GetNotCancelledPaymentsDTO requestDTO) {
        List<Long> responseDTO = paymentService.getNotCanceledPaymentIds(requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @Operation(summary = "Get payment cancellation details")
    @RequestMapping(value = "querying/cancellationDetails", method = RequestMethod.POST)
    public ResponseEntity<PaymentCancellationInfoDTO> getPaymentCancellationDetails(@RequestBody Long paymentId) {
        PaymentCancellationInfoDTO responseDTO = paymentService.getPaymentCancellationDetails(paymentId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

}
