package lt.rimkus.paymentService.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Embeddable
public class Money {
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private String currency;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NotNull BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(@NotNull String currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        this.currency = currency.trim();
    }
}
