package lt.rimkus.paymentService.adapters;

import lt.rimkus.paymentService.utilities.PaymentTypeValidationUtils;
import org.springframework.stereotype.Service;

/**
 * This adapter is used to help with Payment Service testing to avoid mocking a static method from utility class
 */
@Service
public class PaymentTypeValidationAdapter {

    public boolean isPaymentTypeNotValid(String paymentTypeCode) {
        return PaymentTypeValidationUtils.isPaymentTypeNotValid(paymentTypeCode);
    }
}
