package lt.rimkus.paymentService.enums;

import lt.rimkus.paymentService.exceptions.RequestValidationException;

import java.util.Arrays;

import static lt.rimkus.paymentService.messages.ValidationErrorMessages.CURRENCY_NOT_SUPPORTED;

public enum Currency {
    EUR("EUR"),
    USD("USD");

    private final String code;

    Currency(String code) {
        this.code = code;
    }

    public static boolean checkCurrencyValidity(String code) throws RequestValidationException {
        for (Currency currencyType : values()) {
            if (currencyType.code.equals(code)) {
                return true;
            }
        }
        throw new RequestValidationException(CURRENCY_NOT_SUPPORTED + code);
    }

    public static Currency fromCode(String code) {
        if (code == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(c -> c.code.equals(code.trim()))
                .findFirst()
                .orElse(null);
    }

    public String getCode() {
        return code;
    }
}
