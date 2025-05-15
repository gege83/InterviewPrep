package com.gergo.takacs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public final class TransactionBuilder {
    private UUID transactionId = UUID.randomUUID();
    private CreditDebitIndicator creditDebitIndicator = CreditDebitIndicator.CREDIT;
    private BigDecimal amount = BigDecimal.ZERO;
    private String currency = "USD";
    private OffsetDateTime bookingDateTime = OffsetDateTime.now();

    public TransactionBuilder() {
    }

    public static TransactionBuilder aCreditTransaction() {
        return new TransactionBuilder().withCreditDebitIndicator(CreditDebitIndicator.CREDIT);
    }

    public static TransactionBuilder aDebitTransaction() {
        return new TransactionBuilder().withCreditDebitIndicator(CreditDebitIndicator.DEBIT);
    }

    public TransactionBuilder withTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public TransactionBuilder withCreditDebitIndicator(CreditDebitIndicator creditDebitIndicator) {
        this.creditDebitIndicator = creditDebitIndicator;
        return this;
    }

    public TransactionBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransactionBuilder withCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public TransactionBuilder withBookingDate(LocalDate bookingDate) {
        return withBookingDateTime(OffsetDateTime.of(bookingDate, LocalTime.of(12, 0), ZoneOffset.UTC));
    }

    public TransactionBuilder withBookingDateTime(OffsetDateTime bookingDateTime) {
        this.bookingDateTime = bookingDateTime;
        return this;
    }

    public Transaction build() {
        return new Transaction(transactionId, creditDebitIndicator, amount, currency, bookingDateTime);
    }
}
