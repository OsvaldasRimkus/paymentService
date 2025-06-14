package lt.rimkus.paymentService.DTOs;

public class TYPE3PaymentDTO extends PaymentDTO {
    private String creditorBankBIC;

    public String getCreditorBankBIC() {
        return creditorBankBIC;
    }

    public void setCreditorBankBIC(String creditorBankBIC) {
        this.creditorBankBIC = creditorBankBIC;
    }
}
