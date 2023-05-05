package com.xm.crypto.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@AllArgsConstructor
@ApiModel(value = "PriceRangeDetails", description = "Cryptocurrency price range details for a given time range")
public class PriceRangeDetails {

    @ApiModelProperty(value = "Cryptocurrency symbol", example = "BTC")
    String symbol;

    @ApiModelProperty(value = "Oldest price(USD) in a given time range. ", example = "1.5")
    BigDecimal oldest;

    @ApiModelProperty(value = "Newest price(USD) in a given time range", example = "2.1")
    BigDecimal newest;

    @ApiModelProperty(value = "Minimum price(USD) in a given time range", example = "1.5")
    BigDecimal min;

    @ApiModelProperty(value = "Maximum price(USD) in a given time range", example = "2.1")
    BigDecimal max;

    public float normalizedRange() {
        return (max.floatValue() - min.floatValue()) / min.floatValue();
    }
}
