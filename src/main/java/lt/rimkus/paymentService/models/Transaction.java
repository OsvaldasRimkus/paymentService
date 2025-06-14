package lt.rimkus.paymentService.models;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;

public interface Transaction {
    void validateEntityCreationRequest(CreatePaymentRequestDTO requestDTO) throws RequestValidationException;
    void validateTypeSpecificRequirements(CreatePaymentRequestDTO requestDTO) throws RequestValidationException;
    void populateEntityData(CreatePaymentRequestDTO requestDTO);
    PaymentDTO convertToDTO();
}
