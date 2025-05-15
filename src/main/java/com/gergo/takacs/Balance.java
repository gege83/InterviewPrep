package com.gergo.takacs;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Balance(LocalDate date, BigDecimal amount, String currency) {
}
