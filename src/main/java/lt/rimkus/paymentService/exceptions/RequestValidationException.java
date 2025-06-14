package lt.rimkus.paymentService.exceptions;

public class RequestValidationException extends Exception {
    public RequestValidationException(String message) {
        super(message);
    }
}
