package lt.rimkus.paymentService.models;

import jakarta.persistence.Entity;
import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TYPE1PaymentDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;

import static lt.rimkus.paymentService.messages.ValidationErrorMessages.DETAILS_MANDATORY_FOR_TYPE1;

@Entity
public class TYPE1Payment extends Payment {
    private String details;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public void validateTypeSpecificRequirements(CreatePaymentRequestDTO requestDTO) throws RequestValidationException {
        if (requestDTO.getDetails() == null || StringUtils.isEmpty(requestDTO.getDetails().trim())) {
            throw new RequestValidationException(DETAILS_MANDATORY_FOR_TYPE1);
        }
    }

    @Override
    public void populateEntityData(CreatePaymentRequestDTO requestDTO) {
        super.populateCommonData(requestDTO);
        this.setDetails(requestDTO.getDetails());
    }

    @Override
    public PaymentDTO convertToDTO() {
        TYPE1PaymentDTO dto = new TYPE1PaymentDTO();
        super.populateCommonDTOData(dto);
        dto.setDetails(this.getDetails());
        return dto;
    }
}
