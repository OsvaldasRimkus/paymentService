package lt.rimkus.paymentService.models;

import jakarta.persistence.Entity;
import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TYPE2PaymentDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;

@Entity
public class TYPE2Payment extends Payment {
    private String details;

    @Override
    public void validateTypeSpecificRequirements(CreatePaymentRequestDTO requestDTO) {
        //currency is validated in parent class, details are optional
    }

    @Override
    public void populateEntityData(CreatePaymentRequestDTO requestDTO) {
        super.populateCommonData(requestDTO);
        this.setDetails(requestDTO.getDetails());
    }

    @Override
    public PaymentDTO convertToDTO() {
        TYPE2PaymentDTO dto = new TYPE2PaymentDTO();
        super.populateCommonDTOData(dto);
        dto.setDetails(this.getDetails());
        return dto;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
