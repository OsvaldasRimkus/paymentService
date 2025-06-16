package lt.rimkus.paymentService.services;

import lt.rimkus.paymentService.enums.Currency;
import lt.rimkus.paymentService.enums.PaymentType;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.models.Money;
import lt.rimkus.paymentService.models.Payment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static lt.rimkus.paymentService.messages.OtherMessages.HOURS_CANNOT_BE_NEGATIVE;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.IS_ALREADY_CANCELED;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.NO_DATA_FOR_PAYMENT_TYPE;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.PAYMENT_WITH_ID;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.SAME_DAY_CANCELLATION_ONLY;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.TYPE_MANDATORY;

@Service
public class PaymentCancellationService {

    private static final Map<String, BigDecimal> PAYMENT_TYPE_AND_CANCELLATION_COEFFICIENT_MAP =
            Map.of(
                    PaymentType.TYPE1.getCode(), BigDecimal.valueOf(0.05),
                    PaymentType.TYPE2.getCode(), BigDecimal.valueOf(0.1),
                    PaymentType.TYPE3.getCode(), BigDecimal.valueOf(0.15)
            );


    public void preparePaymentForCancellation(Payment paymentToCancel, LocalDate dateOfCancellationRequest, LocalDateTime timeOfCancellationRequest) throws RequestValidationException {
        if (paymentToCancel.isCancelled()) {
            throw new RequestValidationException(PAYMENT_WITH_ID + paymentToCancel.getId() + IS_ALREADY_CANCELED);
        }

        if (!dateOfCancellationRequest.isEqual(paymentToCancel.getCreatedDate())) {
            throw new RequestValidationException(SAME_DAY_CANCELLATION_ONLY);
        }

        long numberOfHours = Duration.between(paymentToCancel.getCreatedAt(), timeOfCancellationRequest).getSeconds() / 3600;
        BigDecimal cancellationFee = calculateCancellationFee(paymentToCancel.getType(), numberOfHours);
        updatePaymentData(paymentToCancel, timeOfCancellationRequest, cancellationFee);
    }

    public BigDecimal calculateCancellationFee(String paymentType, long hours) throws RequestValidationException {
        if (StringUtils.isEmpty(paymentType)) {
            throw new RequestValidationException(TYPE_MANDATORY);
        }
        if (hours < 0) {
            throw new RequestValidationException(HOURS_CANNOT_BE_NEGATIVE);
        }
        if (!PAYMENT_TYPE_AND_CANCELLATION_COEFFICIENT_MAP.containsKey(paymentType)) {
            throw new RequestValidationException(NO_DATA_FOR_PAYMENT_TYPE + paymentType);
        } else {
            BigDecimal coefficient = PAYMENT_TYPE_AND_CANCELLATION_COEFFICIENT_MAP.get(paymentType);
            return BigDecimal.valueOf(hours).multiply(coefficient).setScale(2);
        }
    }

    protected void updatePaymentData(Payment paymentToCancel, LocalDateTime timeOfCancellationRequest, BigDecimal cancellationFee) {
        paymentToCancel.setCancelled(true);
        paymentToCancel.setCancellationFee(new Money(cancellationFee, Currency.EUR.getCode()));
        paymentToCancel.setCancellationTime(timeOfCancellationRequest);
    }
}
