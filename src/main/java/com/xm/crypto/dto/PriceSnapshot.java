package com.xm.crypto.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class PriceSnapshot {

    LocalDateTime timestamp;

    BigDecimal price;
}
