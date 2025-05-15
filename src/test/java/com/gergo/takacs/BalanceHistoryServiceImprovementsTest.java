package com.gergo.takacs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static com.gergo.takacs.BalanceBuilder.aBalance;
import static com.gergo.takacs.TransactionBuilder.aCreditTransaction;
import static com.gergo.takacs.TransactionBuilder.aDebitTransaction;
import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * These tests are the same tests as in the bad examples
 * improvements:
 * - one assertion per test
 * - using the builder pattern to enhance readability and focus on values that matter for the test
 * - creating methods for assertion for easier read and remove duplication
 * - using parametrized test to remove duplication
 *
 */
public class BalanceHistoryServiceImprovementsTest {
    private BalanceHistoryService balanceHistoryService;

    private final LocalDate currentDate = LocalDate.now();
    private final Balance currentAccountBalance = aBalance().withDate(currentDate).build();

    @BeforeEach
    public void setUp() {
        balanceHistoryService = new BalanceHistoryService();
    }

    @Test
    public void shouldReturnAccountBalanceIfStartDateEqualsAccountBalanceDate() {
        Balance accountBalance = aBalance(currentDate).build();

        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, currentDate, List.of());

        assertEquals(List.of(accountBalance), history);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3})
    public void shouldShowTheSameBalanceForAllDaysIfNoTransactionsPresent(int days) {
        Balance accountBalance = aBalance(currentDate).withAmount(ZERO).build();
        LocalDate startDate = currentDate.minusDays(days);
        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, startDate, List.of());

        assertAllEqualsTo(history, ZERO);
    }

    private static void assertAllEqualsTo(List<Balance> history, BigDecimal value) {
        history.forEach(b -> assertEquals(value, b.amount()));
    }

    @Test
    public void shouldShowConsecutiveDatesCountingFromCurrentDateBack() {
        LocalDate startDate = currentDate.minusDays(5);

        List<Balance> history = balanceHistoryService.calculateHistory(currentAccountBalance, startDate, List.of());

        assertBalanceDaysAreEqualsTo(history,
                currentDate,
                currentDate.minusDays(1),
                currentDate.minusDays(2),
                currentDate.minusDays(3),
                currentDate.minusDays(4),
                currentDate.minusDays(5)
        );
    }

    private void assertBalanceDaysAreEqualsTo(List<Balance> history, LocalDate... expectedDatesInOrder) {
        assertEquals(expectedDatesInOrder.length, history.size(), "Incorrect number of days in balance history");
        for (int i = 0; i < expectedDatesInOrder.length; i++) {
            assertEquals(expectedDatesInOrder[i], history.get(i).date(), "Incorrect date in balance history, at index: "+i);
        }
    }

    @Test
    public void shouldShowClosingBalanceIfTransactionsPresent() {
        LocalDate startDate = currentDate.minusDays(2);
        Balance accountBalance = aBalance(currentDate).withAmount(ZERO).build();
        List<Transaction> transactions = List.of(
                aCreditTransaction().withAmount(BigDecimal.valueOf(23.22)).withBookingDate(currentDate).build(),
                aDebitTransaction().withAmount(ONE).withBookingDate(currentDate).build(),
                aDebitTransaction().withAmount(ONE).withBookingDate(currentDate.minusDays(1)).build()
        );

        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, startDate, transactions);

        assertBalanceHistoryAmounts(history, ZERO, BigDecimal.valueOf(-22.22), BigDecimal.valueOf(-21.22));

    }

    private void assertBalanceHistoryAmounts(List<Balance> history, BigDecimal... expectedBalances) {
        assertEquals(expectedBalances.length, history.size(), "Incorrect number of days in balance history");
        for (int i = 0; i < expectedBalances.length; i++) {
            assertEquals(expectedBalances[i], history.get(i).amount(), "Incorrect amount in balance history, at index: "+i);
        }
    }

    @Test
    public void shouldExcludeTransactionIfTransactionDateIsOutOfRange() {
        LocalDate startDate = currentDate.minusDays(1);
        Balance accountBalance = aBalance(currentDate).withAmount(ZERO).build();
        List<Transaction> transactions = List.of(
                aCreditTransaction().withAmount(TEN).withBookingDate(currentDate.minusDays(1)).build(),
                aDebitTransaction().withAmount(ONE).withBookingDate(currentDate.plusDays(1)).build()
        );

        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, startDate, transactions);

        assertBalanceHistoryAmounts(history, ZERO, ZERO);
    }

    @Test
    public void shouldCalculateFromCurrentBalance() {
        LocalDate startDate = currentDate.minusDays(1);
        BigDecimal currentBalance = TEN;
        Balance accountBalance = aBalance(currentDate).withAmount(currentBalance).build();
        List<Transaction> transactions = List.of(
                aCreditTransaction().withAmount(BigDecimal.valueOf(23.22)).withBookingDate(currentDate).build(),
                aDebitTransaction().withAmount(ONE).withBookingDate(currentDate).build()
        );

        List<Balance> history = balanceHistoryService.calculateHistory(accountBalance, startDate, transactions);

        assertBalanceHistoryAmounts(history, currentBalance, BigDecimal.valueOf(-12.22));
    }

    @Test
    public void shouldThrowExceptionIfTransactionCurrencyDoesNotMatchAccountCurrency() {
        LocalDate startDate = currentDate.minusDays(1);
        Balance accountBalance = aBalance().withCurrency("USD").build();
        List<Transaction> transactions = List.of(
                aCreditTransaction().withCurrency("SAR").build(),
                aDebitTransaction().withCurrency("BHD").build()
        );

        assertThrows(IllegalArgumentException.class, () -> balanceHistoryService.calculateHistory(accountBalance, startDate, transactions));
    }
}
