package lt.rimkus.paymentService.DTOs;

import java.math.BigDecimal;

public class GetNotCancelledPaymentsDTO {
    boolean filter;
    BigDecimal minAmount = null;
    BigDecimal maxAmount = null;

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
}
