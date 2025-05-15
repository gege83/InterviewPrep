package com.gergo.takacs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BalanceHistoryService {
    public List<Balance> calculateHistory(Balance accountBalance, LocalDate startDate, List<Transaction> transactions) {
        var currency = accountBalance.currency();
        assertTransactionsCurrency(currency, transactions);
        return calculateBalanceHistory(accountBalance, startDate, transactions, currency);
    }

    private List<Balance> calculateBalanceHistory(Balance accountBalance, LocalDate startDate, List<Transaction> transactions, String currency) {
        List<Balance> balances = new ArrayList<>();
        var date = accountBalance.date();
        BigDecimal currentBalance = accountBalance.amount();
        do {
            balances.add(new Balance(date, currentBalance, currency));
            currentBalance=currentBalance.add(sum(transactions, date));
            date = date.minusDays(1);
        } while (!date.isBefore(startDate));
        return balances;
    }

    private void assertTransactionsCurrency(String currency, List<Transaction> transactions) {
        if(transactions.stream().anyMatch(transaction -> !transaction.currency().equals(currency))) {
            throw new IllegalArgumentException();
        }
    }

    private BigDecimal sum(List<Transaction> transactions, LocalDate date) {
        return transactions.stream()
                .filter(t->t.bookingDateTime().toLocalDate().equals(date))
                .map(t -> t.amount().multiply(BigDecimal.valueOf(t.creditDebitIndicator().multiplier)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
