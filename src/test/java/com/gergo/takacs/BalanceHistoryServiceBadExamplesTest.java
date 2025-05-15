package com.gergo.takacs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;

import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * These tests are the bad examples
 */
public class BalanceHistoryServiceBadExamplesTest {
    public static final String DEFAULT_CURRENCY = "USD";
    private BalanceHistoryService balanceHistoryService;

    @BeforeEach
    public void setUp() {
        balanceHistoryService = new BalanceHistoryService();
    }

    @Test
    public void shouldReturnAccountBalanceIfStartDateEqualsAccountBalanceDate() {
        LocalDate startDate = LocalDate.of(2025, Month.MAY, 14);
        Balance accountBalance = new Balance(startDate, ZERO, DEFAULT_CURRENCY);
        List<Transaction> transactions = List.of();

        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, startDate, transactions);

        assertEquals(List.of(accountBalance), history);
    }

    @Test
    public void shouldShowTheSameBalanceForAllDaysIfNoTransactionsPresent() {
        LocalDate currentDate = LocalDate.of(2025, Month.MAY, 14);
        Balance accountBalance = new Balance(currentDate, ZERO, DEFAULT_CURRENCY);
        List<Transaction> transactions = List.of();
        // first test
        LocalDate startDate = currentDate.minusDays(1);
        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, startDate, transactions);

        assertEquals(
                List.of(
                        accountBalance,
                        new Balance(currentDate.minusDays(1), ZERO, DEFAULT_CURRENCY)
                ), history);

        // second test
        startDate = currentDate.minusDays(3);
        history = balanceHistoryService.calculateHistory(accountBalance, startDate, transactions);

        assertEquals(
                List.of(
                        accountBalance,
                        new Balance(currentDate.minusDays(1), ZERO, DEFAULT_CURRENCY),
                        new Balance(currentDate.minusDays(2), ZERO, DEFAULT_CURRENCY),
                        new Balance(currentDate.minusDays(3), ZERO, DEFAULT_CURRENCY)
                ), history);
    }

    @Test
    public void shouldShowClosingBalanceIfTransactionsPresent() {
        LocalDate currentDate = LocalDate.of(2025, Month.MAY, 14);
        LocalDate startDate = currentDate.minusDays(2);
        Balance accountBalance = new Balance(currentDate, ZERO, DEFAULT_CURRENCY);
        List<Transaction> transactions = List.of(
                new Transaction(UUID.randomUUID(), CreditDebitIndicator.CREDIT, BigDecimal.valueOf(23.22), DEFAULT_CURRENCY, OffsetDateTime.of(currentDate, LocalTime.of(12, 0), ZoneOffset.UTC)),
                new Transaction(UUID.randomUUID(), CreditDebitIndicator.DEBIT, ONE, DEFAULT_CURRENCY, OffsetDateTime.of(currentDate, LocalTime.of(12, 3), ZoneOffset.UTC)),
                new Transaction(UUID.randomUUID(), CreditDebitIndicator.DEBIT, ONE, DEFAULT_CURRENCY, OffsetDateTime.of(currentDate.minusDays(1), LocalTime.of(12, 0), ZoneOffset.UTC))
        );

        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, startDate, transactions);

        assertEquals(List.of(
                new Balance(currentDate, ZERO, DEFAULT_CURRENCY),
                new Balance(currentDate.minusDays(1), BigDecimal.valueOf(-22.22), DEFAULT_CURRENCY),
                new Balance(currentDate.minusDays(2), BigDecimal.valueOf(-21.22), DEFAULT_CURRENCY)
        ), history);
    }

    @Test
    public void shouldExcludeTransactionIfTransactionDateIsOutOfRange() {
        LocalDate currentDate = LocalDate.of(2025, Month.MAY, 14);
        LocalDate startDate = currentDate.minusDays(1);
        Balance accountBalance = new Balance(currentDate, ZERO, DEFAULT_CURRENCY);
        List<Transaction> transactions = List.of(
                new Transaction(UUID.randomUUID(), CreditDebitIndicator.CREDIT, BigDecimal.valueOf(23.22), DEFAULT_CURRENCY, OffsetDateTime.of(startDate.minusDays(1), LocalTime.of(12, 0), ZoneOffset.UTC)),
                new Transaction(UUID.randomUUID(), CreditDebitIndicator.DEBIT, ONE, DEFAULT_CURRENCY, OffsetDateTime.of(currentDate.plusDays(1), LocalTime.of(12, 3), ZoneOffset.UTC))
        );

        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, startDate, transactions);

        assertEquals(List.of(accountBalance, new Balance(startDate, ZERO, DEFAULT_CURRENCY)), history);
    }

    @Test
    public void shouldCalculateFromCurrentBalance() {
        LocalDate currentDate = LocalDate.of(2025, Month.MAY, 14);
        LocalDate startDate = currentDate.minusDays(1);
        Balance accountBalance = new Balance(currentDate, TEN, DEFAULT_CURRENCY);
        List<Transaction> transactions = List.of(
                new Transaction(UUID.randomUUID(), CreditDebitIndicator.CREDIT, BigDecimal.valueOf(23.22), DEFAULT_CURRENCY, OffsetDateTime.of(currentDate, LocalTime.of(12, 0), ZoneOffset.UTC)),
                new Transaction(UUID.randomUUID(), CreditDebitIndicator.DEBIT, ONE, DEFAULT_CURRENCY, OffsetDateTime.of(currentDate, LocalTime.of(12, 3), ZoneOffset.UTC))
        );

        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, startDate, transactions);

        assertEquals(List.of(accountBalance, new Balance(startDate, BigDecimal.valueOf(-12.22), DEFAULT_CURRENCY)), history);
    }

    @Test
    public void shouldThrowExceptionIfTransactionCurrencyDoesNotMatchAccountCurrency() {
        LocalDate currentDate = LocalDate.of(2025, Month.MAY, 14);
        LocalDate startDate = currentDate.minusDays(1);
        Balance accountBalance = new Balance(currentDate, TEN, "USD");
        List<Transaction> transactions = List.of(
                new Transaction(UUID.randomUUID(), CreditDebitIndicator.CREDIT, BigDecimal.valueOf(23.22), "SAR", OffsetDateTime.of(currentDate, LocalTime.of(12, 0), ZoneOffset.UTC)),
                new Transaction(UUID.randomUUID(), CreditDebitIndicator.DEBIT, ONE, "BHD", OffsetDateTime.of(currentDate, LocalTime.of(12, 3), ZoneOffset.UTC))
        );

        assertThrows(IllegalArgumentException.class, ()-> balanceHistoryService.calculateHistory(accountBalance, startDate, transactions));
    }
}
