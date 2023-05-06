package com.xm.crypto.support;

import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.dto.PriceSnapshot;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

public class TestBuilders {

    public static Flux<PriceSnapshot> priceHistory(Float... history) {
        return Flux.fromStream(Arrays.stream(history)
                .map(value -> new PriceSnapshot(LocalDateTime.now(), new BigDecimal(value))));
    }

    public static PriceRangeDetails priceRangeDetails(String symbol, float oldest, float newest, float min, float max) {
        return new PriceRangeDetails(symbol, new BigDecimal(oldest), new BigDecimal(newest), new BigDecimal(min), new BigDecimal(max));
    }

    public static PriceSnapshot priceSnapshot(String dateTime, String price) {
        return new PriceSnapshot(LocalDateTime.parse(dateTime), new BigDecimal(price));
    }
}
