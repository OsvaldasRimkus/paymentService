package lt.rimkus.paymentService.factories;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.enums.PaymentType;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.models.TYPE1Payment;
import lt.rimkus.paymentService.models.TYPE2Payment;
import lt.rimkus.paymentService.models.TYPE3Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentCreationFactory {

    public Payment createNewPayment(CreatePaymentRequestDTO requestDTO) throws RequestValidationException {
        // todo add logging
        Payment payment = createPayment(requestDTO);
        payment.validateEntityCreationRequest(requestDTO);
        payment.populateEntityData(requestDTO);
        return payment;
    }

    Payment createPayment(CreatePaymentRequestDTO requestDTO) {
        PaymentType paymentType = PaymentType.valueOf(requestDTO.getType());
        return switch (paymentType) {
            case TYPE1 -> new TYPE1Payment();
            case TYPE2 -> new TYPE2Payment();
            case TYPE3 -> new TYPE3Payment();
        };
    }
}
