package lt.rimkus.paymentService.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lt.rimkus.paymentService.models.Money;

import java.util.ArrayList;
import java.util.List;

public class CancelPaymentResponseDTO {
    private final List<String> validationErrors = new ArrayList<>();
    private String message;
    @JsonProperty("payment")
    @JsonIgnore
    private PaymentDTO paymentDTO;
    private Money cancellationFee;

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PaymentDTO getPaymentDTO() {
        return paymentDTO;
    }

    public void setPaymentDTO(PaymentDTO paymentDTO) {
        this.paymentDTO = paymentDTO;
    }

    public Money getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(Money cancellationFee) {
        this.cancellationFee = cancellationFee;
    }
}
