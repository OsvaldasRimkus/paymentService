package lt.rimkus.paymentService.DTOs;

import java.math.BigDecimal;

public class CreatePaymentRequestDTO {
    private String type;
    private MoneyDTO money;
    private String debtor_iban;
    private String creditor_iban;
    private String creditorBankBIC;

    private String details;

    public CreatePaymentRequestDTO() {
    }

    public CreatePaymentRequestDTO(String type, BigDecimal amount, String currency, String debtorIban, String creditorIban) {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MoneyDTO getMoney() {
        return money;
    }

    public void setMoney(MoneyDTO money) {
        this.money = money;
    }

    public String getDebtor_iban() {
        return debtor_iban;
    }

    public void setDebtor_iban(String debtor_iban) {
        this.debtor_iban = debtor_iban;
    }

    public String getCreditor_iban() {
        return creditor_iban;
    }

    public void setCreditor_iban(String creditor_iban) {
        this.creditor_iban = creditor_iban;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCreditorBankBIC() {
        return creditorBankBIC;
    }

    public void setCreditorBankBIC(String creditorBankBIC) {
        this.creditorBankBIC = creditorBankBIC;
    }
}
