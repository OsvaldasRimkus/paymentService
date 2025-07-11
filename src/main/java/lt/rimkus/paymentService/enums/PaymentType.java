package lt.rimkus.paymentService.enums;

import lt.rimkus.paymentService.exceptions.RequestValidationException;

import java.util.Arrays;

import static lt.rimkus.paymentService.messages.ValidationErrorMessages.UNSUPPORTED_TYPE;

public enum PaymentType {
    TYPE1("TYPE1"),
    TYPE2("TYPE2"),
    TYPE3("TYPE3");

    private final String code;

    PaymentType(String code) {
        this.code = code;
    }

    public static PaymentType fromCode(String code) {
        if (code == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public String getCode() {
        return code;
    }
}
