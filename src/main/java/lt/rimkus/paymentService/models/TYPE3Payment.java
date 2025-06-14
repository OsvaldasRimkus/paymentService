package lt.rimkus.paymentService.models;

import jakarta.persistence.Entity;
import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TYPE3PaymentDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;

import static lt.rimkus.paymentService.messages.ValidationErrorMessages.CREDITOR_BANK_BIC_MANDATORY_FOR_TYPE3;

@Entity
public class TYPE3Payment extends Payment {
    private String creditorBankBIC;

    @Override
    public void validateTypeSpecificRequirements(CreatePaymentRequestDTO requestDTO) throws RequestValidationException {
        if (requestDTO.getCreditorBankBIC() == null || StringUtils.isEmpty(requestDTO.getCreditorBankBIC().trim())) {
            throw new RequestValidationException(CREDITOR_BANK_BIC_MANDATORY_FOR_TYPE3);
        }
    }

    @Override
    public void populateEntityData(CreatePaymentRequestDTO requestDTO) {
        super.populateCommonData(requestDTO);
        this.setCreditorBankBIC(requestDTO.getCreditorBankBIC());
    }

    @Override
    public PaymentDTO convertToDTO() {
        TYPE3PaymentDTO dto = new TYPE3PaymentDTO();
        super.populateCommonDTOData(dto);
        dto.setCreditorBankBIC(this.getCreditorBankBIC());
        return dto;
    }

    public String getCreditorBankBIC() {
        return creditorBankBIC;
    }

    public void setCreditorBankBIC(String creditorBankBIC) {
        this.creditorBankBIC = creditorBankBIC;
    }
}
