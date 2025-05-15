package com.gergo.takacs;

public enum CreditDebitIndicator {
    CREDIT(-1), DEBIT(1);
    public final int multiplier;
    CreditDebitIndicator(int multiplier) {
        this.multiplier = multiplier;
    }
}
