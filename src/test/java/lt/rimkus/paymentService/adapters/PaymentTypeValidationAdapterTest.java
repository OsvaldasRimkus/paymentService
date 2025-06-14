package lt.rimkus.paymentService.adapters;

import lt.rimkus.paymentService.enums.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTypeValidationAdapterTest {

    private final PaymentTypeValidationAdapter adapter = new PaymentTypeValidationAdapter();

    @Test
    void validPaymentTypeShouldReturnFalse() {
        String validCode = PaymentType.TYPE1.getCode();
        assertFalse(adapter.isPaymentTypeNotValid(validCode));
    }

    @Test
    void invalidPaymentTypeShouldReturnTrue() {
        assertTrue(adapter.isPaymentTypeNotValid("INVALID_CODE"));
    }

    @Test
    void nullShouldReturnTrue() {
        assertTrue(adapter.isPaymentTypeNotValid(null));
    }
}