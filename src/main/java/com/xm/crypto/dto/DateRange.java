package com.xm.crypto.dto;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class DateRange {

    public final static LocalDate MIN = LocalDate.MIN;
    public final static LocalDate MAX = LocalDate.MAX;

    LocalDate fromDate;

    LocalDate toDate;

    public static DateRange of(LocalDate fromDate, LocalDate toDate) {
        return new DateRange(fromDate, toDate);
    }

    public static DateRange of(LocalDateTime fromDate, LocalDateTime toDate) {
        return new DateRange(fromDate.toLocalDate(), toDate.toLocalDate());
    }

    public boolean isWithinRange(LocalDateTime localDateTime) {
        LocalDate localDate = localDateTime.toLocalDate();
        return !localDate.isBefore(fromDate) && !localDate.isAfter(toDate);
    }

    public static DateRange unbounded() {
        return new DateRange(MIN, MAX);
    }
}
