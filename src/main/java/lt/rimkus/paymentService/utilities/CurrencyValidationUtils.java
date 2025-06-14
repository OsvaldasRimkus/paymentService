package lt.rimkus.paymentService.utilities;

import lt.rimkus.paymentService.enums.Currency;
import lt.rimkus.paymentService.enums.PaymentType;

import java.util.Map;
import java.util.Set;

public final class CurrencyValidationUtils {

    private CurrencyValidationUtils() {
        // Utility class cannot be instantiated
    }

    private static final Map<PaymentType, Set<Currency>> PAYMENT_TYPE_AND_CURRENCY_MAP =
            Map.of(
                    PaymentType.TYPE1, Set.of(Currency.EUR),
                    PaymentType.TYPE2, Set.of(Currency.USD),
                    PaymentType.TYPE3, Set.of(Currency.EUR, Currency.USD)
            );

    public static boolean isCurrencyNotValid(String currencyCode) {
        for (Currency currencyType : Currency.values()) {
            if (currencyType.getCode().equals(currencyCode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCurrencyNotValidForPaymentType(String currencyCode, String paymentTypeCode) {
        Currency currency = Currency.fromCode(currencyCode);
        PaymentType paymentType = PaymentType.fromCode(paymentTypeCode);

        if (currency == null || paymentType == null) {
            return true;
        }

        Set<Currency> allowedCurrencies = PAYMENT_TYPE_AND_CURRENCY_MAP.get(paymentType);
        return allowedCurrencies == null || !allowedCurrencies.contains(currency);
    }
}
