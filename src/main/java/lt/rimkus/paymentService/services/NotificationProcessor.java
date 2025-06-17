package lt.rimkus.paymentService.services;

import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.models.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static lt.rimkus.paymentService.enums.PaymentType.TYPE1;
import static lt.rimkus.paymentService.enums.PaymentType.TYPE2;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.UNSUPPORTED_TYPE;

@Service
public class NotificationProcessor {
    @Autowired
    NotificationServiceOne notificationServiceOne;
    @Autowired
    NotificationServiceTwo notificationServiceTwo;

    public CompletableFuture<String> notifyServiceAboutCreatedPayment(Payment payment) throws RequestValidationException {
        if (TYPE1.getCode().equals(payment.getType())) {
            return notificationServiceOne.notifyServiceAsync("osvaldasrimkus");
        } else if (TYPE2.getCode().equals(payment.getType())) {
            return notificationServiceTwo.notifyServiceAsync("osvaldasrimkus");
        } else {
            throw new RequestValidationException(UNSUPPORTED_TYPE + payment.getType());
        }
    }
}
