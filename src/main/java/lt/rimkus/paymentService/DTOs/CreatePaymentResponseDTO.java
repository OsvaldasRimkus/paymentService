package lt.rimkus.paymentService.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CreatePaymentResponseDTO {
    private final List<String> validationErrors = new ArrayList<>();
    @JsonProperty("payment")
    private PaymentDTO paymentDTO;

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public PaymentDTO getPaymentDTO() {
        return paymentDTO;
    }

    public void setPaymentDTO(PaymentDTO paymentDTO) {
        this.paymentDTO = paymentDTO;
    }
}
