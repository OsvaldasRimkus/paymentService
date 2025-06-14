package lt.rimkus.paymentService.constants;

import java.math.BigDecimal;

public class Constants {
    public static final String TYPE_1 = "TYPE1";
    public static final String TYPE_4 = "TYPE4";
    public static final String EUR = "EUR";
    public static final String USD = "USD";
    public static final String GBP = "GBP";

    public static final String TYPE_2 = "TYPE2";
    public static final String TYPE_3 = "TYPE3";
    public static final String UNKNOWN = "UNKNOWN";

    public static final BigDecimal HUNDRED = new BigDecimal(100);
    public static final BigDecimal NEGATIVE_HUNDRED = new BigDecimal(-100);
    public static final String DEBTOR_IBAN = "DE89370400440532013000";
    public static final String CREDIITOR_IBAN = "FR1420041010050500013M02606";
    public static final String SOME_PAYMENT_DETAILS = "Some payment details";
}
