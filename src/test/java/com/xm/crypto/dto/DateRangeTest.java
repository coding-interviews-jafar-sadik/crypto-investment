package com.xm.crypto.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangeTest {

    private final LocalDate DATE_2023_03_01 = LocalDate.of(2023, 3, 1);
    private final LocalDate DATE_2023_03_10 = LocalDate.of(2023, 3, 10);

    @Test
    void verifyThatDateTimeIsWithinUnboundedRange() {
        assertTrue(DateRange.unbounded().isWithinRange(LocalDateTime.now()));
    }

    @Test
    void verifyThatDateTimeIsInsideGivenRange() {
        assertTrue(new DateRange(DATE_2023_03_01, DATE_2023_03_10)
                .isWithinRange(LocalDateTime.of(2023, 3, 1, 0, 0)));

        assertTrue(new DateRange(DATE_2023_03_01, DATE_2023_03_10)
                .isWithinRange(LocalDateTime.of(2023, 3, 10, 0, 0)));
    }

    @Test
    void verifyThatDateIsOutsideGivenRange() {
        assertFalse(new DateRange(DATE_2023_03_01, DATE_2023_03_10)
                .isWithinRange(LocalDateTime.of(2023, 2, 28, 0, 0)));

        assertFalse(new DateRange(DATE_2023_03_01, DATE_2023_03_10)
                .isWithinRange(LocalDateTime.of(2023, 3, 11, 0, 0)));
    }
}