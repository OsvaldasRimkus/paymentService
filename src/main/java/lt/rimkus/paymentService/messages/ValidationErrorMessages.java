package lt.rimkus.paymentService.messages;

public class ValidationErrorMessages {
    public static final String CREATION_REQUEST_NULL = "Payment creation request cannot be null";
    public static final String UNSUPPORTED_TYPE = "Unsupported payment type: ";
    public static final String CURRENCY_NOT_SUPPORTED = "Unsupported currency code: ";

    public static final String TYPE_MANDATORY = "Type is required";
    public static final String AMOUNT_MANDATORY = "Amount is required and must be more than 0";
    public static final String INCORRECT_AMOUNT_VALUE = "Please provide an amount with no more than 2 decimal places";
    public static final String CURRENCY_MANDATORY = "Currency is required";
    public static final String MONEY_MISSING = "Please check your request structure, currency and amount should go as money";
    public static final String DEBTOR_IBAN_MANDATORY = "Debtor IBAN is required";
    public static final String CREDITOR_IBAN_MANDATORY = "Creditor IBAN is required";

    public static final String TYPE_NOT_COMPATIBLE_WITH_CURRENCY = " payment is not allowed to be used with currency ";
    public static final String DETAILS_MANDATORY_FOR_TYPE1 = "TYPE1 payment requires details to be provided";
    public static final String CREDITOR_BANK_BIC_MANDATORY_FOR_TYPE3 = "TYPE3 payment requires creditor bank BIC to be provided";

    public static final String PAYMENT_WITH_ID = "Payment with id ";
    public static final String IS_ALREADY_CANCELED = " is already canceled";
    public static final String SAME_DAY_CANCELLATION_ONLY = "Payment can be cancelled only on the day of its creation";
    public static final String NO_DATA_FOR_PAYMENT_TYPE = "No data found for payment type: ";
    public static final String WAS_CANCELLED_WITH_FEE = " was successfully cancelled. Cancellation fee is: ";
    public static final String PAYMENT_DOES_NOT_EXIST = "Provided payment id does not exist";
}
