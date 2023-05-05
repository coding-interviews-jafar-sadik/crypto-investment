package com.xm.crypto.dto;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class DateRange {

    LocalDate fromDate;

    LocalDate toDate;

    public boolean isWithinRange(LocalDateTime localDateTime) {
        LocalDate localDate = localDateTime.toLocalDate();
        return !localDate.isBefore(fromDate) && !localDate.isAfter(toDate);
    }

    public static DateRange unbounded() {
        return new DateRange(LocalDate.of(1, 1, 1), LocalDate.of(9999, 1, 1));
    }
}
