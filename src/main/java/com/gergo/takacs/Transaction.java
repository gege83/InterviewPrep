package com.gergo.takacs;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Transaction(
        UUID transactionId,
        CreditDebitIndicator creditDebitIndicator,
        BigDecimal amount,
        String currency,
        OffsetDateTime bookingDateTime
) {
}
