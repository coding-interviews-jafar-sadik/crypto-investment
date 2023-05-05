package com.xm.crypto.utils;

import com.xm.crypto.dto.PriceSnapshot;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

public class PriceSnapshotTestUtil {

    public static Flux<PriceSnapshot> priceHistory(Float... history) {
        return Flux.fromStream(Arrays.stream(history)
                .map(value -> new PriceSnapshot(LocalDateTime.now(), new BigDecimal(value))));
    }

}
