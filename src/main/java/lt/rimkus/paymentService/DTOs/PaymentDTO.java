package lt.rimkus.paymentService.DTOs;

public abstract class PaymentDTO {
    private Long id;
    private String type;
    private MoneyDTO money;
    private String debtor_iban;
    private String creditor_iban;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

}
