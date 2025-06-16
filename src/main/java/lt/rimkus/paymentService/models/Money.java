package lt.rimkus.paymentService.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import static lt.rimkus.paymentService.messages.OtherMessages.AMOUNT_NOT_NULL;
import static lt.rimkus.paymentService.messages.OtherMessages.CURRENCY_NOT_NULL;

@Embeddable
public class Money {
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "currency", nullable = false)
    private String currency;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NotNull BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException(AMOUNT_NOT_NULL);
        }
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(@NotNull String currency) {
        if (currency == null) {
            throw new IllegalArgumentException(CURRENCY_NOT_NULL);
        }
        this.currency = currency.trim();
    }
}
