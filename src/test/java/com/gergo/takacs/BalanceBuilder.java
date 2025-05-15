package com.gergo.takacs;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class BalanceBuilder {
    private LocalDate date = LocalDate.now();
    private BigDecimal amount = BigDecimal.ZERO;
    private String currency = "USD";

    public BalanceBuilder() {
    }

    public static BalanceBuilder aBalance() {
        return new BalanceBuilder();
    }

    public static BalanceBuilder aBalance(LocalDate date) {
        return aBalance().withDate(date);
    }

    public BalanceBuilder withDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public BalanceBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public BalanceBuilder withCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public Balance build() {
        return new Balance(date, amount, currency);
    }
}
