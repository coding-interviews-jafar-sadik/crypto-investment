package com.xm.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@AllArgsConstructor
public class PriceRangeDetails {

    String symbol;

    BigDecimal oldest;

    BigDecimal newest;

    BigDecimal min;

    BigDecimal max;

    public float normalizedRange() {
        return (max.floatValue() - min.floatValue()) / min.floatValue();
    }

}
