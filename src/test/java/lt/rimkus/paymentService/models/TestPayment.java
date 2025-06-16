package lt.rimkus.paymentService.models;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TestPaymentDTO;

/**
 * Concrete test implementation of the abstract Payment class
 */
public class TestPayment extends Payment {

    @Override
    public void validateTypeSpecificRequirements(CreatePaymentRequestDTO requestDTO) {
    }

    @Override
    public void populateEntityData(CreatePaymentRequestDTO requestDTO) {
        super.populateCommonData(requestDTO);
    }

    @Override
    public PaymentDTO convertToDTO() {
        return new TestPaymentDTO();
    }
}
